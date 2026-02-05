import { useState } from 'react';
import { Button } from '../../components/ui/Button';
import { Drawer } from '../../components/ui/Drawer';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { Textarea } from '../../components/ui/Textarea';
import { useActions } from '../../domains/action/hooks/useActions';
import { useActionMutations } from '../../domains/action/hooks/useActionMutations';
import { useEffects } from '../../domains/effect/hooks/useEffects';
import { useEffectMutations } from '../../domains/effect/hooks/useEffectMutations';
import { useEnums } from '../../domains/enum/hooks/useEnums';
import type {
  Action,
  ActionCreateRequest,
  ActionUpdateRequest,
  Effect,
  EffectCreateRequest,
  EffectUpdateRequest,
} from '../../types/admin';
import styles from '../NodesPage.module.css';

interface ActionsTabProps {
  questId: string;
  nodeId: string;
}

export function ActionsTab({ questId, nodeId }: ActionsTabProps) {
  const { data: actionsList = [], isLoading: actionsLoading } = useActions(
    questId,
    nodeId
  );
  const actionMutations = useActionMutations(questId, nodeId);
  const [actionDrawerOpen, setActionDrawerOpen] = useState(false);
  const [editingAction, setEditingAction] = useState<Action | null>(null);
  const [expandedActionId, setExpandedActionId] = useState<string | null>(null);
  const [effectDrawerOpen, setEffectDrawerOpen] = useState(false);
  const [editingEffect, setEditingEffect] = useState<Effect | null>(null);
  const [effectActionId, setEffectActionId] = useState<string | null>(null);

  const actionTypeOptions = useEnums('actionType');
  const effectTypeOptions = useEnums('effectType');

  const actionTypeOpts =
    actionTypeOptions.data?.map((v) => ({ value: v, label: v })) ?? [];
  const effectTypeOpts =
    effectTypeOptions.data?.map((v) => ({ value: v, label: v })) ?? [];

  const handleAddAction = () => {
    setEditingAction(null);
    setActionDrawerOpen(true);
  };

  const handleEditAction = (a: Action) => {
    setEditingAction(a);
    setActionDrawerOpen(true);
  };

  const handleCloseActionDrawer = () => {
    setActionDrawerOpen(false);
    setEditingAction(null);
  };

  const handleActionSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const form = e.currentTarget;
    const actionType = (form.elements.namedItem('actionType') as HTMLSelectElement)
      ?.value as Action['actionType'];
    const prompt = (form.elements.namedItem('prompt') as HTMLInputElement)
      ?.value ?? '';
    const optionsStr = (form.elements.namedItem('options') as HTMLTextAreaElement)
      ?.value?.trim();
    let options: Record<string, unknown> | undefined;
    if (optionsStr) {
      try {
        options = JSON.parse(optionsStr) as Record<string, unknown>;
      } catch {
        return;
      }
    }

    if (editingAction) {
      const body: ActionUpdateRequest = { actionType, prompt, options };
      actionMutations.update.mutate(
        { actionId: editingAction.id, body },
        { onSuccess: handleCloseActionDrawer }
      );
    } else {
      const body: ActionCreateRequest = { actionType, prompt, options };
      actionMutations.create.mutate(body, { onSuccess: handleCloseActionDrawer });
    }
  };

  const handleDeleteAction = (a: Action) => {
    if (window.confirm(`액션 "${a.prompt.slice(0, 30)}…"을(를) 삭제할까요?`)) {
      actionMutations.remove.mutate(a.id);
    }
  };

  const handleAddEffect = (actionId: string) => {
    setEffectActionId(actionId);
    setEditingEffect(null);
    setEffectDrawerOpen(true);
  };

  const handleEditEffect = (actionId: string, e: Effect) => {
    setEffectActionId(actionId);
    setEditingEffect(e);
    setEffectDrawerOpen(true);
  };

  const handleCloseEffectDrawer = () => {
    setEffectDrawerOpen(false);
    setEditingEffect(null);
    setEffectActionId(null);
  };

  const handleEffectSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!effectActionId) return;
    const form = e.currentTarget;
    const effectType = (form.elements.namedItem('effectType') as HTMLSelectElement)
      ?.value as Effect['effectType'];
    const effectValueStr = (form.elements.namedItem(
      'effectValue'
    ) as HTMLTextAreaElement)?.value?.trim();
    let effectValue: Record<string, unknown> = {};
    if (effectValueStr) {
      try {
        effectValue = JSON.parse(effectValueStr) as Record<string, unknown>;
      } catch {
        return;
      }
    }

    const effectMutations = useEffectMutations(questId, nodeId, effectActionId);

    if (editingEffect) {
      const body: EffectUpdateRequest = { effectType, effectValue };
      effectMutations.update.mutate(
        { effectId: editingEffect.id, body },
        { onSuccess: handleCloseEffectDrawer }
      );
    } else {
      const body: EffectCreateRequest = { effectType, effectValue };
      effectMutations.create.mutate(body, {
        onSuccess: handleCloseEffectDrawer,
      });
    }
  };

  const handleDeleteEffect = (actionId: string, eff: Effect) => {
    if (
      window.confirm(
        `이펙트 "${eff.effectType}"을(를) 삭제할까요?`
      )
    ) {
      useEffectMutations(questId, nodeId, actionId).remove.mutate(eff.id);
    }
  };

  if (actionsLoading) {
    return <p className={styles.placeholder}>로딩 중…</p>;
  }

  return (
    <div className={styles.tabSection}>
      <div className={styles.tabHeader}>
        <span className={styles.tabCount}>총 {actionsList.length}개</span>
        <Button variant="primary" onClick={handleAddAction}>
          액션 추가
        </Button>
      </div>
      <ul className={styles.list}>
        {actionsList.map((a) => (
          <ActionItem
            key={a.id}
            questId={questId}
            nodeId={nodeId}
            action={a}
            isExpanded={expandedActionId === a.id}
            onToggleExpand={() =>
              setExpandedActionId((id) => (id === a.id ? null : a.id))
            }
            onEdit={() => handleEditAction(a)}
            onDelete={() => handleDeleteAction(a)}
            onAddEffect={() => handleAddEffect(a.id)}
            onEditEffect={(e) => handleEditEffect(a.id, e)}
            onDeleteEffect={(e) => handleDeleteEffect(a.id, e)}
          />
        ))}
      </ul>
      {actionsList.length === 0 && (
        <p className={styles.placeholder}>액션이 없습니다. 추가해 보세요.</p>
      )}

      <Drawer
        open={actionDrawerOpen}
        title={editingAction ? '액션 수정' : '액션 추가'}
        onClose={handleCloseActionDrawer}
      >
        <form
          key={editingAction?.id ?? 'new'}
          onSubmit={handleActionSubmit}
          className={styles.form}
        >
          <Select
            label="액션 타입"
            name="actionType"
            options={actionTypeOpts}
            defaultValue={editingAction?.actionType ?? ''}
            required
          />
          <Input
            label="프롬프트"
            name="prompt"
            defaultValue={editingAction?.prompt ?? ''}
            required
          />
          <Textarea
            label="옵션 (JSON)"
            name="options"
            rows={4}
            defaultValue={
              editingAction?.options
                ? JSON.stringify(editingAction.options, null, 2)
                : '{}'
            }
            className="code"
          />
          <div className={styles.formActions}>
            <Button type="button" variant="ghost" onClick={handleCloseActionDrawer}>
              취소
            </Button>
            <Button
              type="submit"
              variant="primary"
              disabled={
                actionMutations.create.isPending || actionMutations.update.isPending
              }
            >
              {editingAction ? '저장' : '추가'}
            </Button>
          </div>
        </form>
      </Drawer>

      {effectActionId && (
        <EffectDrawer
          questId={questId}
          nodeId={nodeId}
          actionId={effectActionId}
          editingEffect={editingEffect}
          open={effectDrawerOpen}
          onClose={handleCloseEffectDrawer}
          onSubmit={handleEffectSubmit}
          effectTypeOpts={effectTypeOpts}
        />
      )}
    </div>
  );
}

