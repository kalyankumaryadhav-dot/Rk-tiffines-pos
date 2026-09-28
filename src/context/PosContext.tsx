import React, { createContext, useContext, useState, useEffect, useMemo, ReactNode } from 'react';
import {
  BillRecord,
  CartItem,
  DepartmentInfo,
  MenuItem,
  OrderType,
  PaymentMode,
  PosTab,
  PrinterConnectionState,
  PrinterPaperWidth,
  ReceiptFormatConfig,
  SalesSummary,
  SalesTimeFilter,
  ShopSettings,
  SyncStatus,
  PrintChoice,
} from '../types/pos';
import {
  DEFAULT_DEPARTMENTS,
  DEFAULT_MENU_ITEMS,
  DEFAULT_SHOP_SETTINGS,
} from '../data/defaultMenu';

interface PosContextType {
  // Navigation
  currentTab: PosTab;
  setCurrentTab: (tab: PosTab) => void;

  // Billing state
  selectedCategory: string;
  selectCategory: (category: string) => void;
  cartItems: CartItem[];
  orderType: OrderType;
  setOrderType: (type: OrderType) => void;
  tableNumber: string;
  setTableNumber: (table: string) => void;
  customerName: string;
  setCustomerName: (name: string) => void;
  paymentMode: PaymentMode;
  setPaymentMode: (mode: PaymentMode) => void;
  addToCart: (item: MenuItem) => void;
  decrementCart: (item: MenuItem) => void;
  removeFromCart: (item: MenuItem) => void;
  clearCart: () => void;

  // Order processing
  processOrderAndPrint: (choice: PrintChoice) => void;
  lastCompletedBill: BillRecord | null;
  activeReceiptPreview: { type: 'bill' | 'token'; bill: BillRecord } | null;
  closeReceiptPreview: () => void;
  reprintBill: (bill: BillRecord) => void;
  reprintToken: (bill: BillRecord) => void;

  // Sales & Orders
  orders: BillRecord[];
  salesFilter: SalesTimeFilter;
  setSalesFilter: (filter: SalesTimeFilter) => void;
  filteredOrders: BillRecord[];
  salesSummary: SalesSummary;

  // Menu & Department management
  menuItems: MenuItem[];
  departments: DepartmentInfo[];
  addMenuItem: (name: string, category: string, price: number) => void;
  updateMenuItem: (item: MenuItem) => void;
  deleteMenuItem: (id: string | number) => void;
  toggleItemAvailability: (item: MenuItem) => void;
  resetToDefaultMenu: () => void;
  addDepartment: (name: string, code: string) => void;
  renameDepartment: (oldName: string, newName: string, newCode: string) => void;
  deleteDepartment: (dept: DepartmentInfo) => void;
  reorderDepartments: (newList: DepartmentInfo[]) => void;

  // Settings
  settings: ShopSettings;
  updateSettings: (newSettings: Partial<ShopSettings>) => void;
  updateReceiptFormat: (format: ReceiptFormatConfig) => void;
  updatePaperWidth: (width: PrinterPaperWidth) => void;
  setAutoCutPaper: (enabled: boolean) => void;
  resetNextBillNumber: (num: number) => void;
  resetNextTokenNumber: (num: number) => void;
  saveUploadedLogo: (dataUrl: string) => void;
  removeLogo: () => void;

  // Printer & Bluetooth State
  printerState: PrinterConnectionState;
  connectPrinter: (address: string, name: string) => void;
  disconnectPrinter: () => void;
  forgetPrinter: () => void;
  testPrint: () => void;

  // Sync state
  syncState: SyncStatus;
  triggerFirestoreSync: () => void;

  // Toast notifications
  toastMessage: string | null;
  showToast: (msg: string) => void;
}

const PosContext = createContext<PosContextType | undefined>(undefined);

const STORAGE_KEYS = {
  SETTINGS: 'rk_pos_settings_v1',
  MENU_ITEMS: 'rk_pos_menu_items_v1',
  DEPARTMENTS: 'rk_pos_departments_v1',
  ORDERS: 'rk_pos_orders_v1',
};

