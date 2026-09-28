import React, { useState } from 'react';
import {
  Bluetooth,
  BluetoothConnected,
  BluetoothOff,
  Check,
  Cloud,
  CreditCard,
  Image as ImageIcon,
  Printer,
  RefreshCw,
  Save,
  Store,
  Trash2,
  Upload,
} from 'lucide-react';
import { usePos } from '../context/PosContext';
import {
  ElementConfig,
  ItemColumnConfig,
  PrinterPaperWidth,
  ReceiptAlignment,
  ReceiptFontSize,
  ReceiptFormatConfig,
} from '../types/pos';
import { generateCustomerReceiptHtml } from '../utils/receiptGenerator';

type SettingsSection = 'BUSINESS' | 'RECEIPT_FORMAT' | 'PRINTER' | 'BILLING' | 'SALES';

export const SettingsScreen: React.FC = () => {
  const [activeSection, setActiveSection] = useState<SettingsSection>('BUSINESS');
  const {
    settings,
    updateSettings,
    updateReceiptFormat,
    updatePaperWidth,
    setAutoCutPaper,
    resetNextBillNumber,
    resetNextTokenNumber,
    saveUploadedLogo,
    removeLogo,
    printerState,
    connectPrinter,
    disconnectPrinter,
    forgetPrinter,
    testPrint,
    syncState,
    triggerFirestoreSync,
  } = usePos();

  const sections: { id: SettingsSection; label: string }[] = [
    { id: 'BUSINESS', label: 'BUSINESS' },
    { id: 'RECEIPT_FORMAT', label: 'RECEIPT FORMAT' },
    { id: 'PRINTER', label: 'PRINTER' },
    { id: 'BILLING', label: 'BILLING' },
    { id: 'SALES', label: 'SALES' },
  ];

  return (
    <div className="flex-1 flex flex-col p-3 sm:p-4 bg-[#0C0B0A] overflow-y-auto max-w-6xl mx-auto w-full">
      {/* Title */}
      <div className="mb-3">
        <h2 className="text-lg sm:text-xl font-extrabold text-[#F9F6F0]">
          POS Terminal Settings
        </h2>
        <p className="text-xs text-[#B8B0A6]">
          Configure business details, POS-8380 thermal printer, receipt layout, and numbering
        </p>
      </div>

      {/* Tabs */}
      <div className="bg-[#161514] border border-[#FFB300]/25 rounded-xl p-1 flex items-center justify-between mb-4 shadow-sm overflow-x-auto no-scrollbar">
        {sections.map((sec) => {
          const isSelected = activeSection === sec.id;
          return (
            <button
              key={sec.id}
              onClick={() => setActiveSection(sec.id)}
              className={`flex-1 py-1.5 px-3 rounded-lg text-xs font-bold shrink-0 transition-all ${
                isSelected
                  ? 'bg-[#FFB300] text-[#140D00] shadow-sm'
                  : 'text-[#B8B0A6] hover:text-[#F9F6F0] hover:bg-[#22201D]'
              }`}
            >
              {sec.label}
            </button>
          );
        })}
      </div>

      {/* Section Content */}
      <div className="flex-1">
        {activeSection === 'BUSINESS' && (
          <BusinessSettings
            settings={settings}
            onSaveDetails={(name, address, phone) => {
              updateSettings({ shopName: name, address, phone });
            }}
            onUploadLogo={saveUploadedLogo}
            onRemoveLogo={removeLogo}
          />
        )}

        {activeSection === 'RECEIPT_FORMAT' && (
          <ReceiptFormatSettings
            settings={settings}
            onUpdateFormat={updateReceiptFormat}
            onUpdatePaperWidth={updatePaperWidth}
            onUpdateAutoCut={setAutoCutPaper}
            onTestPrint={testPrint}
          />
        )}

        {activeSection === 'PRINTER' && (
          <PrinterSettings
            settings={settings}
            printerState={printerState}
            onUpdateAutoPrint={(autoBill, autoToken, autoBoth, autoReconnect) => {
              updateSettings({
                autoPrintBill: autoBill,
                autoPrintToken: autoToken,
                autoPrintBoth: autoBoth,
                autoReconnectPrinter: autoReconnect,
              });
            }}
            onToggleAutoCut={setAutoCutPaper}
            onConnectPrinter={connectPrinter}
            onDisconnectPrinter={disconnectPrinter}
            onForgetPrinter={forgetPrinter}
            onTestPrint={testPrint}
          />
        )}

        {activeSection === 'BILLING' && (
          <BillingSettings
            settings={settings}
            onUpdateBillNumber={resetNextBillNumber}
            onUpdateTokenNumber={resetNextTokenNumber}
          />
        )}

        {activeSection === 'SALES' && (
          <SalesSettings
            syncState={syncState}
            onTriggerSync={triggerFirestoreSync}
          />
        )}
      </div>
    </div>
  );
};

// -----------------------------------------------------------------------------
// 1. BUSINESS SETTINGS
// -----------------------------------------------------------------------------
interface BusinessSettingsProps {
  settings: any;
  onSaveDetails: (name: string, address: string, phone: string) => void;
  onUploadLogo: (dataUrl: string) => void;
  onRemoveLogo: () => void;
}

