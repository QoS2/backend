import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { useQuest } from '../domains/quest/hooks/useQuest';
import { useNodes } from '../domains/node/hooks/useNodes';
import { useNodeMutations } from '../domains/node/hooks/useNodeMutations';
import { Button } from '../components/ui/Button';
import { Drawer } from '../components/ui/Drawer';
import { Input } from '../components/ui/Input';
import { Select } from '../components/ui/Select';
import { Textarea } from '../components/ui/Textarea';
import { useEnums } from '../domains/enum/hooks/useEnums';
import { ContentsTab } from './nodes/ContentsTab';
import { ActionsTab } from './nodes/ActionsTab';
import { TransitionsTab } from './nodes/TransitionsTab';
import type { Node, NodeUpdateRequest } from '../types/admin';
import styles from './NodesPage.module.css';

export function NodesPage() {
  const { questId } = useParams<{ questId: string }>();
  const navigate = useNavigate();
  const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'contents' | 'actions' | 'transitions'>('contents');

  const quest = useQuest(questId ?? null);
  const nodes = useNodes(questId ?? null);
  const nodeMutations = useNodeMutations(questId ?? '');

  const nodeList = nodes.data ?? [];
  const selectedNode = nodeList.find((n) => n.id === selectedNodeId) ?? null;

  if (!questId) {
    return (
      <div className={styles.page}>
        <p>퀘스트를 선택해 주세요.</p>
        <Button variant="secondary" onClick={() => navigate('/quests')}>
          퀘스트 목록
        </Button>
      </div>
    );
  }

  if (quest.isLoading || !quest.data) {
    return (
      <div className={styles.page}>
        <div className={styles.skeleton}>로딩 중…</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <Button variant="ghost" onClick={() => navigate('/quests')}>
          ← 퀘스트
        </Button>
        <h1 className={styles.title}>{quest.data.title} – 노드</h1>
      </div>

      <div className={styles.split}>
        <div className={styles.left}>
          <NodeList
            questId={questId}
            nodes={nodeList}
            selectedNodeId={selectedNodeId}
            onSelect={setSelectedNodeId}
            onNodeAdded={setSelectedNodeId}
            onReorder={(newOrder) => {
              nodeMutations.reorder.mutate({
                nodes: newOrder.map((id, i) => ({ nodeId: id, orderIndex: i })),
              });
            }}
          />
        </div>
        <div className={styles.right}>
          {selectedNode ? (
            <NodeDetailPanel
              questId={questId}
              node={selectedNode}
              nodes={nodeList}
              activeTab={activeTab}
              onTabChange={setActiveTab}
              onUpdate={(body) =>
                nodeMutations.update.mutate({
                  nodeId: selectedNode.id,
                  body,
                })
              }
              isUpdating={nodeMutations.update.isPending}
            />
          ) : (
            <div className={styles.emptyDetail}>
              노드를 선택하거나 새 노드를 추가하세요.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function NodeList({
  questId,
  nodes,
  selectedNodeId,
  onSelect,
  onNodeAdded,
  onReorder,
}: {
  questId: string;
  nodes: Node[];
  selectedNodeId: string | null;
  onSelect: (id: string | null) => void;
  onNodeAdded: (nodeId: string) => void;
  onReorder: (orderedIds: string[]) => void;
}) {
  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
  );

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;
    const oldIndex = nodes.findIndex((n) => n.id === active.id);
    const newIndex = nodes.findIndex((n) => n.id === over.id);
    if (oldIndex === -1 || newIndex === -1) return;
    const reordered = arrayMove(nodes, oldIndex, newIndex);
    onReorder(reordered.map((n) => n.id));
  };

  return (
    <div className={styles.nodeList}>
      <h2 className={styles.panelTitle}>노드 목록</h2>
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragEnd={handleDragEnd}
      >
        <SortableContext
          items={nodes.map((n) => n.id)}
          strategy={verticalListSortingStrategy}
        >
          {nodes.map((node) => (
            <SortableNodeItem
              key={node.id}
              node={node}
              isSelected={selectedNodeId === node.id}
              onSelect={() => onSelect(node.id)}
            />
          ))}
        </SortableContext>
      </DndContext>
      <AddNodeButton questId={questId} onAdded={onNodeAdded} />
    </div>
  );
}

function SortableNodeItem({
  node,
  isSelected,
  onSelect,
}: {
  node: Node;
  isSelected: boolean;
  onSelect: () => void;
}) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: node.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`${styles.nodeItem} ${isSelected ? styles.nodeItemSelected : ''} ${isDragging ? styles.nodeItemDragging : ''}`}
      onClick={onSelect}
    >
      <span
        className={styles.dragHandle}
        {...attributes}
        {...listeners}
        onClick={(e) => e.stopPropagation()}
      >
        ⋮⋮
      </span>
      <span className={styles.nodeBadge}>{node.nodeType}</span>
      <span className={styles.nodeOrder}>{node.orderIndex}</span>
      <span className={styles.nodeTitle}>{node.title}</span>
    </div>
  );
}