export const PosProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  // Navigation
  const [currentTab, setCurrentTab] = useState<PosTab>('BILLING');

  // Settings
  const [settings, setSettings] = useState<ShopSettings>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.SETTINGS);
      if (saved) return JSON.parse(saved);
    } catch (e) {
      console.error(e);
    }
    return DEFAULT_SHOP_SETTINGS;
  });

  // Departments
  const [departments, setDepartments] = useState<DepartmentInfo[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.DEPARTMENTS);
      if (saved) return JSON.parse(saved);
    } catch (e) {
      console.error(e);
    }
    return DEFAULT_DEPARTMENTS;
  });

  // Menu items
  const [menuItems, setMenuItems] = useState<MenuItem[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.MENU_ITEMS);
      if (saved) return JSON.parse(saved);
    } catch (e) {
      console.error(e);
    }
    return DEFAULT_MENU_ITEMS;
  });

  // Orders
  const [orders, setOrders] = useState<BillRecord[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.ORDERS);
      if (saved) return JSON.parse(saved);
    } catch (e) {
      console.error(e);
    }
    // Seed with a few initial sample orders for today
    const now = new Date();
    const dStr = `${String(now.getDate()).padStart(2, '0')}-${String(now.getMonth() + 1).padStart(2, '0')}-${now.getFullYear()}`;
    return [
      {
        billId: 'BILL-1000-SEED',
        billNumber: 1000,
        tokenNumber: 99,
        timestamp: Date.now() - 3600000,
        dateString: dStr,
        timeString: '08:30 AM',
        orderType: 'DINE_IN',
        tableNumber: '1',
        customerName: 'Kalyan',
        paymentMode: 'UPI',
        items: [
          { menuItem: DEFAULT_MENU_ITEMS[0], quantity: 2, total: 90 },
          { menuItem: DEFAULT_MENU_ITEMS[1], quantity: 1, total: 60 },
        ],
        subtotal: 150,
        grandTotal: 150,
        itemCount: 3,
        isSynced: true,
      },
    ];
  });

  // Billing state
  const [selectedCategory, setSelectedCategory] = useState<string>('SOUTH INDIAN');
  const [cartItems, setCartItems] = useState<CartItem[]>([]);
  const [orderType, setOrderType] = useState<OrderType>('DINE_IN');
  const [tableNumber, setTableNumber] = useState<string>('1');
  const [customerName, setCustomerName] = useState<string>('');
  const [paymentMode, setPaymentMode] = useState<PaymentMode>('CASH');

  // Order result preview
  const [lastCompletedBill, setLastCompletedBill] = useState<BillRecord | null>(null);
  const [activeReceiptPreview, setActiveReceiptPreview] = useState<{
    type: 'bill' | 'token';
    bill: BillRecord;
  } | null>(null);

  // Sales filter
  const [salesFilter, setSalesFilter] = useState<SalesTimeFilter>('TODAY');

  // Printer & Bluetooth state
  const [printerState, setPrinterState] = useState<PrinterConnectionState>({
    status: 'connected',
    deviceName: 'POS-8380 (Bluetooth Classic)',
    address: '66:22:BB:77:88:99',
  });

  // Sync state
  const [syncState, setSyncState] = useState<SyncStatus>('synced');

  // Toast
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage((prev) => (prev === msg ? null : prev));
    }, 3200);
  };

  // Sync to localStorage
  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.SETTINGS, JSON.stringify(settings));
  }, [settings]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.DEPARTMENTS, JSON.stringify(departments));
  }, [departments]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.MENU_ITEMS, JSON.stringify(menuItems));
  }, [menuItems]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.ORDERS, JSON.stringify(orders));
  }, [orders]);

  // Cart actions
  const addToCart = (item: MenuItem) => {
    setCartItems((prev) => {
      const idx = prev.findIndex((c) => c.menuItem.id === item.id);
      if (idx >= 0) {
        const updated = [...prev];
        updated[idx] = {
          ...updated[idx],
          quantity: updated[idx].quantity + 1,
          total: (updated[idx].quantity + 1) * updated[idx].menuItem.price,
        };
        return updated;
      }
      return [...prev, { menuItem: item, quantity: 1, total: item.price }];
    });
  };

  const decrementCart = (item: MenuItem) => {
    setCartItems((prev) => {
      const idx = prev.findIndex((c) => c.menuItem.id === item.id);
      if (idx >= 0) {
        const current = prev[idx];
        if (current.quantity > 1) {
          const updated = [...prev];
          updated[idx] = {
            ...current,
            quantity: current.quantity - 1,
            total: (current.quantity - 1) * current.menuItem.price,
          };
          return updated;
        } else {
          return prev.filter((c) => c.menuItem.id !== item.id);
        }
      }
      return prev;
    });
  };

  const removeFromCart = (item: MenuItem) => {
    setCartItems((prev) => prev.filter((c) => c.menuItem.id !== item.id));
  };

  const clearCart = () => {
    setCartItems([]);
    setCustomerName('');
  };

  // Process order
  const processOrderAndPrint = (choice: PrintChoice) => {
    if (cartItems.length === 0) {
      showToast('Cart is empty! Add items to bill.');
      return;
    }

    const now = new Date();
    const dStr = `${String(now.getDate()).padStart(2, '0')}-${String(now.getMonth() + 1).padStart(2, '0')}-${now.getFullYear()}`;
    const tStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true });

    const totalAmt = cartItems.reduce((acc, it) => acc + it.total, 0);
    const totalQty = cartItems.reduce((acc, it) => acc + it.quantity, 0);

    const newBill: BillRecord = {
      billId: `BILL-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
      billNumber: settings.nextBillNumber,
      tokenNumber: settings.nextTokenNumber,
      timestamp: now.getTime(),
      dateString: dStr,
      timeString: tStr,
      orderType,
      tableNumber: orderType === 'DINE_IN' ? tableNumber : null,
      customerName: customerName.trim() || null,
      paymentMode,
      items: [...cartItems],
      subtotal: totalAmt,
      grandTotal: totalAmt,
      itemCount: totalQty,
      isSynced: true,
    };

    // Increment bill & token counters
    const nextBillNo = settings.nextBillNumber + 1;
    const nextTokenNo = settings.nextTokenNumber + 1;

    setSettings((prev) => ({
      ...prev,
      nextBillNumber: nextBillNo,
      nextTokenNumber: nextTokenNo,
    }));

    setOrders((prev) => [newBill, ...prev]);
    setLastCompletedBill(newBill);
    clearCart();

    // Determine preview modal
    if (choice === 'TOKEN_ONLY') {
      setActiveReceiptPreview({ type: 'token', bill: newBill });
      showToast(`Token #${newBill.tokenNumber} sent to POS-8380 printer!`);
    } else {
      setActiveReceiptPreview({ type: 'bill', bill: newBill });
      showToast(`Bill #${newBill.billNumber} (Token #${newBill.tokenNumber}) processed successfully!`);
    }
  };

  const closeReceiptPreview = () => {
    setActiveReceiptPreview(null);
  };

  const reprintBill = (bill: BillRecord) => {
    setActiveReceiptPreview({ type: 'bill', bill });
    showToast(`Reprinting Bill #${bill.billNumber}`);
  };

  const reprintToken = (bill: BillRecord) => {
    setActiveReceiptPreview({ type: 'token', bill });
    showToast(`Reprinting Token #${bill.tokenNumber}`);
  };

  // Filtered orders & sales summary
  const filteredOrders = useMemo(() => {
    const now = new Date();
    const todayStr = `${String(now.getDate()).padStart(2, '0')}-${String(now.getMonth() + 1).padStart(2, '0')}-${now.getFullYear()}`;

    switch (salesFilter) {
      case 'TODAY':
        return orders.filter((o) => o.dateString === todayStr);
      case 'WEEKLY': {
        const sevenDaysAgo = Date.now() - 7 * 24 * 60 * 60 * 1000;
        return orders.filter((o) => o.timestamp >= sevenDaysAgo);
      }
      case 'MONTHLY': {
        const thirtyDaysAgo = Date.now() - 30 * 24 * 60 * 60 * 1000;
        return orders.filter((o) => o.timestamp >= thirtyDaysAgo);
      }
      case 'ALL':
      default:
        return orders;
    }
  }, [orders, salesFilter]);

  const salesSummary = useMemo(() => {
    let totalRevenue = 0;
    let cashTotal = 0;
    let upiTotal = 0;
    let cardTotal = 0;

    for (const order of filteredOrders) {
      totalRevenue += order.grandTotal;
      if (order.paymentMode === 'CASH') cashTotal += order.grandTotal;
      else if (order.paymentMode === 'UPI') upiTotal += order.grandTotal;
      else if (order.paymentMode === 'CARD') cardTotal += order.grandTotal;
    }

    return {
      totalRevenue,
      billCount: filteredOrders.length,
      cashTotal,
      upiTotal,
      cardTotal,
    };
  }, [filteredOrders]);

  // Menu operations
  const addMenuItem = (name: string, category: string, price: number) => {
    const newItem: MenuItem = {
      id: `item-${Date.now()}`,
      name: name.trim().toUpperCase(),
      category: category.trim().toUpperCase(),
      price: Number(price),
      isAvailable: true,
      isVegetarian: true,
      sortOrder: menuItems.length + 1,
    };
    setMenuItems((prev) => [...prev, newItem]);
    showToast(`Added ${newItem.name} to ${category}`);
  };

  const updateMenuItem = (item: MenuItem) => {
    setMenuItems((prev) => prev.map((m) => (m.id === item.id ? item : m)));
    showToast(`Updated ${item.name}`);
  };

  const deleteMenuItem = (id: string | number) => {
    setMenuItems((prev) => prev.filter((m) => m.id !== id));
    showToast('Menu item removed');
  };

  const toggleItemAvailability = (item: MenuItem) => {
    setMenuItems((prev) =>
      prev.map((m) => (m.id === item.id ? { ...m, isAvailable: !m.isAvailable } : m))
    );
  };

  const resetToDefaultMenu = () => {
    setDepartments(DEFAULT_DEPARTMENTS);
    setMenuItems(DEFAULT_MENU_ITEMS);
    showToast('Menu reset to official 12 departments and 115 items');
  };

  const addDepartment = (name: string, code: string) => {
    const upperName = name.trim().toUpperCase();
    const upperCode = code.trim().toUpperCase() || upperName.slice(0, 2);
    const newDept: DepartmentInfo = {
      id: Date.now(),
      name: upperName,
      code: upperCode,
      isActive: true,
    };
    setDepartments((prev) => [...prev, newDept]);
    showToast(`Department "${upperName}" added`);
  };

  const renameDepartment = (oldName: string, newName: string, newCode: string) => {
    const upperNew = newName.trim().toUpperCase();
    const upperCode = newCode.trim().toUpperCase() || upperNew.slice(0, 2);

    setDepartments((prev) =>
      prev.map((d) => (d.name === oldName ? { ...d, name: upperNew, code: upperCode } : d))
    );

    // Also update all items mapped to oldName
    setMenuItems((prev) =>
      prev.map((m) => (m.category === oldName ? { ...m, category: upperNew } : m))
    );

    if (selectedCategory === oldName) {
      setSelectedCategory(upperNew);
    }
    showToast(`Department updated to "${upperNew}"`);
  };

  const deleteDepartment = (dept: DepartmentInfo) => {
    setDepartments((prev) => prev.filter((d) => d.id !== dept.id));
    if (selectedCategory === dept.name) {
      const remaining = departments.filter((d) => d.id !== dept.id);
      if (remaining.length > 0) {
        setSelectedCategory(remaining[0].name);
      }
    }
    showToast(`Department "${dept.name}" deleted`);
  };

  const reorderDepartments = (newList: DepartmentInfo[]) => {
    setDepartments(newList);
  };

  // Settings operations
  const updateSettings = (newSettings: Partial<ShopSettings>) => {
    setSettings((prev) => ({ ...prev, ...newSettings }));
    showToast('Settings saved successfully');
  };

  const updateReceiptFormat = (format: ReceiptFormatConfig) => {
    setSettings((prev) => ({ ...prev, receiptFormat: format }));
    showToast('Receipt format saved successfully!');
  };

  const updatePaperWidth = (paperWidth: PrinterPaperWidth) => {
    setSettings((prev) => ({ ...prev, paperWidth }));
    showToast(`Printer width set to ${paperWidth === 'WIDTH_80MM' ? '80mm' : '58mm'}`);
  };

  const setAutoCutPaper = (enabled: boolean) => {
    setSettings((prev) => ({
      ...prev,
      autoCutPaper: enabled,
      receiptFormat: { ...prev.receiptFormat, autoCut: enabled },
    }));
    showToast(enabled ? 'Auto Cut: ON (ESC/POS)' : 'Auto Cut: OFF');
  };

  const resetNextBillNumber = (num: number) => {
    setSettings((prev) => ({ ...prev, nextBillNumber: num }));
    showToast(`Next bill number set to #${num}`);
  };

  const resetNextTokenNumber = (num: number) => {
    setSettings((prev) => ({ ...prev, nextTokenNumber: num }));
    showToast(`Next token number set to #${num}`);
  };

  const saveUploadedLogo = (dataUrl: string) => {
    setSettings((prev) => ({ ...prev, logoUrl: dataUrl }));
    showToast('Logo uploaded and saved for thermal printing');
  };

  const removeLogo = () => {
    setSettings((prev) => ({ ...prev, logoUrl: null }));
    showToast('Logo removed');
  };

  // Printer functions
  const connectPrinter = (address: string, name: string) => {
    setPrinterState({ status: 'connecting' });
    setTimeout(() => {
      setPrinterState({
        status: 'connected',
        deviceName: name,
        address,
      });
      setSettings((prev) => ({
        ...prev,
        savedPrinterMac: address,
        savedPrinterName: name,
      }));
      showToast(`Connected to ${name} (${address})`);
    }, 600);
  };

  const disconnectPrinter = () => {
    setPrinterState({ status: 'disconnected' });
    showToast('Printer disconnected');
  };

  const forgetPrinter = () => {
    setPrinterState({ status: 'disconnected' });
    setSettings((prev) => ({
      ...prev,
      savedPrinterMac: '',
      savedPrinterName: '',
    }));
    showToast('Saved printer forgotten');
  };

  const testPrint = () => {
    const dummyBill: BillRecord = {
      billId: 'TEST-PRINT',
      billNumber: settings.nextBillNumber,
      tokenNumber: settings.nextTokenNumber,
      timestamp: Date.now(),
      dateString: new Date().toLocaleDateString('en-GB'),
      timeString: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      orderType: 'DINE_IN',
      tableNumber: 'T-03',
      customerName: 'Kalyan Kumar',
      paymentMode: 'CASH',
      items: [
        { menuItem: { id: 1, name: 'GHEE KARAM DOSA', category: 'DOSA', price: 65, isAvailable: true }, quantity: 2, total: 130 },
        { menuItem: { id: 2, name: 'IDLI (4 PCS)', category: 'IDLI', price: 45, isAvailable: true }, quantity: 1, total: 45 },
        { menuItem: { id: 3, name: 'FILTER COFFEE', category: 'BEVERAGES', price: 20, isAvailable: true }, quantity: 2, total: 40 },
        { menuItem: { id: 4, name: 'SPECIAL BUTTER MASALA DOSA', category: 'DOSA', price: 90, isAvailable: true }, quantity: 1, total: 90 },
      ],
      subtotal: 305,
      grandTotal: 305,
      itemCount: 6,
      isSynced: true,
    };
    setActiveReceiptPreview({ type: 'bill', bill: dummyBill });
    showToast('Test receipt generated for POS-8380 printer!');
  };

  const triggerFirestoreSync = () => {
    setSyncState('syncing');
    setTimeout(() => {
      setSyncState('synced');
      showToast(`Sales synced successfully (${orders.length} bills)`);
    }, 800);
  };

  return (
    <PosContext.Provider
      value={{
        currentTab,
        setCurrentTab,
        selectedCategory,
        selectCategory: setSelectedCategory,
        cartItems,
        orderType,
        setOrderType,
        tableNumber,
        setTableNumber,
        customerName,
        setCustomerName,
        paymentMode,
        setPaymentMode,
        addToCart,
        decrementCart,
        removeFromCart,
        clearCart,
        processOrderAndPrint,
        lastCompletedBill,
        activeReceiptPreview,
        closeReceiptPreview,
        reprintBill,
        reprintToken,
        orders,
        salesFilter,
        setSalesFilter,
        filteredOrders,
        salesSummary,
        menuItems,
        departments,
        addMenuItem,
        updateMenuItem,
        deleteMenuItem,
        toggleItemAvailability,
        resetToDefaultMenu,
        addDepartment,
        renameDepartment,
        deleteDepartment,
        reorderDepartments,
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
        toastMessage,
        showToast,
      }}
    >
      {children}
    </PosContext.Provider>
  );
};

export const usePos = (): PosContextType => {
  const context = useContext(PosContext);
  if (!context) {
    throw new Error('usePos must be used within a PosProvider');
  }
  return context;
};
