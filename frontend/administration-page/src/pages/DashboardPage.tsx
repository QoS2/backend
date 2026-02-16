import { Link } from 'react-router-dom';
import { ArrowRight, Camera, ListOrdered, MapPinned, ShieldCheck } from 'lucide-react';
import styles from './DashboardPage.module.css';

const QUICK_LINKS = [
  {
    to: '/tours',
    title: '투어 관리',
    description: '투어, 스팟, 미션, 가이드 콘텐츠를 편집합니다.',
    icon: MapPinned,
  },
  {
    to: '/photo-submissions',
    title: '포토 검수',
    description: '유저 제출 이미지를 빠르게 승인/거절합니다.',
    icon: Camera,
  },
  {
    to: '/enums',
    title: 'Enum 사전',
    description: '폼에서 사용하는 enum 상수값을 조회합니다.',
    icon: ListOrdered,
  },
] as const;

const PRINCIPLES = [
  '작은 단위로 자주 저장하고 즉시 확인하기',
  '스팟 좌표/반경 변경 시 지도에서 시각적으로 검증하기',
  '사용자 노출 콘텐츠(가이드/미션/사진)는 최종 미리보기로 확인하기',
] as const;

export function DashboardPage() {
  return (
    <div className={styles.page}>
      <section className={styles.hero}>
        <div>
          <p className={styles.eyebrow}>Operations</p>
          <h2>Quest of Seoul 운영 화면</h2>
          <p className={styles.subtitle}>
            투어 운영에 필요한 작업을 최소 단계로 수행할 수 있도록 구성했습니다.
          </p>
        </div>
        <div className={styles.badge}>
          <ShieldCheck size={16} />
          <span>권장 워크플로우 적용</span>
        </div>
      </section>

      <section className={styles.grid}>
        {QUICK_LINKS.map(({ to, title, description, icon: Icon }) => (
          <Link key={to} to={to} className={styles.card}>
            <div className={styles.cardIcon}>
              <Icon size={18} />
            </div>
            <div className={styles.cardBody}>
              <strong>{title}</strong>
              <p>{description}</p>
            </div>
            <ArrowRight size={16} className={styles.cardArrow} />
          </Link>
        ))}
      </section>

      <section className={styles.principles}>
        <h3>UI/UX 운영 원칙</h3>
        <ul>
          {PRINCIPLES.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      </section>
    </div>
  );
}
