import React, { useState } from 'react';
import {
  ArrowLeft,
  BarChart2,
  Bluetooth,
  Cloud,
  CloudCheck,
  CloudOff,
  MoreVertical,
  Printer,
  UtensilsCrossed,
  Settings as SettingsIcon,
  RefreshCw,
} from 'lucide-react';
import { usePos } from '../context/PosContext';
import { PosTab } from '../types/pos';

export const TopBar: React.FC = () => {
  const {
    currentTab,
    setCurrentTab,
    settings,
    printerState,
    syncState,
  } = usePos();

  const [menuOpen, setMenuOpen] = useState(false);

  const getScreenTitle = () => {
    switch (currentTab) {
      case 'SALES':
        return 'Sales & Reports';
      case 'MENU':
        return 'Menu Items';
      case 'SETTINGS':
        return 'Settings';
      case 'BILLING':
      default:
        return settings.shopName || 'RK TIFFINES';
    }
  };

  const getPrinterBadge = () => {
    switch (printerState.status) {
      case 'connected':
        return {
          bg: 'bg-emerald-950/60 border-emerald-500/30 text-emerald-400',
          dot: 'bg-emerald-400',
          text: 'Bluetooth Connected',
        };
      case 'connecting':
        return {
          bg: 'bg-amber-950/60 border-amber-500/30 text-amber-400',
          dot: 'bg-amber-400 animate-pulse',
          text: 'Bluetooth Connecting…',
        };
      case 'error':
      case 'unavailable':
      case 'disconnected':
      default:
        return {
          bg: 'bg-red-950/40 border-red-500/30 text-orange-400',
          dot: 'bg-red-500',
          text: 'Bluetooth Disconnected',
        };
    }
  };

  const printerBadge = getPrinterBadge();

  return (
    <header className="bg-[#0C0B0A] border-b border-[#FFB300]/20 px-3 py-2 select-none sticky top-0 z-40">
      <div className="max-w-7xl mx-auto flex items-center justify-between gap-2">
        {/* Left: Navigation / Title */}
        <div className="flex items-center gap-2 min-w-0">
          {currentTab !== 'BILLING' && (
            <button
              onClick={() => setCurrentTab('BILLING')}
              className="p-1.5 rounded-lg bg-[#161514] border border-[#FFB300]/30 text-[#F9F6F0] hover:bg-[#22201D] active:scale-95 transition-all"
              title="Back to Billing"
            >
              <ArrowLeft className="w-5 h-5 text-[#FFB300]" />
            </button>
          )}

          <div className="min-w-0">
            {currentTab === 'BILLING' ? (
              <div>
                <h1 className="text-base sm:text-lg font-extrabold text-[#F9F6F0] tracking-wide truncate">
                  {settings.shopName}
                </h1>
                <p className="text-xs font-semibold text-[#FFB300]">
                  Bill #{settings.nextBillNumber} • Token #{settings.nextTokenNumber}
                </p>
              </div>
            ) : (
              <div>
                <h1 className="text-base sm:text-lg font-bold text-[#F9F6F0] truncate">
                  {getScreenTitle()}
                </h1>
                <p className="text-xs text-[#B8B0A6]">{settings.shopName} POS</p>
              </div>
            )}
          </div>
        </div>

        {/* Right: Cloud Sync, Printer status, Overflow menu */}
        <div className="flex items-center gap-2">
          {/* Cloud Sync Status Icon */}
          <div
            className="flex items-center justify-center w-8 h-8 rounded-full bg-[#161514] border border-[#FFB300]/20 text-[#FFB300]"
            title={`Cloud Sync: ${syncState.toUpperCase()}`}
          >
            {syncState === 'syncing' ? (
              <RefreshCw className="w-4 h-4 text-[#FFB300] animate-spin" />
            ) : syncState === 'synced' ? (
              <CloudCheck className="w-4 h-4 text-emerald-400" />
            ) : syncState === 'offline' ? (
              <CloudOff className="w-4 h-4 text-[#FF6D00]" />
            ) : (
              <Cloud className="w-4 h-4 text-[#FFB300]" />
            )}
          </div>

          {/* Printer Status Pill */}
          <button
            onClick={() => setCurrentTab('SETTINGS')}
            className={`flex items-center gap-1.5 px-2.5 py-1 rounded-full border text-xs font-semibold transition-all hover:brightness-110 active:scale-95 ${printerBadge.bg}`}
            title="Thermal Printer Status - Click to configure"
          >
            <span className={`w-2 h-2 rounded-full ${printerBadge.dot}`} />
            <span className="hidden sm:inline">{printerBadge.text}</span>
            <span className="sm:hidden">POS-8380</span>
          </button>

          {/* 3-Dot Overflow Menu */}
          <div className="relative">
            <button
              onClick={() => setMenuOpen(!menuOpen)}
              className="p-1.5 rounded-lg bg-[#161514] border border-[#FFB300]/25 text-[#F9F6F0] hover:bg-[#22201D] active:scale-95 transition-all"
              aria-label="Options Menu"
            >
              <MoreVertical className="w-5 h-5 text-[#F9F6F0]" />
            </button>

            {menuOpen && (
              <>
                <div
                  className="fixed inset-0 z-40"
                  onClick={() => setMenuOpen(false)}
                />
                <div className="absolute right-0 mt-2 w-56 bg-[#2B2824] border border-[#FFB300]/30 rounded-xl shadow-2xl py-1.5 z-50 text-sm animate-in fade-in zoom-in-95 duration-100">
                  <button
                    onClick={() => {
                      setCurrentTab('BILLING');
                      setMenuOpen(false);
                    }}
                    className={`w-full flex items-center gap-3 px-4 py-2.5 text-left hover:bg-[#38332D] transition-colors ${
                      currentTab === 'BILLING' ? 'text-[#FFB300] font-bold bg-[#38332D]/60' : 'text-[#F9F6F0]'
                    }`}
                  >
                    <UtensilsCrossed className="w-4 h-4 text-[#FFB300]" />
                    <span>POS Billing</span>
                  </button>

                  <button
                    onClick={() => {
                      setCurrentTab('SALES');
                      setMenuOpen(false);
                    }}
                    className={`w-full flex items-center gap-3 px-4 py-2.5 text-left hover:bg-[#38332D] transition-colors ${
                      currentTab === 'SALES' ? 'text-[#FFB300] font-bold bg-[#38332D]/60' : 'text-[#F9F6F0]'
                    }`}
                  >
                    <BarChart2 className="w-4 h-4 text-[#FFB300]" />
                    <span>Sales & Reports</span>
                  </button>

                  <button
                    onClick={() => {
                      setCurrentTab('MENU');
                      setMenuOpen(false);
                    }}
                    className={`w-full flex items-center gap-3 px-4 py-2.5 text-left hover:bg-[#38332D] transition-colors ${
                      currentTab === 'MENU' ? 'text-[#FFB300] font-bold bg-[#38332D]/60' : 'text-[#F9F6F0]'
                    }`}
                  >
                    <UtensilsCrossed className="w-4 h-4 text-[#FF6D00]" />
                    <span>Menu Items & Departments</span>
                  </button>

                  <button
                    onClick={() => {
                      setCurrentTab('SETTINGS');
                      setMenuOpen(false);
                    }}
                    className={`w-full flex items-center gap-3 px-4 py-2.5 text-left hover:bg-[#38332D] transition-colors ${
                      currentTab === 'SETTINGS' ? 'text-[#FFB300] font-bold bg-[#38332D]/60' : 'text-[#F9F6F0]'
                    }`}
                  >
                    <SettingsIcon className="w-4 h-4 text-[#FFC107]" />
                    <span>Settings & Printer</span>
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
