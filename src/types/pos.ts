export type OrderType = 'DINE_IN' | 'PARCEL';

export type PaymentMode = 'CASH' | 'UPI' | 'CARD';

export type PrinterPaperWidth = 'WIDTH_58MM' | 'WIDTH_80MM';

export type ItemFontSize = 'NORMAL' | 'LARGE';

export type ReceiptAlignment = 'LEFT' | 'CENTER' | 'RIGHT';

export type ReceiptFontSize = 'NORMAL' | 'DOUBLE_HEIGHT' | 'DOUBLE_WIDTH' | 'DOUBLE_BOTH';

export interface ElementConfig {
  visible: boolean;
  bold: boolean;
  fontSize: ReceiptFontSize;
  alignment: ReceiptAlignment;
}

export interface ItemColumnConfig {
  visible: boolean;
  bold: boolean;
}

export interface ReceiptFormatConfig {
  // 1. Logo
  logoVisible: boolean;
  // 2. Shop Name
  shopName: ElementConfig;
  // 3. Address
  address: ElementConfig;
  // 4. Phone
  phone: ElementConfig;
  // 5. Bill Number
  billNumber: ElementConfig;
  // 6. Token Number
  tokenNumber: ElementConfig;
  // 7. Date
  date: ElementConfig;
  // 8. Time
  time: ElementConfig;
  // 9. Order Type (DINE IN / PARCEL)
  orderType: ElementConfig;
  // 10. Table Number
  tableNumber: ElementConfig;
  // 11. Customer Name
  customerName: ElementConfig;
  // 12. ITEM heading row
  itemHeading: ElementConfig;
  // 13. Item Name
  itemName: ElementConfig;
  // 14. QTY column
  colQty: ItemColumnConfig;
  // 15. RATE column
  colRate: ItemColumnConfig;
  // 16. TOTAL column
  colTotal: ItemColumnConfig;
  // 17. Grand Total
  grandTotal: ElementConfig;
  // 18. Separator Lines
  separatorLinesVisible: boolean;
  separatorChar: string;
  // 19. Payment Mode
  paymentMode: ElementConfig;
  // 20. Footer / Thank-you message
  footerMessage: ElementConfig;
  // Auto Cut paper after receipt
  autoCut: boolean;
}

export interface DepartmentInfo {
  id: number;
  name: string;
  code: string;
  isActive: boolean;
}

export interface MenuItem {
  id: string | number;
  name: string;
  category: string;
  price: number;
  isAvailable: boolean;
  sortOrder?: number;
  plu?: number;
  isVegetarian?: boolean;
  deptId?: number;
}

export interface CartItem {
  menuItem: MenuItem;
  quantity: number;
  total: number;
}

export interface ShopSettings {
  shopName: string;
  address: string;
  phone: string;
  logoUrl?: string | null;
  paperWidth: PrinterPaperWidth;
  billItemFontSize: ItemFontSize;
  kotItemFontSize: ItemFontSize;
  autoPrintBill: boolean;
  autoPrintToken: boolean;
  autoPrintBoth: boolean;
  autoCutPaper: boolean;
  nextBillNumber: number;
  nextTokenNumber: number;
  savedPrinterMac: string;
  savedPrinterName: string;
  autoReconnectPrinter: boolean;
  receiptFooter: string;
  showCustomerName: boolean;
  showTableNumber: boolean;
  showLogo: boolean;
  receiptFormat: ReceiptFormatConfig;
}

export interface BillRecord {
  billId: string;
  billNumber: number;
  tokenNumber: number;
  timestamp: number;
  dateString: string;
  timeString: string;
  orderType: OrderType;
  tableNumber?: string | null;
  customerName?: string | null;
  paymentMode: PaymentMode;
  items: CartItem[];
  subtotal: number;
  grandTotal: number;
  itemCount: number;
  isSynced: boolean;
}

export interface SalesSummary {
  totalRevenue: number;
  billCount: number;
  cashTotal: number;
  upiTotal: number;
  cardTotal: number;
}

export type SalesTimeFilter = 'TODAY' | 'WEEKLY' | 'MONTHLY' | 'ALL';

export type SyncStatus = 'idle' | 'syncing' | 'synced' | 'offline' | 'failed';

export interface PrinterDevice {
  name: string;
  address: string;
  isBonded?: boolean;
}

export type PrinterStatus = 'disconnected' | 'connecting' | 'connected' | 'unavailable' | 'error';

export interface PrinterConnectionState {
  status: PrinterStatus;
  deviceName?: string;
  address?: string;
  message?: string;
}

export type PosTab = 'BILLING' | 'SALES' | 'MENU' | 'SETTINGS';

export type PrintChoice = 'BILL_ONLY' | 'TOKEN_ONLY' | 'BOTH' | 'NONE';
