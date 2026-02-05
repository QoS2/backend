import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { useDarkMode } from '../../hooks/useDarkMode';
import styles from './AdminLayout.module.css';

export function AdminLayout() {
  const [darkMode, setDarkMode] = useDarkMode();

  return (
    <div className={styles.root}>
      <Sidebar />
      <div className={styles.main}>
        <Header
          darkMode={darkMode}
          onDarkModeToggle={() => setDarkMode((v) => !v)}
        />
        <main className={styles.content}>
          <Outlet />
        </main>
      </div>
    </div>
  );
}
