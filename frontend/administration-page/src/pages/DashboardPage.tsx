import { Link } from 'react-router-dom';
import styles from './DashboardPage.module.css';

export function DashboardPage() {
  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Dashboard</h1>
      <p className={styles.subtitle}>
        투어·스팟·Enum을 관리할 수 있습니다.
      </p>
      <div className={styles.cards}>
        <Link to="/tours" className={styles.card}>
          <span className={styles.cardTitle}>Tours</span>
          <span className={styles.cardDesc}>투어 및 스팟(MAIN/SUB/PHOTO/TREASURE) 관리</span>
        </Link>
        <Link to="/enums" className={styles.card}>
          <span className={styles.cardTitle}>Enums</span>
          <span className={styles.cardDesc}>폼용 Enum 값 조회 (language, spotType, markerType, stepKind)</span>
        </Link>
      </div>
    </div>
  );
}
