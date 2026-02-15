import { useState, useEffect, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { DndContext, closestCenter, type DragEndEvent } from '@dnd-kit/core';
import { SortableContext, useSortable, verticalListSortingStrategy, arrayMove } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { MapPin, Camera, Gem, Circle, GripVertical } from 'lucide-react';
import { syncRag } from '../api/rag';
import { uploadFile } from '../api/upload';
import {
  fetchTours,
  fetchTour,
  createTour,
  updateTour,
  deleteTour,
  fetchSpots,
  createSpot,
  updateSpot,
  deleteSpot,
  fetchTourDetail,
  fetchMarkers,
  fetchSpotGuide,
  fetchGuide,
  saveGuide,
  type TourAdminResponse,
  type TourCreateRequest,
  type SpotAdminResponse,
  type SpotCreateRequest,
  type SpotUpdateRequest,
  type GuideSaveRequest,
  type GuideLineRequest,
  type GuideAssetRequest,
} from '../api/tour';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { Input } from '../components/ui/Input';
import { Select } from '../components/ui/Select';
import { Textarea } from '../components/ui/Textarea';
import { FileUploadInput } from '../components/ui/FileUploadInput';
import { MapPicker } from '../components/Map/MapPicker';
import { SpotsMap } from '../components/Map/SpotsMap';
import { reverseGeocode, type ReverseGeocodeResult } from '../utils/geocode';
import { useToast } from '../context/ToastContext';
import styles from './ToursPage.module.css';

export function ToursPage() {
  const { showSuccess, showError } = useToast();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editingTourId, setEditingTourId] = useState<number | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<TourAdminResponse | null>(null);
  const [spotDrawerOpen, setSpotDrawerOpen] = useState(false);
  const [selectedTourId, setSelectedTourId] = useState<number | null>(null);
  const [previewTourId, setPreviewTourId] = useState<number | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ['admin', 'tours', page],
    queryFn: () => fetchTours(page, 20),
  });
  const { data: tourDetail } = useQuery({
    queryKey: ['admin', 'tour', editingTourId],
    queryFn: () => (editingTourId ? fetchTour(editingTourId) : null),
    enabled: !!editingTourId,
  });
  const { data: spots } = useQuery({
    queryKey: ['admin', 'tours', selectedTourId, 'spots'],
    queryFn: () => (selectedTourId ? fetchSpots(selectedTourId) : []),
    enabled: !!selectedTourId,
  });

  const createMutation = useMutation({
    mutationFn: (body: TourCreateRequest) => createTour(body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours'] });
      setDrawerOpen(false);
      showSuccess('투어가 생성되었습니다.');
    },
    onError: (e: Error) => showError(e.message),
  });
  const updateMutation = useMutation({
    mutationFn: ({ tourId, body }: { tourId: number; body: Parameters<typeof updateTour>[1] }) =>
      updateTour(tourId, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours'] });
      setDrawerOpen(false);
      setEditingTourId(null);
      showSuccess('투어가 수정되었습니다.');
    },
    onError: (e: Error) => showError(e.message),
  });
  const deleteMutation = useMutation({
    mutationFn: (tourId: number) => deleteTour(tourId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours'] });
      setDeleteTarget(null);
      showSuccess('투어가 삭제되었습니다.');
    },
    onError: (e: Error) => showError(e.message),
  });
  const ragSyncAllMutation = useMutation({
    mutationFn: () => syncRag(),
    onSuccess: (res) => {
      showSuccess(`전체 RAG 동기화 완료 (${res.embeddingsCount}개 임베딩)`);
    },
    onError: (e: Error) => showError(e.message),
  });
  const ragSyncTourMutation = useMutation({
    mutationFn: (tourId: number) => syncRag(tourId),
    onSuccess: (res) => {
      showSuccess(`지식 동기화 완료 (${res.embeddingsCount}개 임베딩)`);
    },
    onError: (e: Error) => showError(e.message),
  });

  const tours = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  const handleCreate = () => {
    setEditingTourId(null);
    setDrawerOpen(true);
  };

  const handleEdit = (tour: TourAdminResponse) => {
    setEditingTourId(tour.id);
    setDrawerOpen(true);
  };

  const handleOpenSpots = (tour: TourAdminResponse) => {
    setSelectedTourId(tour.id);
    setSpotDrawerOpen(true);
  };
  const handlePreview = (tour: TourAdminResponse) => setPreviewTourId(tour.id);

  const handleDelete = (tour: TourAdminResponse) => setDeleteTarget(tour);
  const handleDeleteConfirm = () => {
    if (deleteTarget) deleteMutation.mutate(deleteTarget.id);
  };

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Tours</h1>
        <div className={styles.actions}>
          <Button
            variant="secondary"
            onClick={() => ragSyncAllMutation.mutate()}
            disabled={ragSyncAllMutation.isPending}
          >
            {ragSyncAllMutation.isPending ? '동기화 중...' : '전체 RAG 동기화'}
          </Button>
          <Button onClick={handleCreate}>투어 추가</Button>
        </div>
      </div>

      {isLoading ? (
        <p>로딩 중...</p>
      ) : tours.length === 0 ? (
        <div className={styles.emptyState}>
          <MapPin size={48} strokeWidth={1.5} className={styles.emptyIcon} />
          <h3>등록된 투어가 없습니다</h3>
          <p>우측 상단 &quot;투어 추가&quot; 버튼으로 첫 번째 투어를 만들어보세요.</p>
          <Button onClick={handleCreate}>투어 추가</Button>
        </div>
      ) : (
        <div className={styles.tableWrapper}>
          <Table
            columns={[
            { key: 'externalKey', label: 'Key' },
            { key: 'titleEn', label: '제목' },
            {
              key: 'spots',
              label: 'Spots',
              render: (row: TourAdminResponse) => (
                <span className={styles.spotsCounts}>
                  Main: {row.mainCount} · Sub: {row.subCount} · Photo: {row.photoSpotsCount} · Treasure: {row.treasuresCount} · Missions: {row.missionsCount}
                </span>
              ),
            },
            {
              key: 'actions',
              label: '작업',
              render: (row: TourAdminResponse) => (
                <div className={styles.actions}>
                  <Button onClick={() => handleEdit(row)}>수정</Button>
                  <Button variant="secondary" onClick={() => handleOpenSpots(row)}>
                    Spots
                  </Button>
                  <Button variant="ghost" onClick={() => handlePreview(row)}>
                    미리보기
                  </Button>
                  <Button variant="danger" onClick={() => handleDelete(row)}>
                    삭제
                  </Button>
                </div>
              ),
            },
          ]}
            data={tours}
            keyExtractor={(row) => String(row.id)}
          />
        </div>
      )}

      {totalPages > 1 && (
        <div className={styles.pagination}>
          <Button disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
            이전
          </Button>
          <span>
            {page + 1} / {totalPages}
          </span>
          <Button disabled={page >= totalPages - 1} onClick={() => setPage((p) => p + 1)}>
            다음
          </Button>
        </div>
      )}

      {drawerOpen && (
        <TourFormDrawer
          tour={editingTourId ? tourDetail ?? undefined : undefined}
          editingTourId={editingTourId}
          onClose={() => {
            setDrawerOpen(false);
            setEditingTourId(null);
          }}
          onSubmit={(values) => {
            if (editingTourId) {
              updateMutation.mutate({
                tourId: editingTourId,
                body: values as { titleEn: string; descriptionEn?: string },
              });
            } else {
              createMutation.mutate(values as TourCreateRequest);
            }
          }}
          onRagSync={editingTourId ? () => ragSyncTourMutation.mutate(editingTourId) : undefined}
          ragSyncPending={ragSyncTourMutation.isPending}
          isSubmitting={createMutation.isPending || updateMutation.isPending}
        />
      )}

      {spotDrawerOpen && selectedTourId && (
        <SpotsDrawer
          tourId={selectedTourId}
          spots={spots ?? []}
          onClose={() => {
            setSpotDrawerOpen(false);
            setSelectedTourId(null);
          }}
          onRagSync={() => ragSyncTourMutation.mutate(selectedTourId)}
          ragSyncPending={ragSyncTourMutation.isPending}
        />
      )}

      {previewTourId && (
        <TourPreviewDrawer
          tourId={previewTourId}
          onClose={() => setPreviewTourId(null)}
        />
      )}

      {deleteTarget && (
        <Modal
          open={!!deleteTarget}
          title="투어 삭제"
          onClose={() => setDeleteTarget(null)}
          footer={
            <>
              <Button variant="secondary" onClick={() => setDeleteTarget(null)}>
                취소
              </Button>
              <Button variant="danger" onClick={handleDeleteConfirm}>
                삭제
              </Button>
            </>
          }
        >
          {deleteTarget.titleEn}을(를) 삭제하시겠습니까?
        </Modal>
      )}
    </div>
  );
}

