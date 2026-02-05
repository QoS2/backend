import { useEnums } from '../domains/enum/hooks/useEnums';
import { ENUM_NAMES } from '../config/constants';
import styles from './EnumsPage.module.css';

export function EnumsPage() {
  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Enums</h1>
      <p className={styles.subtitle}>
        폼에서 사용할 Enum 상수 목록입니다. 이름으로 API에서 조회할 수 있습니다.
      </p>
      <div className={styles.grid}>
        {ENUM_NAMES.map((enumName) => (
          <EnumCard key={enumName} enumName={enumName} />
        ))}
      </div>
    </div>
  );
}

function EnumCard({ enumName }: { enumName: string }) {
  const { data, isLoading } = useEnums(enumName);

  if (isLoading) {
    return (
      <div className={styles.card}>
        <h2 className={styles.cardTitle}>{enumName}</h2>
        <div className={styles.skeleton}>로딩 중…</div>
      </div>
    );
  }

  if (!data?.length) {
    return (
      <div className={styles.card}>
        <h2 className={styles.cardTitle}>{enumName}</h2>
        <div className={styles.empty}>값 없음</div>
      </div>
    );
  }

  return (
    <div className={styles.card}>
      <h2 className={styles.cardTitle}>{enumName}</h2>
      <ul className={styles.list}>
        {data.map((value) => (
          <li key={value} className={styles.listItem}>
            <code className={styles.code}>{value}</code>
          </li>
        ))}
      </ul>
    </div>
  );
}
