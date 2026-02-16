import { createContext } from 'react';

type ToastType = 'error' | 'success';

export interface ToastItem {
  id: number;
  type: ToastType;
  message: string;
}

export interface ToastContextValue {
  showError: (message: string) => void;
  showSuccess: (message: string) => void;
}

export const ToastContext = createContext<ToastContextValue | null>(null);

export const TOAST_DURATION_MS = 4000;
