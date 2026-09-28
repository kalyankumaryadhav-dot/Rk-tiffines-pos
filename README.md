# RK TIFFINES POS

A modern, high-performance web POS billing application for **RK TIFFINES**, rewritten as a React + TypeScript application with Vite and Tailwind CSS.

## Features

- **POS Billing**:
  - DINE IN (with table assignment) & PARCEL / Takeaway modes
  - Department chips navigation with instant item filtering (12 official departments, 115 items)
  - Fast touch-friendly menu item cards with vegetarian indicators and price tags
  - Order cart with itemized adjustments, line totals, and payment modes (CASH, UPI, CARD)
  - Quick action buttons: PRINT BILL, KOT TOKEN, and BILL + TOKEN (PRINT BOTH)
  - Mobile responsive with compact bottom summary bar and full order preview dialog

- **Thermal Receipt & KOT Printing**:
  - Thermal receipt printing with dual 80mm (POS-8380) and 58mm paper support
  - 20 independently configurable receipt elements (Logo, Shop name, address, phone, bill #, token #, date, time, order type, table #, customer name, item heading, item name, Qty/Rate/Total columns, grand total, separator styles, payment mode, footer, auto-cut)
  - Live interactive receipt preview in Settings
  - Kitchen Order Ticket (KOT) with prominent Token #, table info, and large item quantities
  - Browser print support (`window.print`) optimized for POS thermal printers

- **Sales & Reports**:
  - Time filters: TODAY, WEEKLY, MONTHLY, and ALL
  - Summary metric cards: Total Revenue, Cash total, UPI total, Card total, and Bill counts
  - Past bill search by Bill #, Token #, Customer name, or Table #
  - Itemized bill detail view with quick reprint actions for Bill and KOT

- **Menu & Department Management**:
  - Full CRUD for menu items with stock availability toggle
  - Department management modal: add departments, edit names/codes, delete, and reorder
  - Instant live search across all departments and item titles
  - One-click restore to official RK TIFFINES 12 departments and 115 items

- **Settings**:
  - Business profile management (Shop name, address, phone)
  - Receipt logo upload, raster thermal preview, and removal
  - Bill number and token counter controls with reset
  - Bluetooth printer connection state and automation controls (auto-cut, auto-print preferences)
  - Cloud sales synchronization status and manual trigger

## Technology Stack

- **Framework**: React 19, TypeScript
- **Bundler & Dev Server**: Vite 6
- **Styling**: Tailwind CSS v4 with custom RK Tiffines dark gold/charcoal design system
- **Icons**: Lucide React
- **Persistence**: Local storage with offline-first state management