function ActionItem({
  questId,
  nodeId,
  action,
  isExpanded,
  onToggleExpand,
  onEdit,
  onDelete,
  onAddEffect,
  onEditEffect,
  onDeleteEffect,
}: {
  questId: string;
  nodeId: string;
  action: Action;
  isExpanded: boolean;
  onToggleExpand: () => void;
  onEdit: () => void;
  onDelete: () => void;
  onAddEffect: () => void;
  onEditEffect: (e: Effect) => void;
  onDeleteEffect: (e: Effect) => void;
}) {
  const { data: effectsList = [] } = useEffects(questId, nodeId, action.id, {
    enabled: isExpanded,
  });

  return (
    <li className={styles.listItem}>
      <div className={styles.listItemMain}>
        <button
          type="button"
          className={styles.expandBtn}
          onClick={onToggleExpand}
          aria-expanded={isExpanded}
        >
          {isExpanded ? '▼' : '▶'}
        </button>
        <span className={styles.badge}>{action.actionType}</span>
        <span className={styles.listItemTitle}>
          {action.prompt.slice(0, 40)}
          {action.prompt.length > 40 ? '…' : ''}
        </span>
      </div>
      <div className={styles.listItemActions}>
        <Button variant="ghost" onClick={onEdit}>
          수정
        </Button>
        <Button variant="ghost" onClick={onDelete} className={styles.dangerButton}>
          삭제
        </Button>
      </div>
      {isExpanded && (
        <div className={styles.effectsList}>
          <div className={styles.effectsHeader}>
            <span>이펙트 ({effectsList.length})</span>
            <Button variant="ghost" onClick={onAddEffect}>
              이펙트 추가
            </Button>
          </div>
          <ul className={styles.sublist}>
            {effectsList.map((e) => (
              <li key={e.id} className={styles.sublistItem}>
                <span className={styles.badge}>{e.effectType}</span>
                <span className={styles.listItemTitle}>
                  {JSON.stringify(e.effectValue).slice(0, 30)}…
                </span>
                <Button variant="ghost" onClick={() => onEditEffect(e)}>
                  수정
                </Button>
                <Button
                  variant="ghost"
                  onClick={() => onDeleteEffect(e)}
                  className={styles.dangerButton}
                >
                  삭제
                </Button>
              </li>
            ))}
          </ul>
        </div>
      )}
    </li>
  );
}

