import React, { useState } from 'react';
import {
  BarChart2,
  Calendar,
  CloudCheck,
  CreditCard,
  Printer,
  QrCode,
  Receipt,
  RefreshCw,
  Search,
  Wallet,
  X,
} from 'lucide-react';
import { usePos } from '../context/PosContext';
import { BillRecord, SalesTimeFilter } from '../types/pos';

export const SalesScreen: React.FC = () => {
  const {
    salesFilter,
    setSalesFilter,
    filteredOrders,
    salesSummary,
    syncState,
    triggerFirestoreSync,
    reprintBill,
    reprintToken,
  } = usePos();

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedOrder, setSelectedOrder] = useState<BillRecord | null>(null);

  // Search filter
  const displayedOrders = filteredOrders.filter((order) => {
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return (
      order.billNumber.toString().includes(q) ||
      order.tokenNumber.toString().includes(q) ||
      (order.customerName && order.customerName.toLowerCase().includes(q)) ||
      (order.tableNumber && order.tableNumber.includes(q))
    );
  });

  const filterTabs: { id: SalesTimeFilter; label: string }[] = [
    { id: 'TODAY', label: 'TODAY' },
    { id: 'WEEKLY', label: 'WEEKLY' },
    { id: 'MONTHLY', label: 'MONTHLY' },
    { id: 'ALL', label: 'ALL' },
  ];

  return (
    <div className="flex-1 flex flex-col p-3 sm:p-4 bg-[#0C0B0A] overflow-y-auto max-w-6xl mx-auto w-full">
      {/* 1. Time Filter Tabs */}
      <div className="bg-[#161514] border border-[#FFB300]/25 rounded-xl p-1 flex items-center justify-between mb-3 shadow-sm">
        {filterTabs.map((tab) => {
          const isSelected = salesFilter === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => setSalesFilter(tab.id)}
              className={`flex-1 py-1.5 px-3 rounded-lg text-xs sm:text-sm font-bold transition-all ${
                isSelected
                  ? 'bg-[#FFB300] text-[#140D00] shadow-sm'
                  : 'text-[#B8B0A6] hover:text-[#F9F6F0] hover:bg-[#22201D]'
              }`}
            >
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* 2. Summary Metric Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-2.5 mb-3">
        {/* Total Revenue */}
        <div className="bg-[#161514] border border-[#FFB300]/40 rounded-xl p-3 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold text-[#FFB300] tracking-wider uppercase">
              TOTAL REVENUE
            </span>
            <BarChart2 className="w-4 h-4 text-[#FFB300]" />
          </div>
          <p className="text-xl sm:text-2xl font-extrabold text-[#FFC107] my-1">
            ₹ {salesSummary.totalRevenue.toFixed(0)}
          </p>
          <p className="text-xs text-[#B8B0A6]">{salesSummary.billCount} Bills</p>
        </div>

        {/* CASH */}
        <div className="bg-[#161514] border border-emerald-500/30 rounded-xl p-3 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold text-emerald-400 tracking-wider uppercase">
              CASH
            </span>
            <Wallet className="w-4 h-4 text-emerald-400" />
          </div>
          <p className="text-xl sm:text-2xl font-extrabold text-emerald-400 my-1">
            ₹ {salesSummary.cashTotal.toFixed(0)}
          </p>
          <p className="text-xs text-[#B8B0A6]">Total Cash</p>
        </div>

        {/* UPI */}
        <div className="bg-[#161514] border border-blue-500/30 rounded-xl p-3 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold text-blue-400 tracking-wider uppercase">
              UPI
            </span>
            <QrCode className="w-4 h-4 text-blue-400" />
          </div>
          <p className="text-xl sm:text-2xl font-extrabold text-blue-400 my-1">
            ₹ {salesSummary.upiTotal.toFixed(0)}
          </p>
          <p className="text-xs text-[#B8B0A6]">Total UPI</p>
        </div>

        {/* CARD */}
        <div className="bg-[#161514] border border-purple-500/30 rounded-xl p-3 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold text-purple-400 tracking-wider uppercase">
              CARD
            </span>
            <CreditCard className="w-4 h-4 text-purple-400" />
          </div>
          <p className="text-xl sm:text-2xl font-extrabold text-purple-400 my-1">
            ₹ {salesSummary.cardTotal.toFixed(0)}
          </p>
          <p className="text-xs text-[#B8B0A6]">Total Card</p>
        </div>
      </div>

      {/* 3. Cloud Synchronization Bar */}
      <div className="bg-[#161514] border border-[#FFB300]/25 rounded-xl px-3.5 py-2.5 flex items-center justify-between gap-3 mb-3 shadow-sm">
        <div className="flex items-center gap-2.5 min-w-0">
          {syncState === 'syncing' ? (
            <RefreshCw className="w-5 h-5 text-[#FFB300] animate-spin shrink-0" />
          ) : (
            <CloudCheck className="w-5 h-5 text-emerald-400 shrink-0" />
          )}
          <span className="text-xs sm:text-sm font-medium text-[#F9F6F0] truncate">
            {syncState === 'syncing'
              ? 'Synchronizing sales to Firestore…'
              : 'All sales are synced'}
          </span>
        </div>

        <button
          onClick={triggerFirestoreSync}
          disabled={syncState === 'syncing'}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-[#FFB300]/40 text-[#FFB300] hover:bg-[#FFB300]/10 active:scale-95 text-xs font-bold transition-all shrink-0"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${syncState === 'syncing' ? 'animate-spin' : ''}`} />
          <span>Sync</span>
        </button>
      </div>

      {/* 4. Past Bills Header & Search */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2.5 mb-2.5">
        <h3 className="font-bold text-sm sm:text-base text-[#F9F6F0]">
          Past Bills ({displayedOrders.length})
        </h3>

        <div className="relative w-full sm:w-64">
          <Search className="w-4 h-4 text-[#FFB300] absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search Bill / Token #"
            className="w-full bg-[#161514] border border-[#FFB300]/25 rounded-lg pl-9 pr-3 py-1.5 text-xs text-[#F9F6F0] placeholder-[#6E6760] focus:outline-none focus:border-[#FFB300]"
          />
        </div>
      </div>

      {/* 5. Past Bills List */}
      <div className="flex-1 space-y-2">
        {displayedOrders.length === 0 ? (
          <div className="bg-[#161514] border border-[#FFB300]/20 rounded-xl p-8 text-center text-[#B8B0A6]">
            <Receipt className="w-10 h-10 text-[#6E6760] mx-auto mb-2" />
            <p className="text-sm font-medium">No bills found for selected period</p>
          </div>
        ) : (
          displayedOrders.map((order) => {
            const itemsSummary = order.items
              .map((it) => `${it.menuItem.name} (${it.quantity})`)
              .join(', ');

            return (
              <div
                key={order.billId}
                onClick={() => setSelectedOrder(order)}
                className="bg-[#161514] border border-[#FFB300]/20 hover:border-[#FFB300]/40 rounded-xl p-3 flex items-center justify-between gap-3 cursor-pointer transition-all active:scale-[0.99]"
              >
                {/* Left: Info */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 flex-wrap mb-1">
                    <span className="font-bold text-sm text-[#F9F6F0]">
                      Bill #{order.billNumber}
                    </span>
                    <span className="px-2 py-0.5 rounded bg-[#22201D] border border-[#FFB300]/30 text-[11px] font-bold text-[#FFB300]">
                      Token #{order.tokenNumber}
                    </span>
                    <span className="text-xs text-[#B8B0A6]">
                      {order.orderType === 'DINE_IN'
                        ? `Dine In (T${order.tableNumber || '-'})`
                        : 'Parcel'}
                    </span>
                  </div>

                  <p className="text-xs text-[#B8B0A6] truncate">{itemsSummary}</p>

                  <p className="text-[11px] text-[#6E6760] mt-1">
                    {order.dateString} at {order.timeString} • Mode: {order.paymentMode}
                  </p>
                </div>

                {/* Right: Total & Reprint buttons */}
                <div className="flex flex-col items-end gap-1.5 shrink-0">
                  <span className="text-lg font-extrabold text-[#FFC107]">
                    ₹ {order.grandTotal.toFixed(0)}
                  </span>

                  <div
                    className="flex items-center gap-1.5"
                    onClick={(e) => e.stopPropagation()}
                  >
                    <button
                      type="button"
                      onClick={() => reprintBill(order)}
                      className="px-2.5 py-1 text-[11px] font-bold rounded border border-[#FFB300] text-[#FFB300] hover:bg-[#FFB300]/10 active:scale-95"
                    >
                      Bill
                    </button>
                    <button
                      type="button"
                      onClick={() => reprintToken(order)}
                      className="px-2.5 py-1 text-[11px] font-bold rounded border border-[#FF6D00] text-[#FF6D00] hover:bg-[#FF6D00]/10 active:scale-95"
                    >
                      KOT
                    </button>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* 6. Order Detail Modal */}
      {selectedOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/80 backdrop-blur-sm">
          <div className="relative w-full max-w-md bg-[#161514] border border-[#FFB300]/30 rounded-2xl shadow-2xl p-4 flex flex-col max-h-[90vh] overflow-hidden">
            {/* Modal Header */}
            <div className="flex items-center justify-between pb-3 border-b border-[#FFB300]/20">
              <div>
                <h3 className="font-bold text-base text-[#F9F6F0]">
                  Bill #{selectedOrder.billNumber} Details
                </h3>
                <span className="text-xs text-[#FFB300] font-bold">
                  Token #{selectedOrder.tokenNumber}
                </span>
              </div>
              <button
                onClick={() => setSelectedOrder(null)}
                className="p-1 rounded-lg text-[#B8B0A6] hover:text-[#F9F6F0] hover:bg-[#2B2824]"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Modal Body */}
            <div className="flex-1 overflow-y-auto py-3 space-y-2 text-xs">
              <div className="text-[#B8B0A6] space-y-1">
                <p>Date & Time: {selectedOrder.dateString} {selectedOrder.timeString}</p>
                <p>
                  Order Type: {selectedOrder.orderType}{' '}
                  {selectedOrder.tableNumber ? `(Table ${selectedOrder.tableNumber})` : ''}
                </p>
                {selectedOrder.customerName && (
                  <p>Customer: {selectedOrder.customerName}</p>
                )}
                <p>Payment Mode: {selectedOrder.paymentMode}</p>
              </div>

              <div className="border-t border-[#FFB300]/20 pt-2">
                <p className="font-bold text-[#F9F6F0] mb-2 text-sm">Items:</p>
                <div className="space-y-1.5">
                  {selectedOrder.items.map((item, idx) => (
                    <div
                      key={idx}
                      className="flex items-center justify-between text-xs py-0.5"
                    >
                      <span className="text-[#F9F6F0]">
                        {item.menuItem.name} × {item.quantity}
                      </span>
                      <span className="font-semibold text-[#FFC107]">
                        ₹ {item.total.toFixed(0)}
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="border-t border-[#FFB300]/20 pt-2 flex items-center justify-between">
                <span className="font-bold text-sm text-[#F9F6F0]">Grand Total:</span>
                <span className="font-extrabold text-xl text-[#FFC107]">
                  ₹ {selectedOrder.grandTotal.toFixed(0)}
                </span>
              </div>
            </div>

            {/* Modal Actions */}
            <div className="pt-3 border-t border-[#FFB300]/20 flex items-center justify-end gap-2">
              <button
                onClick={() => {
                  reprintBill(selectedOrder);
                  setSelectedOrder(null);
                }}
                className="flex items-center gap-1.5 px-3 py-2 rounded-lg bg-[#FFB300] text-[#140D00] font-bold text-xs hover:brightness-110 active:scale-95"
              >
                <Receipt className="w-4 h-4" />
                <span>Reprint Bill</span>
              </button>

              <button
                onClick={() => {
                  reprintToken(selectedOrder);
                  setSelectedOrder(null);
                }}
                className="flex items-center gap-1.5 px-3 py-2 rounded-lg bg-[#FF6D00] text-white font-bold text-xs hover:brightness-110 active:scale-95"
              >
                <Printer className="w-4 h-4" />
                <span>Reprint KOT</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