const BusinessSettings: React.FC<BusinessSettingsProps> = ({
  settings,
  onSaveDetails,
  onUploadLogo,
  onRemoveLogo,
}) => {
  const [shopName, setShopName] = useState(settings.shopName);
  const [address, setAddress] = useState(settings.address);
  const [phone, setPhone] = useState(settings.phone);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        if (event.target?.result) {
          onUploadLogo(event.target.result as string);
        }
      };
      reader.readAsDataURL(file);
    }
  };

  return (
    <div className="space-y-4 max-w-2xl">
      {/* Profile Card */}
      <div className="bg-[#161514] border border-[#FFB300]/25 rounded-xl p-4 shadow-sm space-y-3">
        <h3 className="font-bold text-sm sm:text-base text-[#F9F6F0]">Business Profile</h3>

        <div>
          <label className="text-xs text-[#B8B0A6] font-medium block mb-1">Shop Name</label>
          <input
            type="text"
            value={shopName}
            onChange={(e) => setShopName(e.target.value)}
            className="w-full bg-[#22201D] border border-[#FFB300]/30 rounded-lg px-3 py-2 text-xs sm:text-sm text-[#F9F6F0] focus:outline-none focus:border-[#FFB300]"
          />
        </div>

        <div>
          <label className="text-xs text-[#B8B0A6] font-medium block mb-1">Shop Address</label>
          <input
            type="text"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            className="w-full bg-[#22201D] border border-[#FFB300]/30 rounded-lg px-3 py-2 text-xs sm:text-sm text-[#F9F6F0] focus:outline-none focus:border-[#FFB300]"
          />
        </div>

        <div>
          <label className="text-xs text-[#B8B0A6] font-medium block mb-1">Phone Number</label>
          <input
            type="text"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            className="w-full bg-[#22201D] border border-[#FFB300]/30 rounded-lg px-3 py-2 text-xs sm:text-sm text-[#F9F6F0] focus:outline-none focus:border-[#FFB300]"
          />
        </div>

        <div className="flex justify-end pt-2">
          <button
            onClick={() => onSaveDetails(shopName, address, phone)}
            className="px-4 py-2 rounded-lg bg-[#FFB300] text-[#140D00] font-bold text-xs hover:brightness-110 active:scale-95 transition-all"
          >
            Save Business Details
          </button>
        </div>
      </div>

      {/* Logo Card */}
      <div className="bg-[#161514] border border-[#FFB300]/25 rounded-xl p-4 shadow-sm space-y-3">
        <h3 className="font-bold text-sm sm:text-base text-[#F9F6F0]">Thermal Receipt Logo</h3>
        <p className="text-xs text-[#B8B0A6]">
          Upload a high-contrast logo to be rendered in crisp monochrome raster ESC/POS format on top of printed receipts.
        </p>

        {settings.logoUrl ? (
          <div className="flex items-center gap-4 py-2">
            <img
              src={settings.logoUrl}
              alt="Uploaded Logo"
              className="h-20 w-24 object-contain rounded-lg border border-[#FFB300]/30 bg-white p-1 filter grayscale contrast-200"
            />
            <div className="space-y-2">
              <span className="text-xs font-bold text-emerald-400 block">Logo Active</span>
              <div className="flex items-center gap-2">
                <label className="px-3 py-1.5 rounded-lg border border-[#FFB300]/40 text-xs font-semibold text-[#FFB300] hover:bg-[#22201D] cursor-pointer">
                  <span>Change</span>
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleFileChange}
                    className="hidden"
                  />
                </label>
                <button
                  onClick={onRemoveLogo}
                  className="px-3 py-1.5 rounded-lg border border-red-500/40 text-xs font-semibold text-red-400 hover:bg-red-950/20"
                >
                  Remove
                </button>
              </div>
            </div>
          </div>
        ) : (
          <label className="flex flex-col items-center justify-center p-6 border-2 border-dashed border-[#FFB300]/30 rounded-xl hover:border-[#FFB300] cursor-pointer bg-[#22201D]/40 transition-colors">
            <Upload className="w-8 h-8 text-[#FFB300] mb-2" />
            <span className="text-xs font-bold text-[#FFB300]">Upload Logo from Device</span>
            <span className="text-[11px] text-[#6E6760] mt-0.5">PNG, JPG, or SVG</span>
            <input
              type="file"
              accept="image/*"
              onChange={handleFileChange}
              className="hidden"
            />
          </label>
        )}
      </div>
    </div>
  );
};

// -----------------------------------------------------------------------------
// 2. RECEIPT FORMAT SETTINGS (All 20 elements)
// -----------------------------------------------------------------------------
interface ReceiptFormatSettingsProps {
  settings: any;
  onUpdateFormat: (format: ReceiptFormatConfig) => void;
  onUpdatePaperWidth: (width: PrinterPaperWidth) => void;
  onUpdateAutoCut: (enabled: boolean) => void;
  onTestPrint: () => void;
}

