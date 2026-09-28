import { DepartmentInfo, MenuItem, ReceiptFormatConfig, ShopSettings } from '../types/pos';
import departmentsJson from '../../departments_and_items.json';

export const DEFAULT_DEPARTMENTS: DepartmentInfo[] = (departmentsJson.departments as DepartmentInfo[]) || [
  { id: 1, name: 'SOUTH INDIAN', code: 'SI', isActive: true },
  { id: 2, name: 'NORMAL DOSA', code: 'ND', isActive: true },
  { id: 3, name: 'PESARATTU', code: 'PS', isActive: true },
  { id: 4, name: 'GHEE IDLI', code: 'GI', isActive: true },
  { id: 5, name: 'GHEE DOSA', code: 'GD', isActive: true },
  { id: 6, name: 'BUTTER DOSA', code: 'BD', isActive: true },
  { id: 7, name: 'CHEESE DOSA', code: 'CD', isActive: true },
  { id: 8, name: 'PIZZA DOSA', code: 'PD', isActive: true },
  { id: 9, name: 'PANNER DOSA', code: 'PND', isActive: true },
  { id: 10, name: 'DOSA ITEMS', code: 'DI', isActive: true },
  { id: 11, name: 'MAGGI', code: 'MG', isActive: true },
  { id: 12, name: 'SNACKS', code: 'SN', isActive: true },
];

export const getDepartmentNameById = (id: number): string => {
  const dept = DEFAULT_DEPARTMENTS.find((d) => d.id === id);
  return dept ? dept.name : 'SOUTH INDIAN';
};

export const DEFAULT_MENU_ITEMS: MenuItem[] = (departmentsJson.menuItems || []).map((item: any) => ({
  id: item.id || `item-${item.plu}`,
  name: item.name,
  category: getDepartmentNameById(item.deptId),
  price: Number(item.price),
  isAvailable: item.isActive !== false,
  plu: item.plu,
  isVegetarian: item.isVegetarian !== false,
  deptId: item.deptId,
  sortOrder: item.plu || 0,
}));

export const DEFAULT_RECEIPT_FORMAT: ReceiptFormatConfig = {
  logoVisible: true,
  shopName: { visible: true, bold: true, fontSize: 'DOUBLE_BOTH', alignment: 'CENTER' },
  address: { visible: true, bold: false, fontSize: 'NORMAL', alignment: 'CENTER' },
  phone: { visible: true, bold: false, fontSize: 'NORMAL', alignment: 'CENTER' },
  billNumber: { visible: true, bold: true, fontSize: 'NORMAL', alignment: 'LEFT' },
  tokenNumber: { visible: true, bold: true, fontSize: 'NORMAL', alignment: 'RIGHT' },
  date: { visible: true, bold: false, fontSize: 'NORMAL', alignment: 'LEFT' },
  time: { visible: true, bold: false, fontSize: 'NORMAL', alignment: 'RIGHT' },
  orderType: { visible: true, bold: true, fontSize: 'NORMAL', alignment: 'LEFT' },
  tableNumber: { visible: true, bold: true, fontSize: 'NORMAL', alignment: 'LEFT' },
  customerName: { visible: true, bold: false, fontSize: 'NORMAL', alignment: 'LEFT' },
  itemHeading: { visible: true, bold: true, fontSize: 'NORMAL', alignment: 'LEFT' },
  itemName: { visible: true, bold: false, fontSize: 'NORMAL', alignment: 'LEFT' },
  colQty: { visible: true, bold: false },
  colRate: { visible: true, bold: false },
  colTotal: { visible: true, bold: false },
  grandTotal: { visible: true, bold: true, fontSize: 'DOUBLE_BOTH', alignment: 'RIGHT' },
  separatorLinesVisible: true,
  separatorChar: '-',
  paymentMode: { visible: true, bold: false, fontSize: 'NORMAL', alignment: 'LEFT' },
  footerMessage: { visible: true, bold: false, fontSize: 'NORMAL', alignment: 'CENTER' },
  autoCut: true,
};

export const DEFAULT_SHOP_SETTINGS: ShopSettings = {
  shopName: 'RK TIFFINES',
  address: 'Kadapa, Andhra Pradesh, India',
  phone: '9392509555',
  logoUrl: null,
  paperWidth: 'WIDTH_80MM',
  billItemFontSize: 'NORMAL',
  kotItemFontSize: 'LARGE',
  autoPrintBill: true,
  autoPrintToken: false,
  autoPrintBoth: false,
  autoCutPaper: true,
  nextBillNumber: 1001,
  nextTokenNumber: 1,
  savedPrinterMac: '66:22:BB:77:88:99',
  savedPrinterName: 'POS-8380',
  autoReconnectPrinter: true,
  receiptFooter: 'Thank you! Visit again',
  showCustomerName: true,
  showTableNumber: true,
  showLogo: true,
  receiptFormat: DEFAULT_RECEIPT_FORMAT,
};
