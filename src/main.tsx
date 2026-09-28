import React from 'react';
import ReactDOM from 'react-dom/client';
import { PosProvider } from './context/PosContext';
import { App } from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <PosProvider>
      <App />
    </PosProvider>
  </React.StrictMode>
);
