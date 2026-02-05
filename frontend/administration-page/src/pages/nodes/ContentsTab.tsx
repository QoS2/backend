import { useState } from 'react';
import { Button } from '../../components/ui/Button';
import { Drawer } from '../../components/ui/Drawer';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { Textarea } from '../../components/ui/Textarea';
import { useContents } from '../../domains/content/hooks/useContents';
import { useContentMutations } from '../../domains/content/hooks/useContentMutations';
import { useEnums } from '../../domains/enum/hooks/useEnums';
import type {
  Content,
  ContentCreateRequest,
  ContentUpdateRequest,
} from '../../types/admin';
import styles from '../NodesPage.module.css';

interface ContentsTabProps {
  questId: string;
  nodeId: string;
}

export function ContentsTab({ questId, nodeId }: ContentsTabProps) {
  const { data: list = [], isLoading } = useContents(questId, nodeId);
  const mutations = useContentMutations(questId, nodeId);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editing, setEditing] = useState<Content | null>(null);

  const contentTypeOptions = useEnums('contentType');
  const languageOptions = useEnums('language');
  const displayModeOptions = useEnums('displayMode');

  const handleCreate = () => {
    setEditing(null);
    setDrawerOpen(true);
  };

  const handleEdit = (c: Content) => {
    setEditing(c);
    setDrawerOpen(true);
  };

  const handleClose = () => {
    setDrawerOpen(false);
    setEditing(null);
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const form = e.currentTarget;
    const contentOrder = parseInt(
      (form.elements.namedItem('contentOrder') as HTMLInputElement)?.value ?? '0',
      10
    );
    const contentType = (form.elements.namedItem('contentType') as HTMLSelectElement)
      ?.value as Content['contentType'];
    const language = (form.elements.namedItem('language') as HTMLSelectElement)
      ?.value as Content['language'];
    const body = (form.elements.namedItem('body') as HTMLTextAreaElement)?.value ?? '';
    const displayMode = (form.elements.namedItem('displayMode') as HTMLSelectElement)
      ?.value as Content['displayMode'];

    if (editing) {
      const bodyUpdate: ContentUpdateRequest = {
        contentOrder,
        contentType,
        language,
        body,
        displayMode,
      };
      mutations.update.mutate(
        { contentId: editing.id, body: bodyUpdate },
        { onSuccess: handleClose }
      );
    } else {
      const bodyCreate: ContentCreateRequest = {
        contentOrder,
        contentType,
        language,
        body,
        displayMode,
      };
      mutations.create.mutate(bodyCreate, { onSuccess: handleClose });
    }
  };

  const handleDelete = (c: Content) => {
    if (window.confirm(`콘텐츠 "${c.body.slice(0, 30)}…"을(를) 삭제할까요?`)) {
      mutations.remove.mutate(c.id);
    }
  };

  const contentTypeOpts =
    contentTypeOptions.data?.map((v) => ({ value: v, label: v })) ?? [];
  const languageOpts =
    languageOptions.data?.map((v) => ({ value: v, label: v })) ?? [];
  const displayModeOpts =
    displayModeOptions.data?.map((v) => ({ value: v, label: v })) ?? [];

  if (isLoading) {
    return <p className={styles.placeholder}>로딩 중…</p>;
  }

  return (
    <div className={styles.tabSection}>
      <div className={styles.tabHeader}>
        <span className={styles.tabCount}>총 {list.length}개</span>
        <Button variant="primary" onClick={handleCreate}>
          콘텐츠 추가
        </Button>
      </div>
      <ul className={styles.list}>
        {list.map((c) => (
          <li key={c.id} className={styles.listItem}>
            <div className={styles.listItemMain}>
              <span className={styles.badge}>{c.contentType}</span>
              <span className={styles.badge}>{c.language}</span>
              <span className={styles.listItemTitle}>
                {c.body.slice(0, 50)}
                {c.body.length > 50 ? '…' : ''}
              </span>
            </div>
            <div className={styles.listItemActions}>
              <Button variant="ghost" onClick={() => handleEdit(c)}>
                수정
              </Button>
              <Button
                variant="ghost"
                onClick={() => handleDelete(c)}
                className={styles.dangerButton}
              >
                삭제
              </Button>
            </div>
          </li>
        ))}
      </ul>
      {list.length === 0 && (
        <p className={styles.placeholder}>콘텐츠가 없습니다. 추가해 보세요.</p>
      )}

      <Drawer
        open={drawerOpen}
        title={editing ? '콘텐츠 수정' : '콘텐츠 추가'}
        onClose={handleClose}
      >
        <form
          key={editing?.id ?? 'new'}
          onSubmit={handleSubmit}
          className={styles.form}
        >
          <Input
            label="순서"
            name="contentOrder"
            type="number"
            defaultValue={editing?.contentOrder ?? 0}
          />
          <Select
            label="콘텐츠 타입"
            name="contentType"
            options={contentTypeOpts}
            defaultValue={editing?.contentType ?? ''}
            required
          />
          <Select
            label="언어"
            name="language"
            options={languageOpts}
            defaultValue={editing?.language ?? 'KO'}
            required
          />
          <Select
            label="표시 모드"
            name="displayMode"
            options={displayModeOpts}
            defaultValue={editing?.displayMode ?? 'PARAGRAPH'}
          />
          <Textarea
            label="본문"
            name="body"
            rows={6}
            defaultValue={editing?.body ?? ''}
            required
          />
          <div className={styles.formActions}>
            <Button type="button" variant="ghost" onClick={handleClose}>
              취소
            </Button>
            <Button
              type="submit"
              variant="primary"
              disabled={mutations.create.isPending || mutations.update.isPending}
            >
              {editing ? '저장' : '추가'}
            </Button>
          </div>
        </form>
      </Drawer>
    </div>
  );
}
