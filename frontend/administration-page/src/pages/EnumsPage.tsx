import { useMemo, useState } from 'react';
import { useEnums } from '../domains/enum/hooks/useEnums';
import { ENUM_NAMES } from '../config/constants';
import styles from './EnumsPage.module.css';

export function EnumsPage() {
  const [query, setQuery] = useState('');

  const filteredEnumNames = useMemo(() => {
    const keyword = query.trim().toLowerCase();
    if (!keyword) return ENUM_NAMES;
    return ENUM_NAMES.filter((name) => name.toLowerCase().includes(keyword));
  }, [query]);

  return (
    <div className={styles.page}>
      <div className={styles.topRow}>
        <p className={styles.count}>총 {ENUM_NAMES.length}개 enum</p>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className={styles.searchInput}
          placeholder="enum 이름 검색 (예: marker)"
          aria-label="enum 검색"
        />
      </div>

      {filteredEnumNames.length === 0 ? (
        <div className={styles.empty}>검색 결과가 없습니다.</div>
      ) : (
        <div className={styles.grid}>
          {filteredEnumNames.map((enumName) => (
            <EnumCard key={enumName} enumName={enumName} />
          ))}
        </div>
      )}
    </div>
  );
}

function EnumCard({ enumName }: { enumName: string }) {
  const { data, isLoading } = useEnums(enumName);

  if (isLoading) {
    return (
      <div className={styles.card}>
        <div className={styles.cardHeader}>
          <h3 className={styles.cardTitle}>{enumName}</h3>
          <span className={styles.meta}>로딩</span>
        </div>
        <div className={styles.skeleton}>데이터 로딩 중…</div>
      </div>
    );
  }

  if (!data?.length) {
    return (
      <div className={styles.card}>
        <div className={styles.cardHeader}>
          <h3 className={styles.cardTitle}>{enumName}</h3>
          <span className={styles.meta}>0개</span>
        </div>
        <div className={styles.cardEmpty}>값 없음</div>
      </div>
    );
  }

  return (
    <div className={styles.card}>
      <div className={styles.cardHeader}>
        <h3 className={styles.cardTitle}>{enumName}</h3>
        <span className={styles.meta}>{data.length}개</span>
      </div>
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
