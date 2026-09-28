import React from 'react';
import { usePos } from '../context/PosContext';

export const Toast: React.FC = () => {
  const { toastMessage } = usePos();

  if (!toastMessage) return null;

  return (
    <div className="fixed bottom-4 left-1/2 -translate-x-1/2 z-50 pointer-events-none animate-in fade-in slide-in-from-bottom-3 duration-200">
      <div className="px-4 py-2 rounded-xl bg-[#2B2824] border border-[#FFB300]/40 text-[#F9F6F0] text-xs sm:text-sm font-semibold shadow-2xl flex items-center gap-2">
        <span className="w-2 h-2 rounded-full bg-[#FFB300]" />
        <span>{toastMessage}</span>
      </div>
    </div>
  );
};
