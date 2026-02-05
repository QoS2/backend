import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuests } from '../domains/quest/hooks/useQuests';
import { useQuestMutations } from '../domains/quest/hooks/useQuestMutations';
import { useQuest } from '../domains/quest/hooks/useQuest';
import { useEnums } from '../domains/enum/hooks/useEnums';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Switch } from '../components/ui/Switch';
import { Modal } from '../components/ui/Modal';
import { QuestFormDrawer } from '../domains/quest/components/QuestFormDrawer';
import { DEFAULT_PAGE_SIZE } from '../config/constants';
import type { Quest } from '../types/admin';
import type { QuestCreateRequest, QuestUpdateRequest } from '../types/admin';
import styles from './QuestsPage.module.css';

export function QuestsPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [isActiveFilter, setIsActiveFilter] = useState<boolean | undefined>(
    undefined
  );
  const [themeFilter, setThemeFilter] = useState<string>('');
  const [titleSearch, setTitleSearch] = useState('');
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editingQuestId, setEditingQuestId] = useState<string | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Quest | null>(null);

  const themeOptions = useEnums('questTheme');
  const toneOptions = useEnums('questTone');
  const difficultyOptions = useEnums('difficulty');

  const { data, isLoading: questsLoading } = useQuests({
    isActive: isActiveFilter,
    theme: themeFilter || undefined,
    page,
    size: DEFAULT_PAGE_SIZE,
  });
  const editingQuest = useQuest(editingQuestId);
  const { create, update, remove, setActive } = useQuestMutations();

  const quests = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  const filteredByTitle = useMemo(() => {
    if (!titleSearch.trim()) return quests;
    const q = titleSearch.trim().toLowerCase();
    return quests.filter((quest) =>
      quest.title.toLowerCase().includes(q)
    );
  }, [quests, titleSearch]);

  const themeSelectOptions = useMemo(
    () => themeOptions.data?.map((v) => ({ value: v, label: v })) ?? [],
    [themeOptions.data]
  );
  const toneSelectOptions = useMemo(
    () => toneOptions.data?.map((v) => ({ value: v, label: v })) ?? [],
    [toneOptions.data]
  );
  const difficultySelectOptions = useMemo(
    () => difficultyOptions.data?.map((v) => ({ value: v, label: v })) ?? [],
    [difficultyOptions.data]
  );

  const handleCreate = () => {
    setEditingQuestId(null);
    setDrawerOpen(true);
  };

  const handleEdit = (quest: Quest) => {
    setEditingQuestId(quest.id);
    setDrawerOpen(true);
  };

  const handleDrawerSubmit = (values: {
    title: string;
    subtitle?: string;
    theme: string;
    tone: string;
    difficulty: string;
    estimatedMinutes?: number | null;
    startLocationLatitude?: number | null;
    startLocationLongitude?: number | null;
    isActive?: boolean;
  }) => {
    const isEdit = editingQuestId != null;
    if (isEdit) {
      update.mutate({
        questId: editingQuestId,
        body: values as QuestUpdateRequest,
      });
    } else {
      create.mutate(values as QuestCreateRequest);
    }
    setDrawerOpen(false);
    setEditingQuestId(null);
  };

  const handleToggleActive = (quest: Quest) => {
    setActive.mutate({ questId: quest.id, active: !quest.isActive });
  };

  const handleDeleteClick = (quest: Quest) => setDeleteTarget(quest);
  const handleDeleteConfirm = () => {
    if (!deleteTarget) return;
    remove.mutate(deleteTarget.id);
    setDeleteTarget(null);
  };

  const handleOpenNodes = (quest: Quest) => {
    navigate(`/quests/${quest.id}/nodes`);
  };

  const columns = [
    { key: 'title' as const, label: '제목' },
    {
      key: 'theme' as const,
      label: '테마',
      render: (row: Quest) => row.theme,
    },
    {
      key: 'difficulty' as const,
      label: '난이도',
      render: (row: Quest) => row.difficulty,
    },
    {
      key: 'estimatedMinutes' as const,
      label: '예상 시간',
      render: (row: Quest) =>
        row.estimatedMinutes != null ? `${row.estimatedMinutes}분` : '-',
    },
    {
      key: 'isActive' as const,
      label: '활성',
      render: (row: Quest) => (
        <Switch
          checked={row.isActive}
          onChange={() => handleToggleActive(row)}
          aria-label={`활성 토글 ${row.title}`}
        />
      ),
    },
    {
      key: 'createdAt' as const,
      label: '생성일',
      render: (row: Quest) =>
        new Date(row.createdAt).toLocaleDateString('ko-KR'),
    },
    {
      key: 'actions' as const,
      label: '액션',
      render: (row: Quest) => (
        <div className={styles.cellActions}>
          <Button variant="ghost" onClick={() => handleOpenNodes(row)}>
            노드
          </Button>
          <Button variant="ghost" onClick={() => handleEdit(row)}>
            수정
          </Button>
          <Button
            variant="danger"
            onClick={() => handleDeleteClick(row)}
          >
            삭제
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className={styles.page}>
      <div className={styles.toolbar}>
        <h1 className={styles.title}>Quests</h1>
        <Button variant="primary" onClick={handleCreate}>
          퀘스트 생성
        </Button>
      </div>

      <div className={styles.filters}>
        <input
          type="search"
          placeholder="제목 검색"
          value={titleSearch}
          onChange={(e) => setTitleSearch(e.target.value)}
          className={styles.searchInput}
        />
        <select
          value={isActiveFilter === undefined ? '' : String(isActiveFilter)}
          onChange={(e) => {
            const v = e.target.value;
            setIsActiveFilter(
              v === '' ? undefined : v === 'true'
            );
          }}
          className={styles.filterSelect}
        >
          <option value="">전체</option>
          <option value="true">활성</option>
          <option value="false">비활성</option>
        </select>
        <select
          value={themeFilter}
          onChange={(e) => setThemeFilter(e.target.value)}
          className={styles.filterSelect}
        >
          <option value="">테마 전체</option>
          {themeSelectOptions.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
      </div>

      <div className={styles.tableWrap}>
        <Table<Quest>
          columns={columns}
          data={filteredByTitle}
          keyExtractor={(row) => row.id}
          isLoading={questsLoading}
          emptyMessage="퀘스트가 없습니다."
        />
      </div>

      {totalPages > 1 && (
        <div className={styles.pagination}>
          <Button
            variant="secondary"
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
          >
            이전
          </Button>
          <span className={styles.paginationInfo}>
            {page + 1} / {totalPages} (총 {totalElements}건)
          </span>
          <Button
            variant="secondary"
            disabled={page >= totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
          >
            다음
          </Button>
        </div>
      )}

      <QuestFormDrawer
        open={drawerOpen}
        onClose={() => {
          setDrawerOpen(false);
          setEditingQuestId(null);
        }}
        quest={editingQuest.data ?? null}
        themeOptions={themeSelectOptions}
        toneOptions={toneSelectOptions}
        difficultyOptions={difficultySelectOptions}
        onSubmit={handleDrawerSubmit}
        isSubmitting={create.isPending || update.isPending}
      />

      <Modal
        open={deleteTarget != null}
        onClose={() => setDeleteTarget(null)}
        title="퀘스트 삭제"
        footer={
          <>
            <Button variant="ghost" onClick={() => setDeleteTarget(null)}>
              취소
            </Button>
            <Button
              variant="danger"
              onClick={handleDeleteConfirm}
              disabled={remove.isPending}
            >
              삭제
            </Button>
          </>
        }
      >
        {deleteTarget && (
          <p>
            「{deleteTarget.title}」을(를) 삭제하시겠습니까? 연관 노드·콘텐츠 등이
            함께 삭제될 수 있습니다.
          </p>
        )}
      </Modal>
    </div>
  );
}