const ReceiptFormatSettings: React.FC<ReceiptFormatSettingsProps> = ({
  settings,
  onUpdateFormat,
  onUpdatePaperWidth,
  onUpdateAutoCut,
  onTestPrint,
}) => {
  const [format, setFormat] = useState<ReceiptFormatConfig>(settings.receiptFormat);
  const [paperWidth, setPaperWidth] = useState<PrinterPaperWidth>(settings.paperWidth);

  // Generate live preview dummy bill
  const dummyBill = {
    billId: 'LIVE-PREVIEW',
    billNumber: settings.nextBillNumber,
    tokenNumber: settings.nextTokenNumber,
    timestamp: Date.now(),
    dateString: '27-09-2026',
    timeString: '09:45 AM',
    orderType: 'DINE_IN' as const,
    tableNumber: '3',
    customerName: 'Kalyan Kumar',
    paymentMode: 'CASH' as const,
    items: [
      { menuItem: { id: 1, name: 'GHEE KARAM DOSA', category: 'DOSA', price: 65, isAvailable: true }, quantity: 2, total: 130 },
      { menuItem: { id: 2, name: 'IDLI (4 PCS)', category: 'IDLI', price: 45, isAvailable: true }, quantity: 1, total: 45 },
      { menuItem: { id: 3, name: 'FILTER COFFEE', category: 'BEVERAGES', price: 20, isAvailable: true }, quantity: 2, total: 40 },
    ],
    subtotal: 215,
    grandTotal: 215,
    itemCount: 5,
    isSynced: true,
  };

  const previewHtml = generateCustomerReceiptHtml(dummyBill, {
    ...settings,
    paperWidth,
    receiptFormat: format,
  });

  const handleSave = () => {
    onUpdateFormat(format);
    onUpdatePaperWidth(paperWidth);
    onUpdateAutoCut(format.autoCut);
  };

  return (
    <div className="flex flex-col xl:flex-row gap-4 items-start">
      {/* Configuration Controls (Left) */}
      <div className="flex-1 w-full space-y-4">
        {/* Top Control Bar */}
        <div className="bg-[#161514] border border-[#FFB300]/25 rounded-xl p-3.5 shadow-sm space-y-3">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div>
              <h3 className="font-bold text-sm sm:text-base text-[#F9F6F0]">
                Thermal Receipt Format
              </h3>
              <p className="text-xs text-[#B8B0A6]">
                Configure all 20 printed receipt elements directly via ESC/POS
              </p>
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={onTestPrint}
                className="flex items-center gap-1.5 px-3 py-2 rounded-lg bg-[#FFB300] text-[#140D00] font-bold text-xs hover:brightness-110 active:scale-95 transition-all shadow-sm"
              >
                <Printer className="w-4 h-4 font-bold" />
                <span>Test Print</span>
              </button>

              <button
                onClick={handleSave}
                className="flex items-center gap-1.5 px-3.5 py-2 rounded-lg bg-[#FF6D00] text-white font-bold text-xs hover:brightness-110 active:scale-95 transition-all shadow-sm"
              >
                <Save className="w-4 h-4" />
                <span>Save Format</span>
              </button>
            </div>
          </div>

          {/* Paper Width Selector */}
          <div className="flex items-center justify-between pt-2 border-t border-[#FFB300]/20 text-xs">
            <span className="font-semibold text-[#F9F6F0]">Paper Width:</span>
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setPaperWidth('WIDTH_80MM')}
                className={`px-3 py-1.5 rounded-lg font-bold text-xs transition-all ${
                  paperWidth === 'WIDTH_80MM'
                    ? 'bg-[#FFB300] text-[#140D00]'
                    : 'bg-[#22201D] text-[#B8B0A6] border border-[#FFB300]/20'
                }`}
              >
                80mm (POS-8380)
              </button>

              <button
                type="button"
                onClick={() => setPaperWidth('WIDTH_58MM')}
                className={`px-3 py-1.5 rounded-lg font-bold text-xs transition-all ${
                  paperWidth === 'WIDTH_58MM'
                    ? 'bg-[#FFB300] text-[#140D00]'
                    : 'bg-[#22201D] text-[#B8B0A6] border border-[#FFB300]/20'
                }`}
              >
                58mm Standard
              </button>
            </div>
          </div>
        </div>

        {/* 1. Logo */}
        <div className="bg-[#161514] border border-[#FFB300]/20 rounded-xl p-3 flex items-center justify-between">
          <div>
            <p className="font-bold text-sm text-[#F9F6F0]">1. Shop Logo</p>
            <p className="text-xs text-[#B8B0A6]">Print monochrome raster logo on top</p>
          </div>
          <SwitchToggle
            checked={format.logoVisible}
            onChange={(checked) => setFormat({ ...format, logoVisible: checked })}
          />
        </div>

        {/* 2. Shop Name */}
        <ElementConfigCard
          title="2. Shop Name"
          subtitle={settings.shopName}
          config={format.shopName}
          onChange={(cfg) => setFormat({ ...format, shopName: cfg })}
        />

        {/* 3. Address */}
        <ElementConfigCard
          title="3. Address"
          subtitle={settings.address}
          config={format.address}
          onChange={(cfg) => setFormat({ ...format, address: cfg })}
        />

        {/* 4. Phone Number */}
        <ElementConfigCard
          title="4. Phone Number"
          subtitle={settings.phone}
          config={format.phone}
          onChange={(cfg) => setFormat({ ...format, phone: cfg })}
        />

        {/* 5. Bill Number */}
        <ElementConfigCard
          title="5. Bill Number"
          subtitle="Sequential Bill # (e.g. #1001)"
          config={format.billNumber}
          onChange={(cfg) => setFormat({ ...format, billNumber: cfg })}
        />

        {/* 6. Token Number */}
        <ElementConfigCard
          title="6. Token Number"
          subtitle="Daily counter token (e.g. #1)"
          config={format.tokenNumber}
          onChange={(cfg) => setFormat({ ...format, tokenNumber: cfg })}
        />

        {/* 7. Date */}
        <ElementConfigCard
          title="7. Date"
          subtitle="Bill print date (dd-MM-yyyy)"
          config={format.date}
          onChange={(cfg) => setFormat({ ...format, date: cfg })}
        />

        {/* 8. Time */}
        <ElementConfigCard
          title="8. Time"
          subtitle="Bill print time (hh:mm a)"
          config={format.time}
          onChange={(cfg) => setFormat({ ...format, time: cfg })}
        />

        {/* 9. Order Type */}
        <ElementConfigCard
          title="9. Order Type (DINE IN / PARCEL)"
          subtitle="Prints TYPE: DINE IN or TYPE: PARCEL"
          config={format.orderType}
          onChange={(cfg) => setFormat({ ...format, orderType: cfg })}
        />

        {/* 10. Table Number */}
        <ElementConfigCard
          title="10. Table Number"
          subtitle="Prints Table # for Dine-in orders"
          config={format.tableNumber}
          onChange={(cfg) => setFormat({ ...format, tableNumber: cfg })}
        />

        {/* 11. Customer Name */}
        <ElementConfigCard
          title="11. Customer Name"
          subtitle="Prints customer name when entered"
          config={format.customerName}
          onChange={(cfg) => setFormat({ ...format, customerName: cfg })}
        />

        {/* 12. ITEM Heading */}
        <ElementConfigCard
          title="12. ITEM Heading Row"
          subtitle="Header row: ITEM, QTY, RATE, TOTAL"
          config={format.itemHeading}
          onChange={(cfg) => setFormat({ ...format, itemHeading: cfg })}
        />

        {/* 13. Item Name */}
        <ElementConfigCard
          title="13. Item Name"
          subtitle="Item titles layout"
          config={format.itemName}
          onChange={(cfg) => setFormat({ ...format, itemName: cfg })}
        />

        {/* 14. QTY Column */}
        <div className="bg-[#161514] border border-[#FFB300]/20 rounded-xl p-3 flex items-center justify-between">
          <div>
            <p className="font-bold text-sm text-[#F9F6F0]">14. QTY Column</p>
            <p className="text-xs text-[#B8B0A6]">Quantity column in items table</p>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() =>
                setFormat({
                  ...format,
                  colQty: { ...format.colQty, bold: !format.colQty.bold },
                })
              }
              className={`px-2 py-1 rounded text-xs font-bold ${
                format.colQty.bold
                  ? 'bg-[#FFB300] text-[#140D00]'
                  : 'bg-[#22201D] text-[#B8B0A6]'
              }`}
            >
              Bold
            </button>
            <SwitchToggle
              checked={format.colQty.visible}
              onChange={(checked) =>
                setFormat({
                  ...format,
                  colQty: { ...format.colQty, visible: checked },
                })
              }
            />
          </div>
        </div>

        {/* 15. RATE Column */}
        <div className="bg-[#161514] border border-[#FFB300]/20 rounded-xl p-3 flex items-center justify-between">
          <div>
            <p className="font-bold text-sm text-[#F9F6F0]">15. RATE Column</p>
            <p className="text-xs text-[#B8B0A6]">Item unit price in items table</p>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() =>
                setFormat({
                  ...format,
                  colRate: { ...format.colRate, bold: !format.colRate.bold },
                })
              }
              className={`px-2 py-1 rounded text-xs font-bold ${
                format.colRate.bold
                  ? 'bg-[#FFB300] text-[#140D00]'
                  : 'bg-[#22201D] text-[#B8B0A6]'
              }`}
            >
              Bold
            </button>
            <SwitchToggle
              checked={format.colRate.visible}
              onChange={(checked) =>
                setFormat({
                  ...format,
                  colRate: { ...format.colRate, visible: checked },
                })
              }
            />
          </div>
        </div>

        {/* 16. TOTAL Column */}
        <div className="bg-[#161514] border border-[#FFB300]/20 rounded-xl p-3 flex items-center justify-between">
          <div>
            <p className="font-bold text-sm text-[#F9F6F0]">16. TOTAL Column</p>
            <p className="text-xs text-[#B8B0A6]">Item total amount in items table</p>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() =>
                setFormat({
                  ...format,
                  colTotal: { ...format.colTotal, bold: !format.colTotal.bold },
                })
              }
              className={`px-2 py-1 rounded text-xs font-bold ${
                format.colTotal.bold
                  ? 'bg-[#FFB300] text-[#140D00]'
                  : 'bg-[#22201D] text-[#B8B0A6]'
              }`}
            >
              Bold
            </button>
            <SwitchToggle
              checked={format.colTotal.visible}
              onChange={(checked) =>
                setFormat({
                  ...format,
                  colTotal: { ...format.colTotal, visible: checked },
                })
              }
            />
          </div>
        </div>

        {/* 17. Grand Total */}
        <ElementConfigCard
          title="17. Grand Total"
          subtitle="Prominent bill total (Rs.)"
          config={format.grandTotal}
          onChange={(cfg) => setFormat({ ...format, grandTotal: cfg })}
        />

        {/* 18. Separator Lines */}
        <div className="bg-[#161514] border border-[#FFB300]/20 rounded-xl p-3.5 space-y-2">
          <div className="flex items-center justify-between">
            <div>
              <p className="font-bold text-sm text-[#F9F6F0]">18. Separator Lines</p>
              <p className="text-xs text-[#B8B0A6]">Horizontal dividers between sections</p>
            </div>
            <SwitchToggle
              checked={format.separatorLinesVisible}
              onChange={(checked) => setFormat({ ...format, separatorLinesVisible: checked })}
            />
          </div>

          {format.separatorLinesVisible && (
            <div className="flex items-center justify-between pt-2 border-t border-white/5 text-xs">
              <span className="text-[#B8B0A6]">Line Style:</span>
              <div className="flex items-center gap-1.5">
                {['-', '=', '*'].map((ch) => (
                  <button
                    key={ch}
                    onClick={() => setFormat({ ...format, separatorChar: ch })}
                    className={`px-2.5 py-1 rounded font-mono font-bold text-xs ${
                      format.separatorChar === ch
                        ? 'bg-[#FFB300] text-[#140D00]'
                        : 'bg-[#22201D] text-[#B8B0A6]'
                    }`}
                  >
                    {ch} {ch} {ch}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* 19. Payment Mode */}
        <ElementConfigCard
          title="19. Payment Mode"
          subtitle="CASH, UPI, or CARD label with items count"
          config={format.paymentMode}
          onChange={(cfg) => setFormat({ ...format, paymentMode: cfg })}
        />

        {/* 20. Footer / Thank-you message */}
        <ElementConfigCard
          title="20. Footer Message"
          subtitle={settings.receiptFooter}
          config={format.footerMessage}
          onChange={(cfg) => setFormat({ ...format, footerMessage: cfg })}
        />

        {/* Auto Cut Paper */}
        <div className="bg-[#161514] border border-[#FFB300]/20 rounded-xl p-3 flex items-center justify-between">
          <div>
            <p className="font-bold text-sm text-[#F9F6F0]">
              Auto Cut: {format.autoCut ? 'ON' : 'OFF'}
            </p>
            <p className="text-xs text-[#B8B0A6]">
              Send ESC/POS cut command to thermal printer after printing
            </p>
          </div>
          <SwitchToggle
            checked={format.autoCut}
            onChange={(checked) => setFormat({ ...format, autoCut: checked })}
          />
        </div>

        {/* Save button at bottom */}
        <button
          onClick={handleSave}
          className="w-full py-3 rounded-xl bg-gradient-to-r from-[#FFB300] to-[#FF6D00] text-[#140D00] font-bold text-sm hover:brightness-110 active:scale-95 transition-all shadow-md flex items-center justify-center gap-2"
        >
          <Check className="w-5 h-5 font-bold" />
          <span>Save All Receipt Format Settings</span>
        </button>
      </div>

      {/* Live Interactive Receipt Preview (Right Sticky) */}
      <div className="w-full xl:w-[400px] shrink-0 sticky top-16 bg-[#161514] border border-[#FFB300]/30 rounded-2xl p-4 shadow-xl flex flex-col items-center">
        <h4 className="font-bold text-sm text-[#FFB300] mb-3 self-start">
          Live Thermal Preview ({paperWidth === 'WIDTH_80MM' ? '80mm' : '58mm'})
        </h4>
        <div
          className="w-full flex justify-center overflow-x-auto"
          dangerouslySetInnerHTML={{ __html: previewHtml }}
        />
      </div>
    </div>
  );
};

// Reusable Element Config Card
interface ElementConfigCardProps {
  title: string;
  subtitle: string;
  config: ElementConfig;
  onChange: (cfg: ElementConfig) => void;
}

const ElementConfigCard: React.FC<ElementConfigCardProps> = ({
  title,
  subtitle,
  config,
  onChange,
}) => {
  return (
    <div className="bg-[#161514] border border-[#FFB300]/20 rounded-xl p-3.5 space-y-2.5">
      <div className="flex items-center justify-between">
        <div>
          <p className="font-bold text-sm text-[#F9F6F0]">{title}</p>
          <p className="text-xs text-[#B8B0A6]">{subtitle}</p>
        </div>
        <SwitchToggle
          checked={config.visible}
          onChange={(checked) => onChange({ ...config, visible: checked })}
        />
      </div>

      {config.visible && (
        <div className="pt-2 border-t border-white/5 flex flex-wrap items-center gap-3 text-xs">
          {/* Alignment */}
          <div>
            <span className="text-[11px] text-[#B8B0A6] block mb-1">Alignment:</span>
            <div className="flex items-center gap-1">
              {(['LEFT', 'CENTER', 'RIGHT'] as ReceiptAlignment[]).map((align) => (
                <button
                  key={align}
                  onClick={() => onChange({ ...config, alignment: align })}
                  className={`px-2 py-1 rounded text-xs font-semibold ${
                    config.alignment === align
                      ? 'bg-[#FFB300] text-[#140D00] font-bold'
                      : 'bg-[#22201D] text-[#B8B0A6]'
                  }`}
                >
                  {align}
                </button>
              ))}
            </div>
          </div>

          {/* Font Size */}
          <div>
            <span className="text-[11px] text-[#B8B0A6] block mb-1">Font Size:</span>
            <div className="flex items-center gap-1">
              {(
                [
                  { id: 'NORMAL', label: 'Normal' },
                  { id: 'DOUBLE_HEIGHT', label: 'Tall' },
                  { id: 'DOUBLE_WIDTH', label: 'Wide' },
                  { id: 'DOUBLE_BOTH', label: 'Large' },
                ] as { id: ReceiptFontSize; label: string }[]
              ).map((fs) => (
                <button
                  key={fs.id}
                  onClick={() => onChange({ ...config, fontSize: fs.id })}
                  className={`px-2 py-1 rounded text-xs font-semibold ${
                    config.fontSize === fs.id
                      ? 'bg-[#FFB300] text-[#140D00] font-bold'
                      : 'bg-[#22201D] text-[#B8B0A6]'
                  }`}
                >
                  {fs.label}
                </button>
              ))}
            </div>
          </div>

          {/* Bold */}
          <div>
            <span className="text-[11px] text-[#B8B0A6] block mb-1">Style:</span>
            <button
              onClick={() => onChange({ ...config, bold: !config.bold })}
              className={`px-2.5 py-1 rounded text-xs font-bold ${
                config.bold
                  ? 'bg-[#FFB300] text-[#140D00]'
                  : 'bg-[#22201D] text-[#B8B0A6]'
              }`}
            >
              Bold
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

// -----------------------------------------------------------------------------
// 3. PRINTER SETTINGS
// -----------------------------------------------------------------------------
interface PrinterSettingsProps {
  settings: any;
  printerState: any;
  onUpdateAutoPrint: (
    autoBill: boolean,
    autoToken: boolean,
    autoBoth: boolean,
    autoReconnect: boolean
  ) => void;
  onToggleAutoCut: (enabled: boolean) => void;
  onConnectPrinter: (address: string, name: string) => void;
  onDisconnectPrinter: () => void;
  onForgetPrinter: () => void;
  onTestPrint: () => void;
}

const PrinterSettings: React.FC<PrinterSettingsProps> = ({
  settings,
  printerState,
  onUpdateAutoPrint,
  onToggleAutoCut,
  onConnectPrinter,
  onDisconnectPrinter,
  onForgetPrinter,
  onTestPrint,
}) => {
  const [autoBill, setAutoBill] = useState(settings.autoPrintBill);
  const [autoToken, setAutoToken] = useState(settings.autoPrintToken);
  const [autoBoth, setAutoBoth] = useState(settings.autoPrintBoth);
  const [autoReconnect, setAutoReconnect] = useState(settings.autoReconnectPrinter);

  const isConnected = printerState.status === 'connected';

  const samplePairedDevices = [
    { name: 'POS-8380', address: '66:22:BB:77:88:99', recommended: true },
    { name: 'MPT-II 58mm', address: '00:11:22:33:44:55', recommended: false },
    { name: 'RPP02N Bluetooth', address: 'AA:BB:CC:DD:EE:FF', recommended: false },
  ];

  return (
    <div className="space-y-4 max-w-2xl">
      {/* Printer Status Card */}
      <div className="bg-[#161514] border border-[#FFB300]/25 rounded-xl p-4 shadow-sm space-y-3">
        <h3 className="font-bold text-sm sm:text-base text-[#F9F6F0]">
          POS-8380 Bluetooth Classic SPP Status
        </h3>

        <div className="flex items-center gap-3">
          {isConnected ? (
            <BluetoothConnected className="w-6 h-6 text-emerald-400 shrink-0" />
          ) : (
            <BluetoothOff className="w-6 h-6 text-red-400 shrink-0" />
          )}

          <div>
            <p className="font-bold text-sm text-[#F9F6F0]">
              {isConnected
                ? `CONNECTED to ${printerState.deviceName || 'POS-8380'}`
                : 'Disconnected'}
            </p>
            <p className="text-xs text-[#B8B0A6]">
              {isConnected
                ? `MAC: ${printerState.address || settings.savedPrinterMac} • RFCOMM Socket Active`
                : settings.savedPrinterMac
                ? `Saved: ${settings.savedPrinterName} (${settings.savedPrinterMac})`
                : 'No printer saved yet. Select POS-8380 from paired devices below.'}
            </p>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2 pt-2">
          {isConnected ? (
            <>
              <button
                onClick={onTestPrint}
                className="px-3.5 py-1.5 rounded-lg bg-[#FFB300] text-[#140D00] font-bold text-xs hover:brightness-110 active:scale-95 transition-all"
              >
                Test Print
              </button>
              <button
                onClick={onDisconnectPrinter}
                className="px-3.5 py-1.5 rounded-lg border border-[#FFB300]/30 text-xs font-semibold text-[#B8B0A6] hover:bg-[#22201D]"
              >
                Disconnect
              </button>
            </>
          ) : settings.savedPrinterMac ? (
            <button
              onClick={() => onConnectPrinter(settings.savedPrinterMac, settings.savedPrinterName || 'POS-8380')}
              className="px-3.5 py-1.5 rounded-lg bg-[#FFB300] text-[#140D00] font-bold text-xs hover:brightness-110 active:scale-95 transition-all"
            >
              Connect POS-8380
            </button>
          ) : null}

          {settings.savedPrinterMac && (
            <button
              onClick={onForgetPrinter}
              className="px-3 py-1.5 rounded-lg border border-red-500/40 text-xs font-semibold text-red-400 hover:bg-red-950/20"
            >
              Forget Printer
            </button>
          )}
        </div>
      </div>

      {/* Printing Automation Preferences */}
      <div className="bg-[#161514] border border-[#FFB300]/25 rounded-xl p-4 shadow-sm space-y-3">
        <h3 className="font-bold text-sm sm:text-base text-[#F9F6F0]">Printing Automation</h3>

        <div className="flex items-center justify-between">
          <div>
            <p className="font-semibold text-xs sm:text-sm text-[#F9F6F0]">Auto-Reconnect to POS-8380</p>
            <p className="text-[11px] text-[#B8B0A6]">Reconnects automatically on app launch</p>
          </div>
          <SwitchToggle
            checked={autoReconnect}
            onChange={(checked) => {
              setAutoReconnect(checked);
              onUpdateAutoPrint(autoBill, autoToken, autoBoth, checked);
            }}
          />
        </div>

        <div className="border-t border-white/5 pt-2 flex items-center justify-between">
          <p className="text-xs sm:text-sm text-[#F9F6F0]">Auto-Print Customer Bill on Save</p>
          <SwitchToggle
            checked={autoBill}
            onChange={(checked) => {
              setAutoBill(checked);
              onUpdateAutoPrint(checked, autoToken, autoBoth, autoReconnect);
            }}
          />
        </div>

        <div className="border-t border-white/5 pt-2 flex items-center justify-between">
          <p className="text-xs sm:text-sm text-[#F9F6F0]">Auto-Print Kitchen Token (KOT) on Save</p>
          <SwitchToggle
            checked={autoToken}
            onChange={(checked) => {
              setAutoToken(checked);
              onUpdateAutoPrint(autoBill, checked, autoBoth, autoReconnect);
            }}
          />
        </div>

        <div className="border-t border-white/5 pt-2 flex items-center justify-between">
          <p className="text-xs sm:text-sm text-[#F9F6F0]">Auto-Print Both Bill + Token on Save</p>
          <SwitchToggle
            checked={autoBoth}
            onChange={(checked) => {
              setAutoBoth(checked);
              onUpdateAutoPrint(autoBill, autoToken, checked, autoReconnect);
            }}
          />
        </div>

        <div className="border-t border-white/5 pt-2 flex items-center justify-between">
          <div>
            <p className="font-semibold text-xs sm:text-sm text-[#F9F6F0]">
              Auto Cut: {settings.autoCutPaper ? 'ON' : 'OFF'}
            </p>
            <p className="text-[11px] text-[#B8B0A6]">Send ESC/POS cut command to POS-8380 printer after printing</p>
          </div>
          <SwitchToggle
            checked={settings.autoCutPaper}
            onChange={onToggleAutoCut}
          />
        </div>
      </div>

      {/* Paired Bluetooth Devices List */}
      <div className="bg-[#161514] border border-[#FFB300]/25 rounded-xl p-4 shadow-sm space-y-3">
        <h3 className="font-bold text-sm sm:text-base text-[#F9F6F0]">Paired Bluetooth Printers</h3>

        <div className="space-y-2">
          {samplePairedDevices.map((dev) => (
            <div
              key={dev.address}
              className={`flex items-center justify-between p-3 rounded-lg border text-xs ${
                dev.recommended
                  ? 'bg-[#1E1A14] border-[#FFB300]'
                  : 'bg-[#22201D] border-[#FFB300]/20'
              }`}
            >
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-bold text-[#F9F6F0]">{dev.name}</span>
                  {dev.recommended && (
                    <span className="px-1.5 py-0.5 rounded bg-[#FFB300] text-[#140D00] text-[9px] font-bold">
                      RECOMMENDED
                    </span>
                  )}
                </div>
                <span className="text-[#B8B0A6] text-[11px]">MAC: {dev.address}</span>
              </div>

              {isConnected && settings.savedPrinterMac === dev.address ? (
                <span className="font-bold text-emerald-400">CONNECTED</span>
              ) : (
                <button
                  onClick={() => onConnectPrinter(dev.address, dev.name)}
                  className="px-3 py-1.5 rounded-lg bg-[#FFB300] text-[#140D00] font-bold text-xs hover:brightness-110 active:scale-95 transition-all"
                >
                  Select & Connect
                </button>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

// -----------------------------------------------------------------------------
// 4. BILLING SETTINGS
// -----------------------------------------------------------------------------
interface BillingSettingsProps {
  settings: any;
  onUpdateBillNumber: (num: number) => void;
  onUpdateTokenNumber: (num: number) => void;
}

const BillingSettings: React.FC<BillingSettingsProps> = ({
  settings,
  onUpdateBillNumber,
  onUpdateTokenNumber,
}) => {
  const [billNum, setBillNum] = useState(settings.nextBillNumber.toString());
  const [tokenNum, setTokenNum] = useState(settings.nextTokenNumber.toString());

  return (
    <div className="space-y-4 max-w-2xl">
      <div className="bg-[#161514] border border-[#FFB300]/25 rounded-xl p-4 shadow-sm space-y-3">
        <h3 className="font-bold text-sm sm:text-base text-[#F9F6F0]">
          Bill & Token Numbering Control
        </h3>
        <p className="text-xs text-[#B8B0A6]">
          Bill numbers and token numbers increment automatically and are persisted across app/device restarts.
        </p>

        {/* Bill Number */}
        <div className="flex items-end gap-2 pt-2">
          <div className="flex-1">
            <label className="text-xs text-[#B8B0A6] font-medium block mb-1">
              Next Bill Number
            </label>
            <input
              type="number"
              value={billNum}
              onChange={(e) => setBillNum(e.target.value)}
              className="w-full bg-[#22201D] border border-[#FFB300]/30 rounded-lg px-3 py-2 text-xs sm:text-sm text-[#F9F6F0] focus:outline-none focus:border-[#FFB300]"
            />
          </div>
          <button
            onClick={() => {
              const n = Number(billNum);
              if (n > 0) onUpdateBillNumber(n);
            }}
            className="px-4 py-2 rounded-lg bg-[#FFB300] text-[#140D00] font-bold text-xs hover:brightness-110 active:scale-95"
          >
            Update
          </button>
        </div>

        {/* Token Number */}
        <div className="flex items-end gap-2 pt-2">
          <div className="flex-1">
            <label className="text-xs text-[#B8B0A6] font-medium block mb-1">
              Next Token Number
            </label>
            <input
              type="number"
              value={tokenNum}
              onChange={(e) => setTokenNum(e.target.value)}
              className="w-full bg-[#22201D] border border-[#FFB300]/30 rounded-lg px-3 py-2 text-xs sm:text-sm text-[#F9F6F0] focus:outline-none focus:border-[#FFB300]"
            />
          </div>
          <button
            onClick={() => {
              const n = Number(tokenNum);
              if (n > 0) onUpdateTokenNumber(n);
            }}
            className="px-4 py-2 rounded-lg bg-[#FFB300] text-[#140D00] font-bold text-xs hover:brightness-110 active:scale-95"
          >
            Update
          </button>
          <button
            onClick={() => {
              setTokenNum('1');
              onUpdateTokenNumber(1);
            }}
            className="px-3 py-2 rounded-lg border border-[#FFB300]/30 text-[#FFB300] font-semibold text-xs hover:bg-[#22201D]"
          >
            Reset to 1
          </button>
        </div>
      </div>
    </div>
  );
};

// -----------------------------------------------------------------------------
// 5. SALES SYNC SETTINGS
// -----------------------------------------------------------------------------
interface SalesSettingsProps {
  syncState: any;
  onTriggerSync: () => void;
}

const SalesSettings: React.FC<SalesSettingsProps> = ({
  syncState,
  onTriggerSync,
}) => {
  return (
    <div className="space-y-4 max-w-2xl">
      <div className="bg-[#161514] border border-[#FFB300]/25 rounded-xl p-4 shadow-sm space-y-3">
        <h3 className="font-bold text-sm sm:text-base text-[#F9F6F0]">
          Cloud Sales Synchronization (Firestore)
        </h3>
        <p className="text-xs text-[#B8B0A6] leading-relaxed">
          Sales records are always saved in the local offline database first for instant billing. When internet network is active, sales synchronize to Firebase Firestore so all business devices stay in sync without requiring login.
        </p>

        <div className="pt-2 flex items-center justify-between border-t border-white/5">
          <span className="text-xs font-semibold text-[#F9F6F0]">Sync Action</span>
          <button
            onClick={onTriggerSync}
            disabled={syncState === 'syncing'}
            className="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-[#FFB300] text-[#140D00] font-bold text-xs hover:brightness-110 active:scale-95 transition-all shadow-sm"
          >
            <Cloud className="w-4 h-4 font-bold" />
            <span>Synchronize Sales Now</span>
          </button>
        </div>

        <p className="text-[11px] text-[#6E6760]">
          Collection: sales • Prevents duplicate entries using persistent unique Bill UUIDs
        </p>
      </div>
    </div>
  );
};

// Switch Toggle Helper
const SwitchToggle: React.FC<{ checked: boolean; onChange: (checked: boolean) => void }> = ({
  checked,
  onChange,
}) => {
  return (
    <label className="relative inline-flex items-center cursor-pointer shrink-0">
      <input
        type="checkbox"
        checked={checked}
        onChange={(e) => onChange(e.target.checked)}
        className="sr-only peer"
      />
      <div className="w-9 h-5 bg-[#22201D] border border-[#FFB300]/30 rounded-full peer peer-checked:bg-[#FFB300] peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-[#140D00] after:rounded-full after:h-4 after:w-4 after:transition-all" />
    </label>
  );
};
