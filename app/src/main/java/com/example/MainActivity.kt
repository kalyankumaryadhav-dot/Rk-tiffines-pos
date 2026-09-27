package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.pos.model.PrinterConnectionState
import com.example.pos.ui.MainPosScreen
import com.example.pos.ui.PosViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            MyApplicationTheme {
                MainPosScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Automatically reconnect to saved POS-8380 printer when resuming the app
        val currentSettings = viewModel.settings.value
        if (currentSettings.autoReconnectPrinter &&
            currentSettings.savedPrinterMac.isNotBlank() &&
            viewModel.printerManager.connectionState.value is PrinterConnectionState.Disconnected
        ) {
            viewModel.printerManager.triggerAutoReconnect(
                currentSettings.savedPrinterMac,
                currentSettings.savedPrinterName.ifBlank { "POS-8380" }
            )
        }
    }
}
