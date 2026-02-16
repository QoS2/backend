import { NavLink } from 'react-router-dom';
import { X } from 'lucide-react';
import styles from './Sidebar.module.css';

const NAV_ITEMS = [
  { to: '/tours', label: '투어 관리', description: '투어·스팟·미션 편집' },
  { to: '/photo-submissions', label: '포토 검수', description: '유저 사진 승인/거절' },
  { to: '/enums', label: 'Enum 사전', description: '폼 상수값 조회' },
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
          <X size={18} />
        </button>
      )}

      <nav className={styles.nav}>
        {NAV_ITEMS.map(({ to, label, description }) => (
          <NavLink
            key={to}
            to={to}
            onClick={() => onClose?.()}
            className={({ isActive }) =>
              [styles.navLink, isActive ? styles.navLinkActive : ''].join(' ')
            }
          >
            <span className={styles.navText}>
              <span>{label}</span>
              <small>{description}</small>
            </span>
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