type TourFormValues = TourCreateRequest | { titleEn: string; descriptionEn?: string };

function TourFormDrawer({
  tour,
  editingTourId,
  onClose,
  onSubmit,
  onRagSync,
  ragSyncPending,
  isSubmitting,
}: {
  tour?: TourAdminResponse;
  editingTourId: number | null;
  onClose: () => void;
  onSubmit: (v: TourFormValues) => void;
  onRagSync?: () => void;
  ragSyncPending?: boolean;
  isSubmitting: boolean;
}) {
  const [externalKey, setExternalKey] = useState(tour?.externalKey ?? '');
  const [titleEn, setTitleEn] = useState(tour?.titleEn ?? '');
  const [descriptionEn, setDescriptionEn] = useState(tour?.descriptionEn ?? '');

  useEffect(() => {
    if (tour) {
      setExternalKey(tour.externalKey ?? '');
      setTitleEn(tour.titleEn ?? '');
      setDescriptionEn(tour.descriptionEn ?? '');
    }
  }, [tour]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (tour) {
      onSubmit({ titleEn: titleEn.trim(), descriptionEn: descriptionEn.trim() || undefined });
    } else {
      onSubmit({
        externalKey: externalKey.trim(),
        titleEn: titleEn.trim(),
        descriptionEn: descriptionEn.trim() || undefined,
      });
    }
  };

  return (
    <div className={styles.drawer}>
      <div className={styles.drawerBackdrop} onClick={onClose} />
      <div className={styles.drawerPanel}>
        <h2>{tour ? '투어 수정' : '투어 추가'}</h2>
        <form onSubmit={handleSubmit}>
          <Input
            label="External Key"
            value={externalKey}
            onChange={(e) => setExternalKey(e.target.value)}
            disabled={!!tour}
            required
          />
          <Input
            label="제목"
            value={titleEn}
            onChange={(e) => setTitleEn(e.target.value)}
            required
          />
          <Textarea
            label="설명"
            value={descriptionEn}
            onChange={(e) => setDescriptionEn(e.target.value)}
            rows={4}
          />
          <div className={styles.formActions}>
            <Button type="button" variant="secondary" onClick={onClose}>
              취소
            </Button>
            {editingTourId && onRagSync && (
              <Button
                type="button"
                variant="ghost"
                onClick={onRagSync}
                disabled={ragSyncPending}
              >
                {ragSyncPending ? '동기화 중...' : '지식 동기화'}
              </Button>
            )}
            <Button type="submit" disabled={isSubmitting}>
              {tour ? '수정' : '생성'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}

function TourPreviewDrawer({ tourId, onClose }: { tourId: number; onClose: () => void }) {
  const { data: detail, isLoading } = useQuery({
    queryKey: ['api', 'tour', tourId, 'detail'],
    queryFn: () => fetchTourDetail(tourId),
    enabled: !!tourId,
  });
  const { data: markers } = useQuery({
    queryKey: ['api', 'tour', tourId, 'markers'],
    queryFn: () => fetchMarkers(tourId),
    enabled: !!tourId,
  });

  return (
    <div className={styles.drawer}>
      <div className={styles.drawerBackdrop} onClick={onClose} />
      <div className={styles.drawerPanel}>
        <h2>투어 미리보기 (사용자 화면)</h2>
        {isLoading ? (
          <p>로딩 중...</p>
        ) : detail ? (
          <div className={styles.previewSection}>
            <h3>{detail.title || '(제목 없음)'}</h3>
            {detail.description && <p className={styles.previewDesc}>{detail.description}</p>}
            {detail.counts && (
              <p className={styles.previewCounts}>
                Main: {detail.counts.main} / Sub: {detail.counts.sub} / Photo: {detail.counts.photo} / Treasure: {detail.counts.treasure} / Missions: {detail.counts.missions}
              </p>
            )}
            <p><strong>접근:</strong> {detail.access?.status} (hasAccess: {String(detail.access?.hasAccess)})</p>
            {detail.startSpot && (
              <p><strong>시작 스팟:</strong> {detail.startSpot.title} ({detail.startSpot.lat}, {detail.startSpot.lng})</p>
            )}
            {detail.actions && (
              <p><strong>액션:</strong> {detail.actions.primaryButton} {detail.actions.secondaryButton && `/ ${detail.actions.secondaryButton}`}</p>
            )}
            <h4 className={styles.previewSubtitle}>마커 ({markers?.length ?? 0}개)</h4>
            <ul className={styles.markerList}>
              {(markers ?? []).map((m) => (
                <li key={m.id}>
                  [{m.type}] {m.title} — {m.latitude}, {m.longitude} (반경 {m.radiusM}m)
                </li>
              ))}
            </ul>
        </div>
        ) : (
          <p>데이터를 불러올 수 없습니다.</p>
        )}
        <Button variant="secondary" onClick={onClose}>닫기</Button>
      </div>
    </div>
  );
}

const SPOT_TYPES = ['MAIN', 'SUB', 'PHOTO', 'TREASURE'] as const;

const SPOT_TYPE_CONFIG: Record<string, { Icon: typeof MapPin; label: string; color: string }> = {
  MAIN: { Icon: MapPin, label: 'MAIN', color: 'var(--color-accent)' },
  SUB: { Icon: Circle, label: 'SUB', color: '#22c55e' },
  PHOTO: { Icon: Camera, label: 'PHOTO', color: '#f59e0b' },
  TREASURE: { Icon: Gem, label: 'TREASURE', color: '#a855f7' },
};

function SortableSpotItem({
  spot,
  onEdit,
  onEditGuide,
  onDelete,
}: {
  spot: SpotAdminResponse;
  onEdit: () => void;
  onEditGuide: () => void;
  onDelete: () => void;
}) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: spot.id });

  const style: React.CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <li ref={setNodeRef} style={style} className={styles.spotItem}>
      <div className={styles.spotRow}>
        <button
          type="button"
          className={styles.dragHandle}
          {...attributes}
          {...listeners}
          aria-label="순서 변경"
        >
          <GripVertical size={16} />
        </button>
        <SpotItemContent
          spot={spot}
          onEdit={onEdit}
          onEditGuide={onEditGuide}
          onDelete={onDelete}
        />
      </div>
    </li>
  );
}