function EffectDrawer({
  questId,
  nodeId,
  actionId,
  editingEffect,
  open,
  onClose,
  onSubmit,
  effectTypeOpts,
}: {
  questId: string;
  nodeId: string;
  actionId: string;
  editingEffect: Effect | null;
  open: boolean;
  onClose: () => void;
  onSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
  effectTypeOpts: { value: string; label: string }[];
}) {
  const effectMutations = useEffectMutations(questId, nodeId, actionId);

  return (
    <Drawer
      open={open}
      title={editingEffect ? '이펙트 수정' : '이펙트 추가'}
      onClose={onClose}
    >
      <form onSubmit={onSubmit} className={styles.form}>
        <Select
          label="이펙트 타입"
          name="effectType"
          options={effectTypeOpts}
          defaultValue={editingEffect?.effectType ?? ''}
          required
        />
        <Textarea
          label="effectValue (JSON)"
          name="effectValue"
          rows={6}
          defaultValue={
            editingEffect?.effectValue
              ? JSON.stringify(editingEffect.effectValue, null, 2)
              : '{}'
          }
          className="code"
        />
        <div className={styles.formActions}>
          <Button type="button" variant="ghost" onClick={onClose}>
            취소
          </Button>
          <Button
            type="submit"
            variant="primary"
            disabled={
              effectMutations.create.isPending || effectMutations.update.isPending
            }
          >
            {editingEffect ? '저장' : '추가'}
          </Button>
        </div>
      </form>
    </Drawer>
  );
}
