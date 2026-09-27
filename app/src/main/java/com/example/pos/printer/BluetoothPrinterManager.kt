package com.example.pos.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.pos.model.BluetoothDeviceInfo
import com.example.pos.model.PrinterConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.UUID

class BluetoothPrinterManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "POSPrinterManager"
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val MAX_RECONNECT_ATTEMPTS = 3
    }

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _connectionState = MutableStateFlow<PrinterConnectionState>(PrinterConnectionState.Disconnected)
    val connectionState: StateFlow<PrinterConnectionState> = _connectionState.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDeviceInfo>> = _pairedDevices.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDeviceInfo>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var activeSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private val socketMutex = Mutex()

    private var reconnectJob: Job? = null
    private var reconnectAttempts = 0

    private val discoveryReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(c: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    if (device != null) {
                        val name = try {
                            device.name ?: "Unknown Bluetooth Device"
                        } catch (e: SecurityException) {
                            "Unknown Device"
                        }
                        val info = BluetoothDeviceInfo(
                            name = name,
                            address = device.address,
                            isBonded = device.bondState == BluetoothDevice.BOND_BONDED
                        )
                        val current = _discoveredDevices.value.toMutableList()
                        if (current.none { it.address == info.address }) {
                            current.add(info)
                            _discoveredDevices.value = current
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }
            }
        }
    }

    private var isReceiverRegistered = false

    fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    @SuppressLint("MissingPermission")
    fun loadPairedDevices() {
        if (!hasBluetoothPermission() || bluetoothAdapter == null) {
            _pairedDevices.value = emptyList()
            return
        }
        try {
            val bonded = bluetoothAdapter.bondedDevices ?: emptySet()
            val list = bonded.map { device ->
                BluetoothDeviceInfo(
                    name = device.name ?: "Unknown Device",
                    address = device.address,
                    isBonded = true
                )
            }.sortedWith(compareByDescending<BluetoothDeviceInfo> {
                it.name.contains("8380", ignoreCase = true) || it.name.contains("POS", ignoreCase = true)
            }.thenBy { it.name })
            _pairedDevices.value = list
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching paired devices", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!hasScanPermission() || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            return
        }
        registerDiscoveryReceiver()
        _discoveredDevices.value = emptyList()
        _isScanning.value = true
        try {
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
            bluetoothAdapter.startDiscovery()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting discovery", e)
            _isScanning.value = false
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (bluetoothAdapter?.isDiscovering == true) {
            try {
                bluetoothAdapter.cancelDiscovery()
            } catch (e: Exception) {
                Log.e(TAG, "Error canceling discovery", e)
            }
        }
        _isScanning.value = false
    }

    private fun registerDiscoveryReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
            context.registerReceiver(discoveryReceiver, filter)
            isReceiverRegistered = true
        }
    }

    fun cleanup() {
        disconnect()
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(discoveryReceiver)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver", e)
            }
            isReceiverRegistered = false
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun connect(macAddress: String, expectedName: String = "POS-8380"): Result<Unit> = withContext(Dispatchers.IO) {
        if (!hasBluetoothPermission()) {
            val err = "Bluetooth permission not granted"
            _connectionState.value = PrinterConnectionState.Error(err)
            return@withContext Result.failure(SecurityException(err))
        }
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            val err = "Bluetooth is turned off"
            _connectionState.value = PrinterConnectionState.Error(err)
            return@withContext Result.failure(IllegalStateException(err))
        }

        // Clean any existing connection
        internalDisconnect()

        _connectionState.value = PrinterConnectionState.Connecting
        Log.i(TAG, "Connecting to printer at MAC: $macAddress...")

        try {
            // Cancel discovery before connecting as recommended by Android Bluetooth docs
            if (hasScanPermission() && bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }

            val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress)
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()

            if (socket.isConnected) {
                socketMutex.withLock {
                    activeSocket = socket
                    outputStream = socket.outputStream
                }
                val deviceName = try {
                    device.name ?: expectedName
                } catch (e: Exception) {
                    expectedName
                }
                _connectionState.value = PrinterConnectionState.Connected(deviceName, macAddress)
                reconnectAttempts = 0
                Log.i(TAG, "Successfully connected to $deviceName ($macAddress)")
                Result.success(Unit)
            } else {
                _connectionState.value = PrinterConnectionState.Error("Connection established but socket is closed")
                Result.failure(Exception("Socket not connected"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to printer at $macAddress", e)
            internalDisconnect()
            _connectionState.value = PrinterConnectionState.Error(e.localizedMessage ?: "Failed to connect to printer")
            Result.failure(e)
        }
    }

    fun triggerAutoReconnect(savedMac: String, savedName: String) {
        if (savedMac.isBlank() || reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            return
        }
        if (_connectionState.value is PrinterConnectionState.Connected ||
            _connectionState.value is PrinterConnectionState.Connecting
        ) {
            return
        }

        reconnectJob?.cancel()
        reconnectJob = scope.launch(Dispatchers.IO) {
            while (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                reconnectAttempts++
                Log.i(TAG, "Auto-reconnect attempt $reconnectAttempts of $MAX_RECONNECT_ATTEMPTS to $savedMac...")
                val result = connect(savedMac, savedName)
                if (result.isSuccess) {
                    Log.i(TAG, "Auto-reconnect succeeded on attempt $reconnectAttempts")
                    return@launch
                }
                // Backoff delay: 1.5s, 3s, 4.5s
                delay(1500L * reconnectAttempts)
            }
            Log.w(TAG, "Auto-reconnect stopped after $MAX_RECONNECT_ATTEMPTS failed attempts.")
        }
    }

    fun resetReconnectAttempts() {
        reconnectAttempts = 0
    }

    fun disconnect() {
        reconnectJob?.cancel()
        reconnectAttempts = 0
        scope.launch(Dispatchers.IO) {
            internalDisconnect()
            _connectionState.value = PrinterConnectionState.Disconnected
        }
    }

    private suspend fun internalDisconnect() = socketMutex.withLock {
        try {
            outputStream?.flush()
            outputStream?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing stream", e)
        } finally {
            outputStream = null
        }

        try {
            activeSocket?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing socket", e)
        } finally {
            activeSocket = null
        }
    }

    suspend fun sendBytes(bytes: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        socketMutex.withLock {
            val socket = activeSocket
            val stream = outputStream
            if (socket == null || stream == null || !socket.isConnected) {
                _connectionState.value = PrinterConnectionState.Error("Printer is not connected")
                return@withContext Result.failure(IllegalStateException("Printer is not connected"))
            }

            try {
                stream.write(bytes)
                stream.flush()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error writing data to printer", e)
                _connectionState.value = PrinterConnectionState.Error("Print error: ${e.localizedMessage}")
                Result.failure(e)
            }
        }
    }

    suspend fun printTestReceipt(shopName: String, autoCut: Boolean = true): Result<Unit> {
        val testData = StringBuilder().apply {
            append("\n")
            append("================================\n")
            append("      $shopName\n")
            append("   THERMAL PRINTER TEST RECEIPT\n")
            append("================================\n")
            append("Printer: POS-8380\n")
            append("Protocol: ESC/POS Bluetooth SPP\n")
            append("Status: ONLINE & READY\n")
            append("Auto Cut: ${if (autoCut) "ENABLED" else "DISABLED"}\n")
            append("Speed: High Speed Thermal\n")
            append("Characters: 1234567890 ABCXYZ\n")
            append("Currency Symbol: Rs.\n")
            append("--------------------------------\n")
            append("      PRINT TEST SUCCESSFUL!\n")
            append("================================\n\n\n\n")
        }.toString().toByteArray(Charset.forName("ISO-8859-1"))

        val fullPayload = if (autoCut) {
            byteArrayOf(
                *EscPosCommands.INIT,
                *EscPosCommands.ALIGN_CENTER,
                *testData,
                *EscPosCommands.PAPER_CUT
            )
        } else {
            byteArrayOf(
                *EscPosCommands.INIT,
                *EscPosCommands.ALIGN_CENTER,
                *testData
            )
        }
        return sendBytes(fullPayload)
    }
}