function SpotItemContent({
  spot,
  onEdit,
  onEditGuide,
  onDelete,
}: {
  spot: SpotAdminResponse;
  onEdit: () => void;
  onEditGuide: () => void;
  onDelete: () => void;
}) {
  const [guideOpen, setGuideOpen] = useState(false);
  const { data: guide, isLoading, isFetching } = useQuery({
    queryKey: ['api', 'spot', spot.id, 'guide'],
    queryFn: () => fetchSpotGuide(spot.id),
    enabled: guideOpen,
  });

  const cfg = SPOT_TYPE_CONFIG[spot.type] ?? { Icon: Circle, label: spot.type, color: 'var(--color-text-muted)' };
  const SpotIcon = cfg.Icon;
  return (
    <div className={styles.spotContent}>
      <div className={styles.spotLabelRow}>
        <span className={styles.spotLabel}>
          <span
            className={styles.spotTypeBadge}
            style={{ '--spot-type-color': cfg.color } as React.CSSProperties}
            title={cfg.label}
          >
            <SpotIcon size={12} strokeWidth={2.5} /> {cfg.label}
          </span>
          <span className={styles.spotTitle} title={spot.title}>
            {spot.orderIndex}. {spot.title}
          </span>
          {spot.latitude != null && (
            <span className={styles.spotCoords}>({spot.latitude}, {spot.longitude})</span>
          )}
        </span>
        <div className={styles.spotActions}>
          <Button variant="ghost" onClick={() => setGuideOpen((v) => !v)}>
            {guideOpen ? '가이드 접기' : '가이드'}
          </Button>
          <Button variant="ghost" onClick={onEditGuide}>가이드 편집</Button>
          <Button variant="secondary" onClick={onEdit}>수정</Button>
          <Button variant="danger" onClick={onDelete}>삭제</Button>
        </div>
      </div>
      {guideOpen && (
        <div className={styles.guidePreview}>
          {isLoading || isFetching ? (
            <p>가이드 로딩 중...</p>
          ) : guide?.segments?.length ? (
            <div>
              <p className={styles.guideTitle}>{guide.stepTitle}</p>
              {guide.segments.map((seg) => (
                <p key={seg.id} className={styles.guideSegment}>
                  {seg.textEn}
                  {seg.media?.length ? ` [미디어 ${seg.media.length}개]` : ''}
                </p>
              ))}
            </div>
          ) : (
            <p className={styles.guideEmpty}>가이드 콘텐츠가 없습니다.</p>
          )}
        </div>
      )}
    </div>
  );
}

