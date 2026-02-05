import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { clearAccessToken, getAccessToken } from '../../api/auth';
import { AUTH_QUERY_KEY } from '../../hooks/useAuth';
import { LOGOUT_URL } from '../../config/constants';
import styles from './Header.module.css';

interface HeaderProps {
  searchPlaceholder?: string;
  onSearch?: (value: string) => void;
  darkMode: boolean;
  onDarkModeToggle: () => void;
}

export function Header({
  searchPlaceholder = 'Search...',
  onSearch,
  darkMode,
  onDarkModeToggle,
}: HeaderProps) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [searchValue, setSearchValue] = useState('');

  const handleLogout = (e: React.MouseEvent) => {
    e.preventDefault();
    const hadJwt = getAccessToken() != null;
    clearAccessToken();
    queryClient.removeQueries({ queryKey: AUTH_QUERY_KEY });
    if (hadJwt) {
      navigate('/login', { replace: true });
      return;
    }
    window.location.href = LOGOUT_URL;
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setSearchValue(value);
    onSearch?.(value);
  };

  return (
    <header className={styles.header}>
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
