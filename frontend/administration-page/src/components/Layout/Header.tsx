import { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { Menu } from 'lucide-react';
import { clearAccessToken, getAccessToken } from '../../api/auth';
import { AUTH_QUERY_KEY } from '../../hooks/useAuth';
import { LOGOUT_URL } from '../../config/constants';
import styles from './Header.module.css';

interface HeaderProps {
  searchPlaceholder?: string;
  onSearch?: (value: string) => void;
  darkMode: boolean;
  onDarkModeToggle: () => void;
  onMenuClick?: () => void;
}

export function Header({
  searchPlaceholder = 'Search...',
  onSearch,
  darkMode,
  onDarkModeToggle,
  onMenuClick,
}: HeaderProps) {
  const queryClient = useQueryClient();
  const [searchValue, setSearchValue] = useState('');

  const handleLogout = async (e: React.MouseEvent) => {
    e.preventDefault();
    clearAccessToken();
    queryClient.removeQueries({ queryKey: AUTH_QUERY_KEY });
    try {
      await fetch(LOGOUT_URL, { method: 'GET', credentials: 'include' });
    } finally {
      // 전체 페이지 이동으로 확실히 로그인 페이지로
      window.location.replace('/login');
    }
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setSearchValue(value);
    onSearch?.(value);
  };

  return (
    <header className={styles.header}>
      <button
        type="button"
        className={styles.menuButton}
        onClick={onMenuClick}
        aria-label="메뉴 열기"
      >
        <Menu size={22} />
      </button>
      {onSearch ? (
        <input
          type="search"
          className={styles.search}
          placeholder={searchPlaceholder}
          value={searchValue}
          onChange={handleSearchChange}
          aria-label="Search"
        />
      ) : (
        <div className={styles.searchPlaceholder} />
      )}
      <div className={styles.actions}>
        <button
          type="button"
          className={styles.iconButton}
          onClick={onDarkModeToggle}
          aria-label={darkMode ? 'Switch to light mode' : 'Switch to dark mode'}
          title={darkMode ? 'Light mode' : 'Dark mode'}
        >
          {darkMode ? '☀️' : '🌙'}
        </button>
        <a
          href="/login"
          className={styles.logoutLink}
          onClick={handleLogout}
        >
          로그아웃
        </a>
        <div className={styles.profile}>
          <span className={styles.profileLabel}>Admin</span>
        </div>
      </div>
    </header>
  );
}
