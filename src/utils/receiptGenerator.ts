import { BillRecord, ReceiptFormatConfig, ShopSettings } from '../types/pos';

export interface GeneratedReceipt {
  html: string;
  plainText: string;
  isKot?: boolean;
}

export function generateCustomerReceiptHtml(
  bill: BillRecord,
  settings: ShopSettings
): string {
  const format: ReceiptFormatConfig = settings.receiptFormat;
  const is58mm = settings.paperWidth === 'WIDTH_58MM';
  const widthClass = is58mm ? 'max-w-[280px]' : 'max-w-[380px]';

  const getAlignClass = (alignment: 'LEFT' | 'CENTER' | 'RIGHT') => {
    switch (alignment) {
      case 'CENTER':
        return 'text-center';
      case 'RIGHT':
        return 'text-right';
      default:
        return 'text-left';
    }
  };

  const getFontClass = (fontSize: 'NORMAL' | 'DOUBLE_HEIGHT' | 'DOUBLE_WIDTH' | 'DOUBLE_BOTH', bold: boolean) => {
    const boldClass = bold ? 'font-bold' : 'font-normal';
    switch (fontSize) {
      case 'DOUBLE_HEIGHT':
        return `${boldClass} text-lg scale-y-125 origin-center my-0.5`;
      case 'DOUBLE_WIDTH':
        return `${boldClass} text-lg tracking-wider`;
      case 'DOUBLE_BOTH':
        return `${boldClass} text-xl tracking-wider my-1`;
      default:
        return `${boldClass} text-sm`;
    }
  };

  const sepChar = format.separatorChar || '-';
  const sepLine = sepChar.repeat(is58mm ? 32 : 44);

  let html = `<div class="font-mono text-black bg-white p-4 leading-tight mx-auto ${widthClass} text-xs select-text shadow-md border border-neutral-300">`;

  // 1. Logo
  if (format.logoVisible && settings.showLogo && settings.logoUrl) {
    html += `
      <div class="flex justify-center mb-2">
        <img src="${settings.logoUrl}" alt="Shop Logo" class="max-h-16 max-w-[120px] object-contain filter grayscale contrast-200" />
      </div>
    `;
  }

  // 2. Shop Name
  if (format.shopName.visible && settings.shopName) {
    html += `<div class="${getAlignClass(format.shopName.alignment)} ${getFontClass(format.shopName.fontSize, format.shopName.bold)}">${settings.shopName}</div>`;
  }

  // 3. Address
  if (format.address.visible && settings.address) {
    html += `<div class="${getAlignClass(format.address.alignment)} ${getFontClass(format.address.fontSize, format.address.bold)} text-neutral-700">${settings.address}</div>`;
  }

  // 4. Phone
  if (format.phone.visible && settings.phone) {
    html += `<div class="${getAlignClass(format.phone.alignment)} ${getFontClass(format.phone.fontSize, format.phone.bold)} text-neutral-700">Ph: ${settings.phone}</div>`;
  }

  html += `<div class="my-1.5"></div>`;

  // 5. Bill Number & 6. Token Number
  const showBill = format.billNumber.visible;
  const showToken = format.tokenNumber.visible;
  if (showBill || showToken) {
    html += `<div class="flex justify-between items-center text-xs my-0.5">`;
    if (showBill) {
      html += `<span class="${getFontClass(format.billNumber.fontSize, format.billNumber.bold)}">Bill No: #${bill.billNumber}</span>`;
    } else {
      html += `<span></span>`;
    }
    if (showToken) {
      html += `<span class="${getFontClass(format.tokenNumber.fontSize, format.tokenNumber.bold)} font-mono">Token: #${bill.tokenNumber}</span>`;
    }
    html += `</div>`;
  }

  // 7. Date & 8. Time
  const showDate = format.date.visible;
  const showTime = format.time.visible;
  if (showDate || showTime) {
    html += `<div class="flex justify-between items-center text-xs text-neutral-700 my-0.5">`;
    if (showDate) {
      html += `<span class="${getFontClass(format.date.fontSize, format.date.bold)}">Date: ${bill.dateString}</span>`;
    } else {
      html += `<span></span>`;
    }
    if (showTime) {
      html += `<span class="${getFontClass(format.time.fontSize, format.time.bold)}">Time: ${bill.timeString}</span>`;
    }
    html += `</div>`;
  }

  // 9. Order Type (DINE IN / PARCEL)
  if (format.orderType.visible) {
    const typeLabel = bill.orderType === 'DINE_IN' ? 'TYPE: DINE IN' : 'TYPE: PARCEL (Takeaway)';
    html += `<div class="${getAlignClass(format.orderType.alignment)} ${getFontClass(format.orderType.fontSize, format.orderType.bold)} text-xs my-0.5">${typeLabel}</div>`;
  }

  // 10. Table Number
  if (format.tableNumber.visible && bill.tableNumber) {
    html += `<div class="${getAlignClass(format.tableNumber.alignment)} ${getFontClass(format.tableNumber.fontSize, format.tableNumber.bold)} text-xs my-0.5">Table: ${bill.tableNumber}</div>`;
  }

  // 11. Customer Name
  if (format.customerName.visible && bill.customerName) {
    html += `<div class="${getAlignClass(format.customerName.alignment)} ${getFontClass(format.customerName.fontSize, format.customerName.bold)} text-xs my-0.5">Customer: ${bill.customerName}</div>`;
  }

  // Separator line before items
  if (format.separatorLinesVisible) {
    html += `<div class="overflow-hidden text-neutral-500 my-1 font-mono tracking-tighter">${sepLine}</div>`;
  }

  // 12. ITEM Heading
  if (format.itemHeading.visible) {
    html += `<div class="flex justify-between items-center ${getFontClass(format.itemHeading.fontSize, format.itemHeading.bold)} text-xs py-0.5 border-b border-neutral-300">`;
    html += `<span class="flex-1 text-left">ITEM</span>`;
    if (format.colQty.visible) html += `<span class="w-10 text-center ${format.colQty.bold ? 'font-bold' : ''}">QTY</span>`;
    if (format.colRate.visible) html += `<span class="w-14 text-right ${format.colRate.bold ? 'font-bold' : ''}">RATE</span>`;
    if (format.colTotal.visible) html += `<span class="w-14 text-right ${format.colTotal.bold ? 'font-bold' : ''}">TOTAL</span>`;
    html += `</div>`;
  }

  // 13. Items list
  html += `<div class="space-y-1 my-1">`;
  bill.items.forEach((item) => {
    html += `<div class="flex justify-between items-center text-xs py-0.5">`;
    html += `<span class="flex-1 text-left ${getFontClass(format.itemName.fontSize, format.itemName.bold)} truncate pr-1">${item.menuItem.name}</span>`;
    if (format.colQty.visible) html += `<span class="w-10 text-center font-mono ${format.colQty.bold ? 'font-bold' : ''}">${item.quantity}</span>`;
    if (format.colRate.visible) html += `<span class="w-14 text-right font-mono ${format.colRate.bold ? 'font-bold' : ''}">₹${item.menuItem.price.toFixed(0)}</span>`;
    if (format.colTotal.visible) html += `<span class="w-14 text-right font-mono ${format.colTotal.bold ? 'font-bold' : ''}">₹${item.total.toFixed(0)}</span>`;
    html += `</div>`;
  });
  html += `</div>`;

  // Separator line before grand total
  if (format.separatorLinesVisible) {
    html += `<div class="overflow-hidden text-neutral-500 my-1 font-mono tracking-tighter">${sepLine}</div>`;
  }

  // 17. Grand Total
  if (format.grandTotal.visible) {
    html += `
      <div class="${getAlignClass(format.grandTotal.alignment)} ${getFontClass(format.grandTotal.fontSize, format.grandTotal.bold)} py-1">
        TOTAL: ₹ ${bill.grandTotal.toFixed(0)}
      </div>
    `;
  }

  // 19. Payment Mode
  if (format.paymentMode.visible) {
    html += `
      <div class="flex justify-between items-center text-xs my-1 text-neutral-800 ${getFontClass(format.paymentMode.fontSize, format.paymentMode.bold)}">
        <span>Payment: ${bill.paymentMode}</span>
        <span>Items: ${bill.itemCount}</span>
      </div>
    `;
  }

  // Separator line before footer
  if (format.separatorLinesVisible) {
    html += `<div class="overflow-hidden text-neutral-500 my-1 font-mono tracking-tighter">${sepLine}</div>`;
  }

  // 20. Footer / Thank-you message
  if (format.footerMessage.visible && settings.receiptFooter) {
    html += `
      <div class="${getAlignClass(format.footerMessage.alignment)} ${getFontClass(format.footerMessage.fontSize, format.footerMessage.bold)} text-xs py-1 text-neutral-700">
        ${settings.receiptFooter}
      </div>
    `;
  }

  if (settings.autoCutPaper && format.autoCut) {
    html += `<div class="text-center text-[10px] text-neutral-400 mt-2 tracking-widest border-t border-dashed border-neutral-300 pt-1">--- AUTO CUT LINE ---</div>`;
  }

  html += `</div>`;
  return html;
}

