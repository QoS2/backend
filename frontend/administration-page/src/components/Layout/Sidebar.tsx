import { NavLink } from 'react-router-dom';
import { LayoutDashboard, MapPin, ListOrdered, X } from 'lucide-react';
import styles from './Sidebar.module.css';

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/tours', label: 'Tours', icon: MapPin },
  { to: '/enums', label: 'Enums', icon: ListOrdered },
] as const;

export function Sidebar({ open, onClose }: { open?: boolean; onClose?: () => void }) {
  return (
    <aside className={`${styles.sidebar} ${open ? styles.sidebarOpen : ''}`}>
      {onClose && (
        <button
          type="button"
          className={styles.closeButton}
          onClick={onClose}
          aria-label="메뉴 닫기"
        >
          <X size={20} />
        </button>
      )}
      <div className={styles.logo}>Quest Admin</div>
      <nav className={styles.nav}>
        {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            onClick={() => onClose?.()}
            className={({ isActive }) =>
              [styles.navLink, isActive ? styles.navLinkActive : ''].join(' ')
            }
          >
            <Icon className={styles.navIcon} size={18} strokeWidth={2} />
            {label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
