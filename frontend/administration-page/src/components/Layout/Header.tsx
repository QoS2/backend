import { useState } from 'react';
import { useLocation } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { LogOut, Menu, MoonStar, SunMedium } from 'lucide-react';
import { clearAccessToken, fetchAuthLogout } from '../../api/auth';
import { AUTH_QUERY_KEY } from '../../hooks/useAuth';
import styles from './Header.module.css';

interface HeaderProps {
  darkMode: boolean;
  onDarkModeToggle: () => void;
  onMenuClick?: () => void;
}

const PAGE_META: Record<string, { title: string; subtitle: string }> = {
  '/tours': {
    title: '투어 관리',
    subtitle: '투어, 스팟, 미션, 가이드를 편집합니다.',
  },
  '/photo-submissions': {
    title: '포토 제출 검수',
    subtitle: 'PHOTO 스팟 제출 사진을 승인/거절합니다.',
  },
  '/enums': {
    title: 'Enum 사전',
    subtitle: '클라이언트 폼에서 사용하는 enum 값을 조회합니다.',
  },
};

export function Header({
  darkMode,
  onDarkModeToggle,
  onMenuClick,
}: HeaderProps) {
  const location = useLocation();
  const queryClient = useQueryClient();
  const [loggingOut, setLoggingOut] = useState(false);

  const pageMeta = PAGE_META[location.pathname] ?? {
    title: 'Tour Administrator',
    subtitle: '운영 데이터를 관리합니다.',
  };

  const handleLogout = async (e: React.MouseEvent) => {
    e.preventDefault();
    if (loggingOut) return;

    setLoggingOut(true);
    clearAccessToken();
    queryClient.removeQueries({ queryKey: AUTH_QUERY_KEY });

    try {
      await fetchAuthLogout();
    } finally {
      window.location.replace('/login');
    }
  };

  return (
    <header className={styles.header}>
      <div className={styles.left}>
        <button
          type="button"
          className={styles.menuButton}
          onClick={onMenuClick}
          aria-label="메뉴 열기"
        >
          <Menu size={18} />
        </button>

        <div className={styles.titleWrap}>
          <h1>{pageMeta.title}</h1>
          <p className={styles.subtitle}>{pageMeta.subtitle}</p>
        </div>
      </div>

      <div className={styles.actions}>
        <button
          type="button"
          className={styles.iconButton}
          onClick={onDarkModeToggle}
          aria-label={darkMode ? '라이트 모드 전환' : '다크 모드 전환'}
          title={darkMode ? '라이트 모드' : '다크 모드'}
        >
          {darkMode ? <SunMedium size={16} /> : <MoonStar size={16} />}
        </button>

        <div className={styles.profilePill}>
          <span className={styles.profileName}>ADMIN</span>
          <button
            type="button"
            className={styles.logoutButton}
            onClick={handleLogout}
            disabled={loggingOut}
          >
            <LogOut size={15} />
            <span>{loggingOut ? '종료 중...' : '로그아웃'}</span>
          </button>
        </div>
      </div>
    </header>
  );
}