function GuideEditor({
  tourId,
  spotId,
  spotTitle,
  onClose,
  onSuccess,
}: {
  tourId: number;
  spotId: number;
  spotTitle: string;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const { data: guide, isLoading, isError, refetch } = useQuery({
    queryKey: ['admin', 'guide', tourId, spotId],
    queryFn: () => fetchGuide(tourId, spotId),
  });
  const saveMutation = useMutation({
    mutationFn: (body: GuideSaveRequest) => saveGuide(tourId, spotId, body),
    onSuccess: () => {
      showSuccess('가이드가 저장되었습니다.');
      onSuccess();
      onClose();
    },
  });
  const { showSuccess, showError } = useToast();

  const [stepTitle, setStepTitle] = useState('');
  const [nextAction, setNextAction] = useState<string>('');
  const [lines, setLines] = useState<GuideLineRequest[]>([]);
  const [forceCreateMode, setForceCreateMode] = useState(false);
  const [assetUploading, setAssetUploading] = useState(false);
  const [pendingAddLineIdx, setPendingAddLineIdx] = useState<number | null>(null);
  const assetFileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!guide && !isError) return;
    setStepTitle(guide?.stepTitle || spotTitle);
    setNextAction(guide?.nextAction || '');
    setLines(
      guide && guide.lines.length > 0
        ? guide.lines.map((l) => ({
            text: l.text,
            assets: l.assets.map((a) => ({
              url: a.url,
              assetType: a.assetType as 'IMAGE' | 'AUDIO',
              usage: a.usage as 'ILLUSTRATION' | 'SCRIPT_AUDIO',
            })),
          }))
        : [{ text: '', assets: [] }]
    );
  }, [guide, spotTitle, isError]);

  const addLine = () => setLines((prev) => [...prev, { text: '', assets: [] }]);
  const removeLine = (idx: number) => setLines((prev) => prev.filter((_, i) => i !== idx));

  const updateLineText = (idx: number, text: string) =>
    setLines((prev) => prev.map((l, i) => (i === idx ? { ...l, text } : l)));

  const addAsset = (lineIdx: number, asset: GuideAssetRequest) =>
    setLines((prev) =>
      prev.map((l, i) =>
        i === lineIdx ? { ...l, assets: [...l.assets, asset] } : l
      )
    );
  const updateAssetUrl = (lineIdx: number, assetIdx: number, url: string) =>
    setLines((prev) =>
      prev.map((l, i) =>
        i === lineIdx
          ? {
              ...l,
              assets: l.assets.map((a, j) =>
                j === assetIdx ? { ...a, url } : a
              ),
            }
          : l
      )
    );
  const removeAsset = (lineIdx: number, assetIdx: number) =>
    setLines((prev) =>
      prev.map((l, i) =>
        i === lineIdx ? { ...l, assets: l.assets.filter((_, j) => j !== assetIdx) } : l
      )
    );

  const handleAddAssetFile = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    const lineIdx = pendingAddLineIdx;
    e.target.value = '';
    setPendingAddLineIdx(null);
    if (!file || lineIdx === null) return;
    const isImage = file.type.startsWith('image/');
    const isAudio = file.type.startsWith('audio/');
    if (!isImage && !isAudio) {
      showError('이미지 또는 오디오 파일만 업로드할 수 있습니다.');
      return;
    }
    const assetType = isImage ? 'IMAGE' : 'AUDIO';
    const usage = isImage ? 'ILLUSTRATION' : 'SCRIPT_AUDIO';
    setAssetUploading(true);
    try {
      const url = await uploadFile(file, isImage ? 'image' : 'audio');
      addAsset(lineIdx, { url, assetType, usage });
    } catch (err) {
      showError(err instanceof Error ? err.message : '업로드 실패');
    } finally {
      setAssetUploading(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const validLines = lines.filter((l) => l.text.trim());
    if (validLines.length === 0) {
      showError('최소 1개 문장이 필요합니다.');
      return;
    }
    saveMutation.mutate({
      language: 'ko',
      stepTitle: stepTitle.trim() || spotTitle,
      nextAction: nextAction.trim() || undefined,
      lines: validLines.map((l) => ({
        text: l.text.trim(),
        assets: l.assets.filter((a) => a.url.trim()),
      })),
    });
  };

  if (isLoading && !guide && !forceCreateMode) return <p>로딩 중...</p>;

  if (isError && !forceCreateMode) {
    return (
      <div className={styles.guideEditor}>
        <h3>가이드 편집: {spotTitle}</h3>
        <p className={styles.guideError}>
          가이드를 불러오지 못했습니다. 처음부터 작성하시겠습니까?
        </p>
        <div className={styles.formActions}>
          <Button variant="secondary" onClick={onClose}>
            취소
          </Button>
          <Button variant="secondary" onClick={() => refetch()}>
            다시 시도
          </Button>
          <Button
            variant="primary"
            onClick={() => {
              setStepTitle(spotTitle);
              setLines([{ text: '', assets: [] }]);
              setForceCreateMode(true);
            }}
          >
            처음부터 작성
          </Button>
        </div>
      </div>
    );
  }

  if (lines.length === 0 && !forceCreateMode) {
    return (
      <div className={styles.guideEditor}>
        <h3>가이드 편집: {spotTitle}</h3>
        <p>로딩 중...</p>
      </div>
    );
  }

  return (
    <div className={styles.guideEditor}>
      <h3>가이드 편집: {spotTitle}</h3>
      <form onSubmit={handleSubmit}>
        <Input
          label="Step 제목"
          value={stepTitle}
          onChange={(e) => setStepTitle(e.target.value)}
          placeholder={spotTitle}
        />
        <Select
          label="컨텐츠 후 버튼"
          value={nextAction}
          onChange={(e) => setNextAction(e.target.value)}
          options={[
            { value: '', label: '(선택 안 함)' },
            { value: 'NEXT', label: 'NEXT - 다음 컨텐츠' },
            { value: 'MISSION_CHOICE', label: 'MISSION_CHOICE - 게임 스타트/스킵' },
          ]}
        />
        <input
          ref={assetFileInputRef}
          type="file"
          accept="image/*,audio/*"
          className={styles.hiddenFileInput}
          onChange={handleAddAssetFile}
        />
        {lines.map((line, lineIdx) => (
          <div key={lineIdx} className={styles.guideLine}>
            <div className={styles.guideLineHeader}>
              <span className={styles.guideLineNum}>{lineIdx + 1}</span>
              <Button
                type="button"
                variant="ghost"
                onClick={() => removeLine(lineIdx)}
                disabled={lines.length <= 1}
              >
                삭제
              </Button>
            </div>
            <Textarea
              value={line.text}
              onChange={(e) => updateLineText(lineIdx, e.target.value)}
              rows={2}
              placeholder="가이드 문장"
            />
            <div className={styles.guideAssets}>
              {line.assets.map((asset, assetIdx) => (
                <div key={assetIdx} className={styles.guideAssetRow}>
                  <FileUploadInput
                    type={asset.assetType.toLowerCase() as 'image' | 'audio'}
                    value={asset.url}
                    onChange={(url) => updateAssetUrl(lineIdx, assetIdx, url)}
                    placeholder="URL 또는 파일 업로드"
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() => removeAsset(lineIdx, assetIdx)}
                  >
                    ×
                  </Button>
                </div>
              ))}
              <div className={styles.guideAssetAdd}>
                <Button
                  type="button"
                  variant="ghost"
                  disabled={assetUploading}
                  onClick={() => {
                    setPendingAddLineIdx(lineIdx);
                    assetFileInputRef.current?.click();
                  }}
                >
                  {assetUploading ? '업로드 중…' : '+ 에셋 추가'}
                </Button>
              </div>
            </div>
          </div>
        ))}
        <Button type="button" variant="secondary" onClick={addLine}>
          문장 추가
        </Button>
        <div className={styles.formActions}>
          <Button type="button" variant="secondary" onClick={onClose}>
            취소
          </Button>
          <Button type="submit" disabled={saveMutation.isPending}>
            저장
          </Button>
        </div>
      </form>
    </div>
  );
}

