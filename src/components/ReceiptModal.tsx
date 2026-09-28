import React from 'react';
import { Check, Printer, X } from 'lucide-react';
import { usePos } from '../context/PosContext';
import {
  generateCustomerReceiptHtml,
  generateKitchenTicketHtml,
} from '../utils/receiptGenerator';

export const ReceiptModal: React.FC = () => {
  const { activeReceiptPreview, closeReceiptPreview, settings } = usePos();

  if (!activeReceiptPreview) return null;

  const { type, bill } = activeReceiptPreview;
  const isKot = type === 'token';
  const receiptHtml = isKot
    ? generateKitchenTicketHtml(bill, settings)
    : generateCustomerReceiptHtml(bill, settings);

  const handleBrowserPrint = () => {
    window.print();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/80 backdrop-blur-sm animate-in fade-in">
      <div className="relative w-full max-w-md bg-[#161514] border border-[#FFB300]/30 rounded-2xl shadow-2xl flex flex-col max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-4 py-3 border-b border-[#FFB300]/20 bg-[#22201D]">
          <div>
            <h3 className="font-bold text-base text-[#F9F6F0]">
              {isKot ? 'Kitchen Order Ticket (KOT)' : `Customer Bill #${bill.billNumber}`}
            </h3>
            <p className="text-xs text-[#FFB300]">
              Token #{bill.tokenNumber} • {settings.paperWidth === 'WIDTH_80MM' ? '80mm' : '58mm'} Thermal Format
            </p>
          </div>
          <button
            onClick={closeReceiptPreview}
            className="p-1 rounded-lg text-[#B8B0A6] hover:text-[#F9F6F0] hover:bg-[#2B2824] transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable Receipt Body */}
        <div className="flex-1 overflow-y-auto p-4 bg-[#0C0B0A] flex justify-center items-start">
          <div
            id="printable-receipt"
            className="w-full flex justify-center"
            dangerouslySetInnerHTML={{ __html: receiptHtml }}
          />
        </div>

        {/* Footer Actions */}
        <div className="px-4 py-3 border-t border-[#FFB300]/20 bg-[#22201D] flex items-center justify-end gap-2.5">
          <button
            onClick={closeReceiptPreview}
            className="px-4 py-2 text-xs font-semibold rounded-lg border border-[#FFB300]/20 text-[#B8B0A6] hover:bg-[#2B2824] hover:text-[#F9F6F0] transition-colors"
          >
            Close
          </button>

          <button
            onClick={handleBrowserPrint}
            className="flex items-center gap-1.5 px-4 py-2 text-xs font-bold rounded-lg bg-gradient-to-r from-[#FFB300] to-[#FF6D00] text-[#140D00] hover:brightness-110 active:scale-95 transition-all shadow-md"
          >
            <Printer className="w-4 h-4" />
            <span>Print to POS-8380</span>
          </button>
        </div>
      </div>
    </div>
  );
};
