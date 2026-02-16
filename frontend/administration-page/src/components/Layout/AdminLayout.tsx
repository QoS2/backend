import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { useDarkMode } from '../../hooks/useDarkMode';
import styles from './AdminLayout.module.css';

export function AdminLayout() {
  const [darkMode, setDarkMode] = useDarkMode();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className={styles.root}>
      <Sidebar
        open={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
      />

      <div className={styles.main}>
        <Header
          darkMode={darkMode}
          onDarkModeToggle={() => setDarkMode((v) => !v)}
          onMenuClick={() => setSidebarOpen(true)}
        />

        <main className={styles.content}>
          <Outlet />
        </main>
      </div>

      {sidebarOpen && (
        <div
          className={styles.sidebarBackdrop}
          onClick={() => setSidebarOpen(false)}
          aria-hidden="true"
        />
      )}
    </div>
  );
}
