import { useCallback, useEffect, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import {
  ToastContext,
  TOAST_DURATION_MS,
  type ToastItem,
} from './toast-context';
import styles from '../components/ui/Toast.module.css';

let nextId = 0;

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([]);
  const queryClient = useQueryClient();

  const showError = useCallback((message: string) => {
    const id = nextId++;
    setToasts((prev) => [...prev, { id, type: 'error', message }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, TOAST_DURATION_MS);
  }, []);

  const showSuccess = useCallback((message: string) => {
    const id = nextId++;
    setToasts((prev) => [...prev, { id, type: 'success', message }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, TOAST_DURATION_MS);
  }, []);

  useEffect(() => {
    const cache = queryClient.getMutationCache();
    const unsub = cache.subscribe((event) => {
      if (event.type === 'updated') {
        const mutation = event.mutation;
        if (mutation.state.status === 'error' && mutation.state.error) {
          const msg =
            mutation.state.error instanceof Error
              ? mutation.state.error.message
              : String(mutation.state.error);
          showError(msg || '오류가 발생했습니다.');
        }
      }
    });
    return () => unsub();
  }, [queryClient, showError]);

  const removeToast = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ showError, showSuccess }}>
      {children}
      <div className={styles.container} role="region" aria-label="알림">
        {toasts.map((t) => (
          <div
            key={t.id}
            className={`${styles.toast} ${styles[t.type]}`}
            role="alert"
          >
            <span className={styles.message}>{t.message}</span>
            <button
              type="button"
              className={styles.close}
              onClick={() => removeToast(t.id)}
              aria-label="닫기"
            >
              ×
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}
