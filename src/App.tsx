import React from 'react';
import { usePos } from './context/PosContext';
import { TopBar } from './components/TopBar';
import { Toast } from './components/Toast';
import { ReceiptModal } from './components/ReceiptModal';
import { BillingScreen } from './screens/BillingScreen';
import { SalesScreen } from './screens/SalesScreen';
import { MenuManagementScreen } from './screens/MenuManagementScreen';
import { SettingsScreen } from './screens/SettingsScreen';

export const App: React.FC = () => {
  const { currentTab } = usePos();

  return (
    <div className="min-h-screen bg-[#0C0B0A] text-[#F9F6F0] flex flex-col font-sans">
      <TopBar />

      <main className="flex-1 flex flex-col overflow-hidden">
        {currentTab === 'BILLING' && <BillingScreen />}
        {currentTab === 'SALES' && <SalesScreen />}
        {currentTab === 'MENU' && <MenuManagementScreen />}
        {currentTab === 'SETTINGS' && <SettingsScreen />}
      </main>

      <ReceiptModal />
      <Toast />
    </div>
  );
};
