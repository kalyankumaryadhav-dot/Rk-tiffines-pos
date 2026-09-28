import React, { useState } from 'react';
import {
  Check,
  CreditCard,
  Minus,
  Plus,
  Printer,
  Receipt,
  ShoppingBag,
  Trash2,
  UtensilsCrossed,
  Package,
  X,
} from 'lucide-react';
import { usePos } from '../context/PosContext';
import { MenuItem, PaymentMode, OrderType } from '../types/pos';

export const BillingScreen: React.FC = () => {
  const {
    departments,
    menuItems,
    selectedCategory,
    selectCategory,
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
  } = usePos();

  const [mobileCartModalOpen, setMobileCartModalOpen] = useState(false);

  // Filter items by category
  const filteredItems = menuItems.filter(
    (item) => item.category === selectedCategory && item.isAvailable
  );

  const totalAmount = cartItems.reduce((acc, it) => acc + it.total, 0);
  const totalItemsCount = cartItems.reduce((acc, it) => acc + it.quantity, 0);
  const hasItems = cartItems.length > 0;

  return (
    <div className="flex-1 flex flex-col lg:flex-row h-[calc(100vh-53px)] overflow-hidden">
      {/* LEFT SECTION: Menu & Order Controls (60-65% on desktop) */}
      <div className="flex-1 flex flex-col h-full overflow-hidden p-2 sm:p-3 bg-[#0C0B0A]">
        {/* Order Type & Table Controls */}
        <div className="bg-[#161514] border border-[#FFB300]/25 rounded-xl p-2.5 mb-2.5 shadow-sm">
          <div className="flex flex-wrap items-center gap-2 justify-between">
            {/* Dine In vs Parcel toggle */}
            <div className="flex items-center gap-2 flex-1 min-w-[200px]">
              <button
                type="button"
                onClick={() => setOrderType('DINE_IN')}
                className={`flex-1 flex items-center justify-center gap-2 py-2 px-3 rounded-lg font-bold text-xs sm:text-sm transition-all active:scale-95 ${
                  orderType === 'DINE_IN'
                    ? 'bg-[#FFB300] text-[#140D00] shadow-md'
                    : 'bg-[#22201D] text-[#F9F6F0] border border-[#FFB300]/20 hover:bg-[#2B2824]'
                }`}
              >
                <UtensilsCrossed className="w-4 h-4" />
                <span>DINE IN</span>
              </button>

              <button
                type="button"
                onClick={() => setOrderType('PARCEL')}
                className={`flex-1 flex items-center justify-center gap-2 py-2 px-3 rounded-lg font-bold text-xs sm:text-sm transition-all active:scale-95 ${
                  orderType === 'PARCEL'
                    ? 'bg-[#FFB300] text-[#140D00] shadow-md'
                    : 'bg-[#22201D] text-[#F9F6F0] border border-[#FFB300]/20 hover:bg-[#2B2824]'
                }`}
              >
                <Package className="w-4 h-4" />
                <span>PARCEL</span>
              </button>
            </div>

            {/* Optional Table Number & Customer Name */}
            <div className="flex items-center gap-2">
              {orderType === 'DINE_IN' && (
                <div className="flex items-center gap-1.5 bg-[#22201D] border border-[#FFB300]/25 rounded-lg px-2.5 py-1">
                  <span className="text-xs text-[#B8B0A6] font-medium">Table:</span>
                  <input
                    type="text"
                    value={tableNumber}
                    onChange={(e) => setTableNumber(e.target.value)}
                    placeholder="1"
                    className="w-12 bg-transparent text-[#FFB300] font-bold text-sm text-center focus:outline-none"
                  />
                </div>
              )}

              <div className="flex items-center gap-1.5 bg-[#22201D] border border-[#FFB300]/25 rounded-lg px-2.5 py-1">
                <input
                  type="text"
                  value={customerName}
                  onChange={(e) => setCustomerName(e.target.value)}
                  placeholder="Customer (optional)"
                  className="w-28 sm:w-36 bg-transparent text-[#F9F6F0] text-xs focus:outline-none placeholder-[#6E6760]"
                />
              </div>
            </div>
          </div>
        </div>

        {/* Category Horizontal Chips Bar */}
        <div className="overflow-x-auto no-scrollbar pb-1 mb-2.5 flex items-center gap-2">
          {departments.map((dept) => {
            const isSelected = dept.name === selectedCategory;
            return (
              <button
                key={dept.id}
                onClick={() => selectCategory(dept.name)}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg shrink-0 text-xs font-semibold transition-all active:scale-95 ${
                  isSelected
                    ? 'bg-[#FFB300] text-[#140D00] shadow-sm font-bold'
                    : 'bg-[#161514] text-[#F9F6F0] border border-[#FFB300]/20 hover:bg-[#22201D]'
                }`}
              >
                <span
                  className={`text-[10px] font-bold px-1 rounded ${
                    isSelected ? 'bg-black/15 text-[#140D00]' : 'bg-[#22201D] text-[#FFB300]'
                  }`}
                >
                  {dept.code}
                </span>
                <span>{dept.name}</span>
              </button>
            );
          })}
        </div>

        {/* Menu Items Grid */}
        <div className="flex-1 overflow-y-auto pr-1">
          {filteredItems.length === 0 ? (
            <div className="h-full flex items-center justify-center text-center p-8 text-[#B8B0A6]">
              <div>
                <ShoppingBag className="w-12 h-12 text-[#6E6760] mx-auto mb-2" />
                <p className="text-sm">No items in this category.</p>
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-4 gap-2">
              {filteredItems.map((item) => {
                const cartEntry = cartItems.find((c) => c.menuItem.id === item.id);
                const inCartQty = cartEntry?.quantity || 0;
                const isSelected = inCartQty > 0;

                return (
                  <div
                    key={item.id}
                    onClick={() => {
                      if (!isSelected) addToCart(item);
                    }}
                    className={`flex flex-col justify-between p-2.5 rounded-xl border transition-all select-none ${
                      isSelected
                        ? 'bg-[#1E1A14] border-[#FFB300] shadow-md'
                        : 'bg-[#161514] border-[#FFB300]/20 hover:border-[#FFB300]/40'
                    }`}
                  >
                    {/* Item Title & Veg Indicator */}
                    <div className="flex items-start justify-between gap-1 mb-1">
                      <span className="text-xs sm:text-sm font-bold text-[#F9F6F0] leading-snug line-clamp-2">
                        {item.name}
                      </span>
                      {item.isVegetarian !== false ? (
                        <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 shrink-0 mt-0.5" title="Vegetarian" />
                      ) : (
                        <span className="w-2.5 h-2.5 rounded-full bg-red-500 shrink-0 mt-0.5" title="Non-Vegetarian" />
                      )}
                    </div>

                    {/* Price and Add/Qty Controls */}
                    <div className="flex items-center justify-between mt-2 pt-1 border-t border-white/5">
                      <span className="font-extrabold text-[#FFC107] text-sm sm:text-base">
                        ₹ {item.price.toFixed(0)}
                      </span>

                      {isSelected ? (
                        <div
                          className="flex items-center gap-1.5 bg-[#22201D] border border-[#FFB300]/30 rounded-lg p-0.5"
                          onClick={(e) => e.stopPropagation()}
                        >
                          <button
                            type="button"
                            onClick={() => decrementCart(item)}
                            className="w-6 h-6 rounded flex items-center justify-center bg-[#161514] text-[#F9F6F0] hover:bg-[#2B2824] active:scale-95"
                          >
                            <Minus className="w-3.5 h-3.5" />
                          </button>
                          <span className="font-bold text-xs sm:text-sm text-[#F9F6F0] px-1 min-w-[14px] text-center">
                            {inCartQty}
                          </span>
                          <button
                            type="button"
                            onClick={() => addToCart(item)}
                            className="w-6 h-6 rounded flex items-center justify-center bg-[#FFB300] text-[#140D00] hover:brightness-110 active:scale-95 font-bold"
                          >
                            <Plus className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      ) : (
                        <button
                          type="button"
                          onClick={() => addToCart(item)}
                          className="w-7 h-7 rounded-lg flex items-center justify-center bg-[#FFB300] text-[#140D00] hover:brightness-110 active:scale-95 shadow-sm"
                        >
                          <Plus className="w-4 h-4 font-bold" />
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Quick Cart Bottom Bar on Mobile/Tablet */}
        <div className="lg:hidden mt-2 bg-[#161514] border border-[#FFB300]/30 rounded-xl p-2.5 flex items-center justify-between gap-3 shadow-lg">
          <div
            onClick={() => setMobileCartModalOpen(true)}
            className="flex-1 cursor-pointer"
          >
            <p className="text-[11px] font-bold text-[#FFB300]">
              {totalItemsCount} Items • Tap to view bill
            </p>
            <p className="text-base sm:text-lg font-extrabold text-[#FFC107]">
              Total: ₹ {totalAmount.toFixed(0)}
            </p>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => processOrderAndPrint('BILL_ONLY')}
              disabled={!hasItems}
              className="px-3 py-2 text-xs font-bold rounded-lg bg-gradient-to-r from-[#FFC107] to-[#FF8F00] text-[#140D00] disabled:opacity-40 disabled:pointer-events-none transition-all active:scale-95"
            >
              BILL
            </button>
            <button
              onClick={() => processOrderAndPrint('BOTH')}
              disabled={!hasItems}
              className="px-3 py-2 text-xs font-bold rounded-lg bg-gradient-to-r from-[#FFB300] to-[#FF6D00] text-[#F9F6F0] disabled:opacity-40 disabled:pointer-events-none transition-all active:scale-95"
            >
              BILL+TOKEN
            </button>
          </div>
        </div>
      </div>

      {/* RIGHT SECTION: Cart Pane (Fixed on desktop, or modal on mobile) */}
      <div className="hidden lg:flex w-[380px] xl:w-[420px] flex-col h-full bg-[#161514] border-l border-[#FFB300]/20 p-3.5">
        <CartContent
          cartItems={cartItems}
          orderType={orderType}
          paymentMode={paymentMode}
          onPaymentModeChange={setPaymentMode}
          onIncrement={addToCart}
          onDecrement={decrementCart}
          onRemove={removeFromCart}
          onClearCart={clearCart}
          onPrintAction={processOrderAndPrint}
        />
      </div>

      {/* Mobile Cart Modal */}
      {mobileCartModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/80 backdrop-blur-sm lg:hidden">
          <div className="relative w-full max-w-lg bg-[#161514] border border-[#FFB300]/30 rounded-2xl shadow-2xl flex flex-col max-h-[90vh] overflow-hidden p-4">
            <div className="flex items-center justify-between pb-2 mb-2 border-b border-[#FFB300]/20">
              <h3 className="font-bold text-base text-[#F9F6F0]">Current Order Bill</h3>
              <button
                onClick={() => setMobileCartModalOpen(false)}
                className="p-1 rounded-lg text-[#B8B0A6] hover:text-[#F9F6F0] hover:bg-[#2B2824]"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="flex-1 overflow-hidden flex flex-col">
              <CartContent
                cartItems={cartItems}
                orderType={orderType}
                paymentMode={paymentMode}
                onPaymentModeChange={setPaymentMode}
                onIncrement={addToCart}
                onDecrement={decrementCart}
                onRemove={removeFromCart}
                onClearCart={clearCart}
                onPrintAction={(choice) => {
                  setMobileCartModalOpen(false);
                  processOrderAndPrint(choice);
                }}
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

interface CartContentProps {
  cartItems: any[];
  orderType: OrderType;
  paymentMode: PaymentMode;
  onPaymentModeChange: (mode: PaymentMode) => void;
  onIncrement: (item: MenuItem) => void;
  onDecrement: (item: MenuItem) => void;
  onRemove: (item: MenuItem) => void;
  onClearCart: () => void;
  onPrintAction: (choice: any) => void;
}

const CartContent: React.FC<CartContentProps> = ({
  cartItems,
  orderType,
  paymentMode,
  onPaymentModeChange,
  onIncrement,
  onDecrement,
  onRemove,
  onClearCart,
  onPrintAction,
}) => {
  const totalAmount = cartItems.reduce((acc, it) => acc + it.total, 0);
  const totalItemsCount = cartItems.reduce((acc, it) => acc + it.quantity, 0);
  const hasItems = cartItems.length > 0;

  return (
    <div className="flex-1 flex flex-col h-full overflow-hidden">
      {/* Header */}
      <div className="flex items-center justify-between pb-2 border-b border-[#FFB300]/20">
        <div>
          <h2 className="text-base font-bold text-[#F9F6F0]">Current Bill</h2>
          <span className="text-[11px] font-bold text-[#FFB300] tracking-wider uppercase">
            {orderType === 'DINE_IN' ? 'DINE IN' : 'PARCEL'}
          </span>
        </div>

        {hasItems && (
          <button
            onClick={onClearCart}
            className="flex items-center gap-1 text-xs font-semibold text-red-400 hover:text-red-300 transition-colors py-1 px-2 rounded hover:bg-red-950/20"
          >
            <Trash2 className="w-3.5 h-3.5" />
            <span>Clear</span>
          </button>
        )}
      </div>

      {/* Cart Items List */}
      <div className="flex-1 overflow-y-auto py-2 space-y-2 pr-1">
        {!hasItems ? (
          <div className="h-full flex flex-col items-center justify-center text-center p-6 text-[#B8B0A6]">
            <ShoppingBag className="w-12 h-12 text-[#6E6760] mb-2" />
            <p className="font-semibold text-sm">Cart is empty</p>
            <p className="text-xs text-[#6E6760] mt-1">Tap menu items to add to bill</p>
          </div>
        ) : (
          cartItems.map((item) => (
            <div
              key={item.menuItem.id}
              className="flex items-center justify-between p-2 rounded-lg bg-[#22201D] border border-[#FFB300]/20 text-xs gap-2"
            >
              <div className="flex-1 min-w-0">
                <p className="font-bold text-[#F9F6F0] truncate">{item.menuItem.name}</p>
                <p className="text-[11px] text-[#B8B0A6]">₹ {item.menuItem.price.toFixed(0)} each</p>
              </div>

              {/* Quantity Controls */}
              <div className="flex items-center gap-1.5 shrink-0">
                <button
                  type="button"
                  onClick={() => onDecrement(item.menuItem)}
                  className="w-5 h-5 rounded-full flex items-center justify-center bg-[#161514] border border-[#FFB300]/30 text-[#F9F6F0] hover:bg-[#2B2824]"
                >
                  <Minus className="w-3 h-3" />
                </button>
                <span className="font-bold text-xs text-[#F9F6F0] min-w-[16px] text-center">
                  {item.quantity}
                </span>
                <button
                  type="button"
                  onClick={() => onIncrement(item.menuItem)}
                  className="w-5 h-5 rounded-full flex items-center justify-center bg-[#FFB300] text-[#140D00] hover:brightness-110 font-bold"
                >
                  <Plus className="w-3 h-3" />
                </button>
              </div>

              {/* Line total & remove */}
              <div className="flex items-center gap-1.5 shrink-0">
                <span className="font-bold text-[#FFC107] text-xs min-w-[42px] text-right">
                  ₹ {item.total.toFixed(0)}
                </span>
                <button
                  type="button"
                  onClick={() => onRemove(item.menuItem)}
                  className="text-[#6E6760] hover:text-red-400 p-0.5"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Bill Total Summary */}
      <div className="pt-2 border-t border-[#FFB300]/20 space-y-2">
        <div className="flex items-center justify-between">
          <span className="text-xs text-[#B8B0A6]">Items: {totalItemsCount}</span>
          <div className="text-right">
            <span className="text-[10px] text-[#B8B0A6] uppercase font-bold tracking-wider block">
              GRAND TOTAL
            </span>
            <span className="text-2xl font-extrabold text-[#FFC107] tracking-tight">
              ₹ {totalAmount.toFixed(0)}
            </span>
          </div>
        </div>

        {/* Payment Mode Selector */}
        <div>
          <span className="text-[11px] font-bold text-[#B8B0A6] block mb-1">Payment Mode:</span>
          <div className="grid grid-cols-3 gap-1.5">
            <button
              type="button"
              onClick={() => onPaymentModeChange('CASH')}
              className={`py-1.5 px-2 rounded-lg text-xs font-bold transition-all ${
                paymentMode === 'CASH'
                  ? 'bg-emerald-700 text-white shadow-md'
                  : 'bg-[#22201D] text-[#B8B0A6] border border-[#FFB300]/20 hover:bg-[#2B2824]'
              }`}
            >
              CASH
            </button>
            <button
              type="button"
              onClick={() => onPaymentModeChange('UPI')}
              className={`py-1.5 px-2 rounded-lg text-xs font-bold transition-all ${
                paymentMode === 'UPI'
                  ? 'bg-blue-700 text-white shadow-md'
                  : 'bg-[#22201D] text-[#B8B0A6] border border-[#FFB300]/20 hover:bg-[#2B2824]'
              }`}
            >
              UPI
            </button>
            <button
              type="button"
              onClick={() => onPaymentModeChange('CARD')}
              className={`py-1.5 px-2 rounded-lg text-xs font-bold transition-all ${
                paymentMode === 'CARD'
                  ? 'bg-purple-800 text-white shadow-md'
                  : 'bg-[#22201D] text-[#B8B0A6] border border-[#FFB300]/20 hover:bg-[#2B2824]'
              }`}
            >
              CARD
            </button>
          </div>
        </div>

        {/* Print Action Buttons */}
        <div className="grid grid-cols-2 gap-2 pt-1">
          <button
            type="button"
            disabled={!hasItems}
            onClick={() => onPrintAction('BILL_ONLY')}
            className="flex items-center justify-center gap-1.5 py-2.5 px-3 rounded-lg font-bold text-xs bg-gradient-to-r from-[#FFC107] to-[#FF8F00] text-[#140D00] shadow-md hover:brightness-110 active:scale-95 disabled:opacity-40 disabled:pointer-events-none transition-all"
          >
            <Receipt className="w-4 h-4" />
            <span>PRINT BILL</span>
          </button>

          <button
            type="button"
            disabled={!hasItems}
            onClick={() => onPrintAction('TOKEN_ONLY')}
            className="flex items-center justify-center gap-1.5 py-2.5 px-3 rounded-lg font-bold text-xs bg-gradient-to-r from-[#FFB300] to-[#FF6D00] text-white shadow-md hover:brightness-110 active:scale-95 disabled:opacity-40 disabled:pointer-events-none transition-all"
          >
            <Printer className="w-4 h-4" />
            <span>KOT TOKEN</span>
          </button>
        </div>

        {/* Combined PRINT BOTH */}
        <button
          type="button"
          disabled={!hasItems}
          onClick={() => onPrintAction('BOTH')}
          className="w-full flex items-center justify-center gap-1.5 py-2.5 px-3 rounded-lg font-extrabold text-xs sm:text-sm bg-gradient-to-r from-[#FFB300] via-[#FF6D00] to-[#E64A19] text-white shadow-lg hover:brightness-110 active:scale-95 disabled:opacity-40 disabled:pointer-events-none transition-all"
        >
          <Receipt className="w-4 h-4" />
          <span>BILL + TOKEN (PRINT BOTH)</span>
        </button>
      </div>
    </div>
  );
};
