import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import styles from './AuthGuard.module.css';

interface AuthGuardProps {
  children: React.ReactNode;
}

export function AuthGuard({ children }: AuthGuardProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const { data, isLoading, isError } = useAuth();

  useEffect(() => {
    if (isLoading) return;
    if (isError || !data) {
      const from = location.pathname + location.search;
      navigate(`/login${from !== '/' ? `?from=${encodeURIComponent(from)}` : ''}`, {
        replace: true,
      });
    }
  }, [data, isError, isLoading, location.pathname, location.search, navigate]);

  if (isLoading) {
    return (
      <div className={styles.wrapper}>
        <div className={styles.spinner} aria-label="로딩 중" />
        <span className={styles.label}>로그인 확인 중…</span>
      </div>
    );
  }

  if (isError || !data) {
    return null;
  }

  return <>{children}</>;
}
