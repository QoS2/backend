import { Link } from 'react-router-dom';
import styles from './DashboardPage.module.css';

export function DashboardPage() {
  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Dashboard</h1>
      <p className={styles.subtitle}>
        퀘스트·노드·콘텐츠·액션·전환을 관리할 수 있습니다.
      </p>
      <div className={styles.cards}>
        <Link to="/quests" className={styles.card}>
          <span className={styles.cardTitle}>Quests</span>
          <span className={styles.cardDesc}>퀘스트 목록 및 CRUD</span>
        </Link>
        <Link to="/quests" className={styles.card}>
          <span className={styles.cardTitle}>노드 관리</span>
          <span className={styles.cardDesc}>
            퀘스트 선택 후 노드·콘텐츠·액션·전환 관리
          </span>
        </Link>
        <Link to="/enums" className={styles.card}>
          <span className={styles.cardTitle}>Enums</span>
          <span className={styles.cardDesc}>폼용 Enum 값 조회</span>
        </Link>
      </div>
    </div>
  );
}