export function generateKitchenTicketHtml(
  bill: BillRecord,
  settings: ShopSettings
): string {
  const is58mm = settings.paperWidth === 'WIDTH_58MM';
  const widthClass = is58mm ? 'max-w-[280px]' : 'max-w-[380px]';
  const sepLine = '='.repeat(is58mm ? 32 : 44);

  let html = `<div class="font-mono text-black bg-white p-4 leading-tight mx-auto ${widthClass} text-xs select-text shadow-md border-2 border-neutral-800">`;

  // Header
  html += `
    <div class="text-center font-bold text-sm tracking-wider">*** KITCHEN ORDER TICKET ***</div>
    <div class="text-center font-bold text-base my-0.5">${settings.shopName}</div>
    <div class="text-center font-extrabold text-3xl my-2 border-2 border-black py-1.5 bg-neutral-100">
      TOKEN #${bill.tokenNumber}
    </div>
  `;

  // Order Type & Table
  const orderTypeStr = bill.orderType === 'DINE_IN'
    ? `DINE IN ${bill.tableNumber ? `- TABLE #${bill.tableNumber}` : ''}`
    : 'PARCEL / TAKEAWAY';

  html += `
    <div class="text-center font-bold text-base my-1 underline">${orderTypeStr}</div>
    <div class="flex justify-between text-xs text-neutral-700 my-1">
      <span>Date: ${bill.dateString}</span>
      <span>Time: ${bill.timeString}</span>
    </div>
    <div class="overflow-hidden text-neutral-800 my-1 font-mono tracking-tighter">${sepLine}</div>
  `;

  // Column header
  html += `
    <div class="flex justify-between items-center font-bold text-xs py-1 border-b border-black">
      <span class="flex-1 text-left">ITEM</span>
      <span class="w-12 text-center text-sm">QTY</span>
    </div>
    <div class="space-y-2 my-2">
  `;

  // Items with large font
  bill.items.forEach((item) => {
    html += `
      <div class="flex justify-between items-center text-sm py-1 border-b border-dashed border-neutral-300">
        <span class="flex-1 text-left font-bold ${settings.kotItemFontSize === 'LARGE' ? 'text-base' : 'text-sm'}">${item.menuItem.name}</span>
        <span class="w-12 text-center font-extrabold text-lg bg-black text-white rounded px-1">${item.quantity}</span>
      </div>
    `;
  });

  html += `
    </div>
    <div class="overflow-hidden text-neutral-800 my-1 font-mono tracking-tighter">${sepLine}</div>
    <div class="flex justify-between items-center font-bold text-sm my-1">
      <span>Total Items:</span>
      <span>${bill.itemCount}</span>
    </div>
  `;

  if (settings.autoCutPaper) {
    html += `<div class="text-center text-[10px] text-neutral-400 mt-3 tracking-widest border-t border-dashed border-neutral-300 pt-1">--- AUTO CUT LINE ---</div>`;
  }

  html += `</div>`;
  return html;
}
