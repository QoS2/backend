import { useState } from 'react';
import { Button } from '../../components/ui/Button';
import { Drawer } from '../../components/ui/Drawer';
import { FileUploadInput } from '../../components/ui/FileUploadInput';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { Textarea } from '../../components/ui/Textarea';
import {
  useOutgoingTransitions,
  useIncomingTransitions,
} from '../../domains/transition/hooks/useTransitions';
import { useTransitionMutations } from '../../domains/transition/hooks/useTransitionMutations';
import { useEnums } from '../../domains/enum/hooks/useEnums';
import type {
  Node,
  Transition,
  TransitionCreateRequest,
  TransitionUpdateRequest,
} from '../../types/admin';
import styles from '../NodesPage.module.css';

interface TransitionsTabProps {
  questId: string;
  nodeId: string;
  nodes: Node[];
}

export function TransitionsTab({ questId, nodeId, nodes }: TransitionsTabProps) {
  const { data: outgoing = [], isLoading: outgoingLoading } =
    useOutgoingTransitions(questId, nodeId);
  const { data: incoming = [], isLoading: incomingLoading } =
    useIncomingTransitions(questId, nodeId);
  const mutations = useTransitionMutations(questId);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editing, setEditing] = useState<Transition | null>(null);
  const [mode, setMode] = useState<'outgoing' | 'incoming' | 'edit'>('outgoing');

  const messageTypeOptions = useEnums('transitionMessageType');
  const languageOptions = useEnums('language');

  const messageTypeOpts =
    messageTypeOptions.data?.map((v) => ({ value: v, label: v })) ?? [];
  const languageOpts =
    languageOptions.data?.map((v) => ({ value: v, label: v })) ?? [];

  const otherNodes = nodes.filter((n) => n.id !== nodeId).map((n) => ({
    value: n.id,
    label: `${n.title} (${n.nodeType})`,
  }));

  const handleAddOutgoing = () => {
    setEditing(null);
    setMode('outgoing');
    setDrawerOpen(true);
  };

  const handleAddIncoming = () => {
    setEditing(null);
    setMode('incoming');
    setDrawerOpen(true);
  };

  const handleEdit = (t: Transition) => {
    setEditing(t);
    setMode('edit');
    setDrawerOpen(true);
  };

  const handleClose = () => {
    setDrawerOpen(false);
    setEditing(null);
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const form = e.currentTarget;
    const transitionOrder = parseInt(
      (form.elements.namedItem('transitionOrder') as HTMLInputElement)?.value ??
        '0',
      10
    );
    const messageType = (form.elements.namedItem(
      'messageType'
    ) as HTMLSelectElement)?.value as Transition['messageType'];
    const textContent = (form.elements.namedItem(
      'textContent'
    ) as HTMLTextAreaElement)?.value?.trim();
    const audioUrl = (form.elements.namedItem('audioUrl') as HTMLInputElement)
      ?.value?.trim();
    const language = (form.elements.namedItem('language') as HTMLSelectElement)
      ?.value as Transition['language'];

    if (editing) {
      const body: TransitionUpdateRequest = {
        transitionOrder,
        messageType,
        textContent: textContent ?? undefined,
        audioUrl: audioUrl || undefined,
        language,
      };
      mutations.update.mutate(
        { transitionId: editing.id, body },
        { onSuccess: handleClose }
      );
    } else {
      let fromNodeId: string;
      let toNodeId: string;
      if (mode === 'outgoing') {
        fromNodeId = nodeId;
        toNodeId =
          (form.elements.namedItem('toNodeId') as HTMLSelectElement)?.value ?? '';
      } else {
        fromNodeId =
          (form.elements.namedItem('fromNodeId') as HTMLSelectElement)?.value ??
          '';
        toNodeId = nodeId;
      }
      if (!fromNodeId || !toNodeId) return;
      const body: TransitionCreateRequest = {
        fromNodeId,
        toNodeId,
        transitionOrder,
        messageType,
        textContent: textContent ?? undefined,
        audioUrl: audioUrl || undefined,
        language,
      };
      mutations.create.mutate(body, { onSuccess: handleClose });
    }
  };

  const handleDelete = (t: Transition) => {
    if (
      window.confirm(
        `전환 "${t.textContent?.slice(0, 20) ?? t.id}…"을(를) 삭제할까요?`
      )
    ) {
      mutations.remove.mutate(t.id);
    }
  };

  const getNodeTitle = (id: string) =>
    nodes.find((n) => n.id === id)?.title ?? id.slice(0, 8);

  if (outgoingLoading || incomingLoading) {
    return <p className={styles.placeholder}>로딩 중…</p>;
  }

  return (
    <div className={styles.tabSection}>
      <div className={styles.transitionBlocks}>
        <div className={styles.transitionBlock}>
          <div className={styles.tabHeader}>
            <h3 className={styles.transitionBlockTitle}>
              나가는 전환 ({outgoing.length})
            </h3>
            <Button variant="primary" onClick={handleAddOutgoing}>
              추가
            </Button>
          </div>
          <ul className={styles.list}>
            {outgoing.map((t) => (
              <li key={t.id} className={styles.listItem}>
                <span className={styles.listItemTitle}>
                  → {getNodeTitle(t.toNodeId)}
                  {t.textContent ? `: ${t.textContent.slice(0, 25)}…` : ''}
                </span>
                <div className={styles.listItemActions}>
                  <Button variant="ghost" onClick={() => handleEdit(t)}>
                    수정
                  </Button>
                  <Button
                    variant="ghost"
                    onClick={() => handleDelete(t)}
                    className={styles.dangerButton}
                  >
                    삭제
                  </Button>
                </div>
              </li>
            ))}
          </ul>
        </div>
        <div className={styles.transitionBlock}>
          <div className={styles.tabHeader}>
            <h3 className={styles.transitionBlockTitle}>
              들어오는 전환 ({incoming.length})
            </h3>
            <Button variant="primary" onClick={handleAddIncoming}>
              추가
            </Button>
          </div>
          <ul className={styles.list}>
            {incoming.map((t) => (
              <li key={t.id} className={styles.listItem}>
                <span className={styles.listItemTitle}>
                  {getNodeTitle(t.fromNodeId)} →
                  {t.textContent ? ` ${t.textContent.slice(0, 25)}…` : ''}
                </span>
                <div className={styles.listItemActions}>
                  <Button variant="ghost" onClick={() => handleEdit(t)}>
                    수정
                  </Button>
                  <Button
                    variant="ghost"
                    onClick={() => handleDelete(t)}
                    className={styles.dangerButton}
                  >
                    삭제
                  </Button>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <Drawer
        open={drawerOpen}
        title={
          editing
            ? '전환 수정'
            : mode === 'outgoing'
              ? '나가는 전환 추가'
              : '들어오는 전환 추가'
        }
        onClose={handleClose}
      >
        <form onSubmit={handleSubmit} className={styles.form}>
          {!editing && mode === 'outgoing' && (
            <Select
              label="도착 노드"
              name="toNodeId"
              options={otherNodes}
              required
            />
          )}
          {!editing && mode === 'incoming' && (
            <Select
              label="출발 노드"
              name="fromNodeId"
              options={otherNodes}
              required
            />
          )}
          <Input
            label="순서"
            name="transitionOrder"
            type="number"
            defaultValue={editing?.transitionOrder ?? 0}
          />
          <Select
            label="메시지 타입"
            name="messageType"
            options={messageTypeOpts}
            defaultValue={editing?.messageType ?? 'TEXT'}
          />
          <Select
            label="언어"
            name="language"
            options={languageOpts}
            defaultValue={editing?.language ?? 'KO'}
          />
          <Textarea
            label="텍스트"
            name="textContent"
            rows={3}
            defaultValue={editing?.textContent ?? ''}
          />
          <FileUploadInput
            label="오디오 URL"
            name="audioUrl"
            defaultValue={editing?.audioUrl ?? ''}
            type="audio"
            placeholder="파일 업로드 또는 URL 입력"
          />
          <div className={styles.formActions}>
            <Button type="button" variant="ghost" onClick={handleClose}>
              취소
            </Button>
            <Button
              type="submit"
              variant="primary"
              disabled={
                mutations.create.isPending || mutations.update.isPending
              }
            >
              {editing ? '저장' : '추가'}
            </Button>
          </div>
        </form>
      </Drawer>
    </div>
  );
}
