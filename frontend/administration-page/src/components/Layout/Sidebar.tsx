import { NavLink } from 'react-router-dom';
import styles from './Sidebar.module.css';

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard' },
  { to: '/quests', label: 'Quests' },
  { to: '/tours', label: 'Tours' },
  { to: '/enums', label: 'Enums' },
  { to: '/mobile-preview', label: 'Mobile Preview' },
] as const;

export function Sidebar() {
  return (
    <aside className={styles.sidebar}>
      <div className={styles.logo}>Quest Admin</div>
      <nav className={styles.nav}>
        {NAV_ITEMS.map(({ to, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              [styles.navLink, isActive ? styles.navLinkActive : ''].join(' ')
            }
          >
            {label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