function AddNodeButton({
  questId,
  onAdded,
}: {
  questId: string;
  onAdded: (newNodeId: string) => void;
}) {
  const [open, setOpen] = useState(false);
  const nodeOptions = useEnums('nodeType');
  const options = nodeOptions.data?.map((v) => ({ value: v, label: v })) ?? [];
  const createMutation = useNodeMutations(questId).create;

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const form = e.currentTarget;
    const title = (form.elements.namedItem('title') as HTMLInputElement)?.value;
    const nodeType = (form.elements.namedItem('nodeType') as HTMLSelectElement)?.value;
    const orderIndex = parseInt(
      (form.elements.namedItem('orderIndex') as HTMLInputElement)?.value ?? '0',
      10
    );
    if (!title || !nodeType) return;
    createMutation.mutate(
      { title, nodeType: nodeType as Node['nodeType'], orderIndex },
      {
        onSuccess: (newNode) => {
          setOpen(false);
          onAdded(newNode.id);
        },
      }
    );
  };

  return (
    <>
      <Button variant="primary" onClick={() => setOpen(true)} className={styles.addNodeBtn}>
        노드 추가
      </Button>
      <Drawer
        open={open}
        title="노드 추가"
        onClose={() => setOpen(false)}
      >
          <form onSubmit={handleSubmit} className={styles.form}>
            <Input label="제목" name="title" required />
            <Select label="노드 타입" options={options} name="nodeType" required />
            <Input label="순서" name="orderIndex" type="number" defaultValue={0} />
            <div className={styles.formActions}>
              <Button type="button" variant="ghost" onClick={() => setOpen(false)}>
                취소
              </Button>
              <Button type="submit" variant="primary" disabled={createMutation.isPending}>
                생성
              </Button>
            </div>
          </form>
      </Drawer>
    </>
  );
}

function NodeDetailPanel({
  questId,
  node,
  nodes,
  activeTab,
  onTabChange,
  onUpdate,
  isUpdating,
}: {
  questId: string;
  node: Node;
  nodes: Node[];
  activeTab: 'contents' | 'actions' | 'transitions';
  onTabChange: (tab: 'contents' | 'actions' | 'transitions') => void;
  onUpdate: (body: NodeUpdateRequest) => void;
  isUpdating: boolean;
}) {
  const [title, setTitle] = useState(node.title);
  const [nodeType, setNodeType] = useState(node.nodeType);
  const [orderIndex, setOrderIndex] = useState(node.orderIndex);
  const [unlockJson, setUnlockJson] = useState(
    () => JSON.stringify(node.unlockCondition ?? {}, null, 2)
  );

  const nodeTypeOptions = useEnums('nodeType');
  const nodeTypeSelectOptions =
    nodeTypeOptions.data?.map((v) => ({ value: v, label: v })) ?? [];

  const handleSave = () => {
    let unlock: Record<string, unknown> | undefined;
    try {
      unlock = JSON.parse(unlockJson);
    } catch {
      return;
    }
    onUpdate({
      title,
      nodeType: nodeType as Node['nodeType'],
      orderIndex,
      unlockCondition: unlock,
    });
  };

  return (
    <div className={styles.detailPanel}>
      <h2 className={styles.panelTitle}>노드 상세</h2>
      <div className={styles.detailForm}>
        <Input
          label="제목"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
        <Select
          label="노드 타입"
          options={nodeTypeSelectOptions}
          value={nodeType}
          onChange={(e) => setNodeType(e.target.value as Node['nodeType'])}
        />
        <Input
          label="순서"
          type="number"
          value={orderIndex}
          onChange={(e) => setOrderIndex(parseInt(e.target.value, 10) || 0)}
        />
        <Textarea
          label="Unlock Condition (JSON)"
          value={unlockJson}
          onChange={(e) => setUnlockJson(e.target.value)}
          rows={6}
          className="code"
        />
        <Button
          variant="primary"
          onClick={handleSave}
          disabled={isUpdating}
          className={styles.stickySave}
        >
          저장
        </Button>
      </div>
      <div className={styles.tabs}>
        {(['contents', 'actions', 'transitions'] as const).map((tab) => (
          <button
            key={tab}
            type="button"
            className={`${styles.tab} ${activeTab === tab ? styles.tabActive : ''}`}
            onClick={() => onTabChange(tab)}
          >
            {tab === 'contents' && '콘텐츠'}
            {tab === 'actions' && '액션'}
            {tab === 'transitions' && '전환'}
          </button>
        ))}
      </div>
      <div className={styles.tabContent}>
        {activeTab === 'contents' && (
          <ContentsTab questId={questId} nodeId={node.id} />
        )}
        {activeTab === 'actions' && (
          <ActionsTab questId={questId} nodeId={node.id} />
        )}
        {activeTab === 'transitions' && (
          <TransitionsTab questId={questId} nodeId={node.id} nodes={nodes} />
        )}
      </div>
    </div>
  );
}