function SpotsDrawer({
  tourId,
  spots,
  onClose,
  onRagSync,
  ragSyncPending,
}: {
  tourId: number;
  spots: SpotAdminResponse[];
  onClose: () => void;
  onRagSync?: () => void;
  ragSyncPending?: boolean;
}) {
  const [formOpen, setFormOpen] = useState(false);
  const [editingSpot, setEditingSpot] = useState<SpotAdminResponse | null>(null);
  const [type, setType] = useState<string>('MAIN');
  const [orderIndex, setOrderIndex] = useState(spots.length + 1);
  const [title, setTitle] = useState('');
  const [titleKr, setTitleKr] = useState('');
  const [description, setDescription] = useState('');
  const [pronunciationUrl, setPronunciationUrl] = useState('');
  const [address, setAddress] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [radiusM, setRadiusM] = useState('60');
  const [geocodeResult, setGeocodeResult] = useState<ReverseGeocodeResult | null>(null);
  const [geocodeLoading, setGeocodeLoading] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<SpotAdminResponse | null>(null);
  const [editingGuideSpot, setEditingGuideSpot] = useState<SpotAdminResponse | null>(null);
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useToast();

  const createMutation = useMutation({
    mutationFn: (body: SpotCreateRequest) => createSpot(tourId, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours', tourId, 'spots'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours'] });
      setFormOpen(false);
      resetForm();
      showSuccess('Spot이 추가되었습니다.');
    },
    onError: (e: Error) => showError(e.message),
  });
  const updateMutation = useMutation({
    mutationFn: ({ spotId, body }: { spotId: number; body: SpotUpdateRequest }) =>
      updateSpot(tourId, spotId, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours', tourId, 'spots'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours'] });
      setFormOpen(false);
      setEditingSpot(null);
      resetForm();
      showSuccess('Spot이 수정되었습니다.');
    },
    onError: (e: Error) => showError(e.message),
  });
  const deleteMutation = useMutation({
    mutationFn: (spotId: number) => deleteSpot(tourId, spotId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours', tourId, 'spots'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours'] });
      queryClient.invalidateQueries({ queryKey: ['api', 'spot'] });
      setDeleteTarget(null);
      setEditingGuideSpot(null);
      showSuccess('Spot이 삭제되었습니다.');
    },
    onError: (e: Error) => showError(e.message),
  });

  function resetForm() {
    setType('MAIN');
    setOrderIndex((spots?.length ?? 0) + 1);
    setTitle('');
    setTitleKr('');
    setDescription('');
    setPronunciationUrl('');
    setAddress('');
    setLatitude('');
    setLongitude('');
    setRadiusM('60');
    setGeocodeResult(null);
  }

  const handleAddSpot = () => {
    setEditingSpot(null);
    resetForm();
    setFormOpen(true);
  };
  const handleEditSpot = (spot: SpotAdminResponse) => {
    setEditingSpot(spot);
    setTitle(spot.title ?? '');
    setTitleKr(spot.titleKr ?? '');
    setDescription(spot.description ?? '');
    setPronunciationUrl(spot.pronunciationUrl ?? '');
    setAddress(spot.address ?? '');
    setOrderIndex(spot.orderIndex);
    setLatitude(spot.latitude?.toString() ?? '');
    setLongitude(spot.longitude?.toString() ?? '');
    setRadiusM(spot.radiusM?.toString() ?? '60');
    setGeocodeResult(null);
    setFormOpen(true);
  };

  const handleMapSelect = async (lat: number, lng: number) => {
    setLatitude(lat.toFixed(6));
    setLongitude(lng.toFixed(6));
    setGeocodeLoading(true);
    setGeocodeResult(null);
    try {
      const result = await reverseGeocode(lat, lng);
      if (result) {
        setGeocodeResult(result);
        if (!editingSpot && !title.trim()) {
          setTitle(result.name);
        }
        if (!editingSpot && !address.trim()) {
          setAddress(result.displayName);
        }
        if (result.suggestedRadiusM != null && !editingSpot) {
          setRadiusM(String(result.suggestedRadiusM));
        }
      }
    } finally {
      setGeocodeLoading(false);
    }
  };
  const handleSpotDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;
    const oldIndex = spots.findIndex((s) => s.id === active.id);
    const newIndex = spots.findIndex((s) => s.id === over.id);
    if (oldIndex === -1 || newIndex === -1) return;
    const reordered = arrayMove([...spots], oldIndex, newIndex);
    const updates = reordered
      .map((s, i) => (s.orderIndex !== i ? { spotId: s.id, orderIndex: i } : null))
      .filter((u): u is { spotId: number; orderIndex: number } => u != null);
    if (updates.length === 0) return;
    try {
      await Promise.all(
        updates.map((u) =>
          updateSpot(tourId, u.spotId, { orderIndex: u.orderIndex })
        )
      );
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours', tourId, 'spots'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours'] });
      showSuccess('순서가 변경되었습니다.');
    } catch (e) {
      showError(e instanceof Error ? e.message : '순서 변경에 실패했습니다.');
    }
  };

  const handleSubmitSpot = (e: React.FormEvent) => {
    e.preventDefault();
    if (editingSpot) {
      updateMutation.mutate({
        spotId: editingSpot.id,
        body: {
          title: title.trim(),
          titleKr: titleKr.trim() || undefined,
          description: description.trim() || undefined,
          pronunciationUrl: pronunciationUrl.trim() || undefined,
          address: address.trim() || undefined,
          orderIndex,
          latitude: latitude ? parseFloat(latitude) : undefined,
          longitude: longitude ? parseFloat(longitude) : undefined,
          radiusM: radiusM ? parseInt(radiusM, 10) : undefined,
        },
      });
    } else {
      createMutation.mutate({
        type,
        title: title.trim(),
        titleKr: titleKr.trim() || undefined,
        description: description.trim() || undefined,
        pronunciationUrl: pronunciationUrl.trim() || undefined,
        address: address.trim() || undefined,
        orderIndex,
        latitude: latitude ? parseFloat(latitude) : undefined,
        longitude: longitude ? parseFloat(longitude) : undefined,
        radiusM: radiusM ? parseInt(radiusM, 10) : 60,
      });
    }
  };

  return (
    <div className={styles.drawer}>
      <div className={styles.drawerBackdrop} onClick={onClose} />
      <div
        className={
          spots.length > 0 && !editingGuideSpot && !formOpen
            ? `${styles.spotsDrawerPanel} ${styles.spotsDrawerPanelWithMap}`
            : styles.spotsDrawerPanel
        }
      >
        <div className={styles.spotsDrawerMain}>
          <div className={styles.spotsDrawerHeader}>
            <h2>Spots</h2>
            {onRagSync && (
              <Button
                variant="ghost"
                onClick={onRagSync}
                disabled={ragSyncPending}
              >
                {ragSyncPending ? '동기화 중...' : '지식 동기화'}
              </Button>
            )}
          </div>
        {editingGuideSpot ? (
          <GuideEditor
            tourId={tourId}
            spotId={editingGuideSpot.id}
            spotTitle={editingGuideSpot.title}
            onClose={() => setEditingGuideSpot(null)}
            onSuccess={() => {
              queryClient.invalidateQueries({ queryKey: ['admin', 'guide', tourId] });
              queryClient.invalidateQueries({ queryKey: ['api', 'spot'] });
            }}
          />
        ) : !formOpen ? (
          spots.length === 0 ? (
            <div className={styles.emptyState}>
              <MapPin size={40} strokeWidth={1.5} className={styles.emptyIcon} />
              <h3>아직 Spot이 없습니다</h3>
              <p>아래 버튼으로 이 투어의 첫 번째 Spot을 추가하세요.</p>
              <Button onClick={handleAddSpot}>Spot 추가</Button>
            </div>
          ) : (
            <Button onClick={handleAddSpot}>Spot 추가</Button>
          )
        ) : (
          <form onSubmit={handleSubmitSpot}>
            {!editingSpot && (
              <div className={styles.formRow}>
                <label>Type</label>
                <select value={type} onChange={(e) => setType(e.target.value)}>
                  {SPOT_TYPES.map((t) => (
                    <option key={t} value={t}>{t}</option>
                  ))}
                </select>
              </div>
            )}
            {editingSpot && <p className={styles.formHint}>Type: {editingSpot.type} (수정 불가)</p>}
            <Input label="Order" type="number" value={String(orderIndex)} onChange={(e) => setOrderIndex(parseInt(e.target.value, 10) || 1)} />
            <Input label="제목" value={title} onChange={(e) => setTitle(e.target.value)} required placeholder="지도에서 클릭 시 자동 입력" />
            <Input label="한글 제목" value={titleKr} onChange={(e) => setTitleKr(e.target.value)} placeholder="예: 광화문" />
            <Input label="설명" value={description} onChange={(e) => setDescription(e.target.value)} placeholder="가이드에서 안내할 내용" />
            <Input label="발음 URL" value={pronunciationUrl} onChange={(e) => setPronunciationUrl(e.target.value)} placeholder="https://..." />
            <Input label="주소" value={address} onChange={(e) => setAddress(e.target.value)} placeholder="지도 클릭 시 자동 입력" />
            <div className={styles.formSection}>
              <label className={styles.formSectionLabel}>위치 (지도에서 클릭하여 설정)</label>
              <MapPicker
                lat={latitude ? parseFloat(latitude) : undefined}
                lng={longitude ? parseFloat(longitude) : undefined}
                onSelect={handleMapSelect}
                height={220}
              />
              {(geocodeLoading || (geocodeResult && !geocodeLoading)) && (
                <div className={styles.geocodeSection}>
                  {geocodeLoading && (
                    <p className={styles.geocodeHint}>위치 정보 조회 중...</p>
                  )}
                  {geocodeResult && !geocodeLoading && (
                    <div className={styles.geocodeResult}>
                      <p className={styles.geocodeAddress}>📍 {geocodeResult.displayName}</p>
                      <div className={styles.geocodeActions}>
                        <Button
                          type="button"
                          variant="ghost"
                          onClick={() => setTitle(geocodeResult.name)}
                        >
                          제목에 반영
                        </Button>
                        {geocodeResult.suggestedRadiusM != null && (
                          <Button
                            type="button"
                            variant="ghost"
                            onClick={() => setRadiusM(String(geocodeResult.suggestedRadiusM))}
                          >
                            반경 {geocodeResult.suggestedRadiusM}m
                          </Button>
                        )}
                        <Button
                          type="button"
                          variant="ghost"
                          onClick={() =>
                            setDescription((prev) =>
                              prev.trim()
                                ? `${prev}\n\n📍 ${geocodeResult.displayName}`
                                : `📍 ${geocodeResult.displayName}`
                            )
                          }
                        >
                          설명에 주소 추가
                        </Button>
                      </div>
                    </div>
                  )}
                </div>
              )}
              <div className={styles.coordInputs}>
                <Input label="위도" value={latitude} onChange={(e) => setLatitude(e.target.value)} placeholder="37.5665" />
                <Input label="경도" value={longitude} onChange={(e) => setLongitude(e.target.value)} placeholder="126.978" />
              </div>
            </div>
            <Input label="반경 (m)" type="number" value={radiusM} onChange={(e) => setRadiusM(e.target.value)} />
            <div className={styles.formActions}>
              <Button type="button" variant="secondary" onClick={() => { setFormOpen(false); setEditingSpot(null); }}>취소</Button>
              <Button type="submit" disabled={createMutation.isPending || updateMutation.isPending}>
                {editingSpot ? '수정' : '추가'}
              </Button>
            </div>
          </form>
        )}
        {!editingGuideSpot && spots.length > 0 && (
          <>
            <h3 className={styles.listSectionTitle}>Spot 목록 ({spots.length}개)</h3>
            <DndContext
            collisionDetection={closestCenter}
            onDragEnd={(e) => handleSpotDragEnd(e)}
          >
            <SortableContext
              items={spots.map((s) => s.id)}
              strategy={verticalListSortingStrategy}
            >
              <ul className={styles.stepList}>
                {spots.map((s) => (
                  <SortableSpotItem
                    key={s.id}
                    spot={s}
                    onEdit={() => handleEditSpot(s)}
                    onEditGuide={() => setEditingGuideSpot(s)}
                    onDelete={() => setDeleteTarget(s)}
                  />
                ))}
              </ul>
            </SortableContext>
          </DndContext>
          </>
        )}
        <Button variant="secondary" onClick={onClose}>닫기</Button>
        </div>
        {spots.length > 0 && !editingGuideSpot && !formOpen && (
          <div className={styles.spotsDrawerMap}>
            <h3 className={styles.mapSectionTitle}>위치 미리보기</h3>
            <SpotsMap spots={spots} />
          </div>
        )}
      </div>
      {deleteTarget && (
        <Modal
          open={!!deleteTarget}
          title="Spot 삭제"
          onClose={() => setDeleteTarget(null)}
          footer={
            <>
              <Button variant="secondary" onClick={() => setDeleteTarget(null)}>취소</Button>
              <Button
                variant="danger"
                onClick={() => deleteMutation.mutate(deleteTarget.id)}
                disabled={deleteMutation.isPending}
              >
                {deleteMutation.isPending ? '삭제 중...' : '삭제'}
              </Button>
            </>
          }
        >
          {deleteTarget.title}을(를) 삭제하시겠습니까?
        </Modal>
      )}
    </div>
  );
}
