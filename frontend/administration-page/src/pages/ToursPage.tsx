import { useState, useEffect, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { DndContext, closestCenter, type DragEndEvent } from '@dnd-kit/core';
import { SortableContext, useSortable, verticalListSortingStrategy, arrayMove } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { MapPin, GripVertical, X } from 'lucide-react';
import { syncRag } from '../api/rag';
import { uploadFile, deleteUploadedFile } from '../api/upload';
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
  fetchGuideSteps,
  saveGuideSteps,
  fetchTourAssets,
  addTourAsset,
  deleteTourAsset,
  fetchSpotAssets,
  addSpotAsset,
  deleteSpotAsset,
  fetchMissionSteps,
  createMissionStep,
  updateMissionStep,
  deleteMissionStep,
  type TourAdminResponse,
  type TourCreateRequest,
  type SpotAdminResponse,
  type SpotCreateRequest,
  type SpotUpdateRequest,
  type GuideStepsSaveRequest,
  type GuideStepSaveRequest,
  type GuideLineRequest,
  type GuideAssetRequest,
  type TourAssetRequest,
  type SpotAssetRequest,
  type MissionStepResponse,
  type MissionStepCreateRequest,
  type MissionStepUpdateRequest,
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
import { useToast } from '../hooks/useToast';
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
  const [tourAssetsDrawerOpen, setTourAssetsDrawerOpen] = useState(false);
  const [tourAssetsTour, setTourAssetsTour] = useState<{ id: number; title: string } | null>(null);

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
  const handleOpenTourAssets = (tour: TourAdminResponse) => {
    setTourAssetsTour({ id: tour.id, title: tour.titleEn });
    setTourAssetsDrawerOpen(true);
  };
  const handlePreview = (tour: TourAdminResponse) => setPreviewTourId(tour.id);

  const handleDelete = (tour: TourAdminResponse) => setDeleteTarget(tour);
  const handleDeleteConfirm = () => {
    if (deleteTarget) deleteMutation.mutate(deleteTarget.id);
  };

  return (
    <div className={styles.page}>
      <div className={styles.header}>
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
                <div className={styles.rowActions}>
                  <Button
                    variant="secondary"
                    className={styles.rowPrimaryAction}
                    onClick={() => handleEdit(row)}
                  >
                    투어 수정
                  </Button>
                  <Button
                    className={styles.rowPrimaryAction}
                    onClick={() => handleOpenSpots(row)}
                  >
                    Spots
                  </Button>
                  <Button
                    variant="ghost"
                    className={styles.rowPrimaryAction}
                    onClick={() => handleOpenTourAssets(row)}
                  >
                    이미지
                  </Button>
                  <Button
                    variant="ghost"
                    className={styles.rowPrimaryAction}
                    onClick={() => handlePreview(row)}
                  >
                    미리보기
                  </Button>
                  <Button
                    variant="danger"
                    className={styles.rowPrimaryAction}
                    onClick={() => handleDelete(row)}
                  >
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
          key={`${editingTourId ?? 'new'}-${tourDetail?.id ?? 'pending'}`}
          tour={editingTourId ? tourDetail ?? undefined : undefined}
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

      {tourAssetsDrawerOpen && tourAssetsTour && (
        <TourAssetsDrawer
          tourId={tourAssetsTour.id}
          tourTitle={tourAssetsTour.title}
          onClose={() => {
            setTourAssetsDrawerOpen(false);
            setTourAssetsTour(null);
          }}
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
  onClose,
  onSubmit,
  onRagSync,
  ragSyncPending,
  isSubmitting,
}: {
  tour?: TourAdminResponse;
  onClose: () => void;
  onSubmit: (v: TourFormValues) => void;
  onRagSync?: () => void;
  ragSyncPending?: boolean;
  isSubmitting: boolean;
}) {
  const [externalKey, setExternalKey] = useState(tour?.externalKey ?? '');
  const [titleEn, setTitleEn] = useState(tour?.titleEn ?? '');
  const [descriptionEn, setDescriptionEn] = useState(tour?.descriptionEn ?? '');

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
        <form onSubmit={handleSubmit} className={styles.tourForm}>
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
          <div className={`${styles.formActions} ${styles.tourFormActions}`}>
            <Button type="button" variant="secondary" onClick={onClose}>
              취소
            </Button>
            {tour && onRagSync && (
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

const TOUR_ASSET_USAGES = [
  { value: 'THUMBNAIL', label: '썸네일' },
  { value: 'HERO_IMAGE', label: '히어로 이미지' },
  { value: 'GALLERY_IMAGE', label: '갤러리 이미지' },
] as const;

const SPOT_ASSET_USAGES = [
  { value: 'THUMBNAIL', label: '썸네일 (mainPlaceThumbnails)' },
  { value: 'HERO_IMAGE', label: '히어로 이미지' },
  { value: 'GALLERY_IMAGE', label: '갤러리 이미지' },
] as const;

const AUDIO_URL_PATTERN = /\.(mp3|wav|ogg|m4a|aac|flac)(?:[?#].*)?$/i;

function isAudioUrl(url: string): boolean {
  const trimmed = url.trim();
  if (!trimmed) return false;
  if (trimmed.toLowerCase().startsWith('data:audio/')) return true;
  return AUDIO_URL_PATTERN.test(trimmed);
}

function SpotAssetsDrawer({
  tourId,
  spotId,
  spotTitle,
  onClose,
}: {
  tourId: number;
  spotId: number;
  spotTitle: string;
  onClose: () => void;
}) {
  const { showSuccess, showError } = useToast();
  const queryClient = useQueryClient();
  const [usage, setUsage] = useState('THUMBNAIL');
  const [caption, setCaption] = useState('');
  const [assetUploading, setAssetUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const { data: assets = [], isLoading } = useQuery({
    queryKey: ['admin', 'spot', tourId, spotId, 'assets'],
    queryFn: () => fetchSpotAssets(tourId, spotId),
    enabled: !!tourId && !!spotId,
  });

  const addMutation = useMutation({
    mutationFn: (body: SpotAssetRequest) => addSpotAsset(tourId, spotId, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'spot', tourId, spotId, 'assets'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours'] });
      setCaption('');
      showSuccess('에셋이 추가되었습니다.');
    },
    onError: (e: Error) => showError(e.message),
  });

  const deleteMutation = useMutation({
    mutationFn: (spotAssetId: number) => deleteSpotAsset(tourId, spotId, spotAssetId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'spot', tourId, spotId, 'assets'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours'] });
      showSuccess('에셋이 삭제되었습니다.');
    },
    onError: (e: Error) => showError(e.message),
  });

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file || !file.type.startsWith('image/')) {
      showError('이미지 파일만 업로드할 수 있습니다.');
      return;
    }
    setAssetUploading(true);
    try {
      const url = await uploadFile(file, 'image', 'spot');
      addMutation.mutate({ url, usage, caption: caption.trim() || undefined });
    } catch (err) {
      showError(err instanceof Error ? err.message : '업로드 실패');
    } finally {
      setAssetUploading(false);
    }
  };

  return (
    <div className={styles.drawer}>
      <div className={styles.drawerBackdrop} onClick={onClose} />
      <div className={styles.drawerPanel}>
        <h2>스팟 이미지 — {spotTitle}</h2>
        <p className={styles.tourAssetsDesc}>
          메인 플레이스별 썸네일(mainPlaceThumbnails)에 사용됩니다. THUMBNAIL이 우선 사용됩니다.
        </p>

        <div className={styles.tourAssetsAdd}>
          <Select
            label="용도"
            value={usage}
            onChange={(e) => setUsage(e.target.value)}
            options={SPOT_ASSET_USAGES.map((u) => ({ value: u.value, label: u.label }))}
          />
          <Input
            label="캡션 (선택)"
            value={caption}
            onChange={(e) => setCaption(e.target.value)}
            placeholder="설명"
          />
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp"
            onChange={handleFileSelect}
            className={styles.hiddenFileInput}
          />
          <Button
            type="button"
            variant="secondary"
            onClick={() => fileInputRef.current?.click()}
            disabled={assetUploading}
          >
            {assetUploading ? '업로드 중…' : '+ 이미지 추가'}
          </Button>
        </div>

        {isLoading ? (
          <p>로딩 중...</p>
        ) : assets.length === 0 ? (
          <p className={styles.tourAssetsEmpty}>등록된 이미지가 없습니다.</p>
        ) : (
          <div className={styles.tourAssetsList}>
            {assets.map((a) => (
              <div key={a.id} className={styles.tourAssetItem}>
                {isAudioUrl(a.url) ? (
                  <audio controls src={a.url} className={styles.tourAssetAudio} />
                ) : (
                  <img src={a.url} alt={a.caption ?? a.usage} className={styles.tourAssetThumb} referrerPolicy="no-referrer" />
                )}
                <div className={styles.tourAssetMeta}>
                  <span className={styles.tourAssetUsage}>{a.usage}</span>
                  {a.caption && <span className={styles.tourAssetCaption}>{a.caption}</span>}
                </div>
                <Button
                  type="button"
                  variant="danger"
                  onClick={() => deleteMutation.mutate(a.id)}
                  disabled={deleteMutation.isPending}
                >
                  삭제
                </Button>
              </div>
            ))}
          </div>
        )}

        <div className={styles.formActions} style={{ marginTop: 16 }}>
          <Button variant="secondary" onClick={onClose}>
            닫기
          </Button>
        </div>
      </div>
    </div>
  );
}

function TourAssetsDrawer({
  tourId,
  tourTitle,
  onClose,
}: {
  tourId: number;
  tourTitle: string;
  onClose: () => void;
}) {
  const { showSuccess, showError } = useToast();
  const queryClient = useQueryClient();
  const [usage, setUsage] = useState('THUMBNAIL');
  const [caption, setCaption] = useState('');
  const [assetUploading, setAssetUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const { data: assets = [], isLoading } = useQuery({
    queryKey: ['admin', 'tour', tourId, 'assets'],
    queryFn: () => fetchTourAssets(tourId),
    enabled: !!tourId,
  });

  const addMutation = useMutation({
    mutationFn: (body: TourAssetRequest) => addTourAsset(tourId, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'tour', tourId, 'assets'] });
      setCaption('');
      showSuccess('에셋이 추가되었습니다.');
    },
    onError: (e: Error) => showError(e.message),
  });

  const deleteMutation = useMutation({
    mutationFn: (tourAssetId: number) => deleteTourAsset(tourId, tourAssetId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'tour', tourId, 'assets'] });
      showSuccess('에셋이 삭제되었습니다.');
    },
    onError: (e: Error) => showError(e.message),
  });

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file || !file.type.startsWith('image/')) {
      showError('이미지 파일만 업로드할 수 있습니다.');
      return;
    }
    setAssetUploading(true);
    try {
      const url = await uploadFile(file, 'image', 'tour');
      addMutation.mutate({ url, usage, caption: caption.trim() || undefined });
    } catch (err) {
      showError(err instanceof Error ? err.message : '업로드 실패');
    } finally {
      setAssetUploading(false);
    }
  };

  return (
    <div className={styles.drawer}>
      <div className={styles.drawerBackdrop} onClick={onClose} />
      <div className={styles.drawerPanel}>
        <h2>투어 이미지 — {tourTitle}</h2>
        <p className={styles.tourAssetsDesc}>
          투어 디테일 페이지 캐러셀에 사용됩니다. tour_assets가 없으면 메인 플레이스 이미지가 표시됩니다.
        </p>

        <div className={styles.tourAssetsAdd}>
          <Select
            label="용도"
            value={usage}
            onChange={(e) => setUsage(e.target.value)}
            options={TOUR_ASSET_USAGES.map((u) => ({ value: u.value, label: u.label }))}
          />
          <Input
            label="캡션 (선택)"
            value={caption}
            onChange={(e) => setCaption(e.target.value)}
            placeholder="설명"
          />
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp"
            onChange={handleFileSelect}
            className={styles.hiddenFileInput}
          />
          <Button
            type="button"
            variant="secondary"
            onClick={() => fileInputRef.current?.click()}
            disabled={assetUploading}
          >
            {assetUploading ? '업로드 중…' : '+ 이미지 추가'}
          </Button>
        </div>

        {isLoading ? (
          <p>로딩 중...</p>
        ) : assets.length === 0 ? (
          <p className={styles.tourAssetsEmpty}>등록된 이미지가 없습니다.</p>
        ) : (
          <div className={styles.tourAssetsList}>
            {assets.map((a) => (
              <div key={a.id} className={styles.tourAssetItem}>
                {isAudioUrl(a.url) ? (
                  <audio controls src={a.url} className={styles.tourAssetAudio} />
                ) : (
                  <img src={a.url} alt={a.caption ?? a.usage} className={styles.tourAssetThumb} referrerPolicy="no-referrer" />
                )}
                <div className={styles.tourAssetMeta}>
                  <span className={styles.tourAssetUsage}>{a.usage}</span>
                  {a.caption && <span className={styles.tourAssetCaption}>{a.caption}</span>}
                </div>
                <Button
                  type="button"
                  variant="danger"
                  onClick={() => deleteMutation.mutate(a.id)}
                  disabled={deleteMutation.isPending}
                >
                  삭제
                </Button>
              </div>
            ))}
          </div>
        )}

        <div className={styles.formActions} style={{ marginTop: 16 }}>
          <Button variant="secondary" onClick={onClose}>
            닫기
          </Button>
        </div>
      </div>
    </div>
  );
}

const SPOT_TYPES = ['MAIN', 'SUB', 'PHOTO', 'TREASURE'] as const;

function SortableSpotItem({
  spot,
  onEdit,
  onEditGuide,
  onEditMission,
  onEditSpotAssets,
  onDelete,
}: {
  spot: SpotAdminResponse;
  onEdit: () => void;
  onEditGuide: () => void;
  onEditMission: () => void;
  onEditSpotAssets: () => void;
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
          onEditMission={onEditMission}
          onEditSpotAssets={onEditSpotAssets}
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
  onEditMission,
  onEditSpotAssets,
  onDelete,
}: {
  spot: SpotAdminResponse;
  onEdit: () => void;
  onEditGuide: () => void;
  onEditMission: () => void;
  onEditSpotAssets: () => void;
  onDelete: () => void;
}) {
  return (
    <div className={styles.spotContent}>
      <div className={styles.spotLabelRow}>
        <span className={styles.spotTitle} title={spot.title}>
          {spot.orderIndex}. {spot.title}
        </span>
        <div className={styles.spotMeta}>
          <span className={styles.spotTypeBadge}>{spot.type}</span>
          {spot.latitude != null && (
            <span className={styles.spotCoords}>
              {spot.latitude}, {spot.longitude}
            </span>
          )}
        </div>
      </div>
      <div className={styles.spotActions}>
        <Button variant="ghost" onClick={onEdit}>
          수정
        </Button>
        <Button variant="ghost" onClick={onEditGuide}>
          컨텐츠
        </Button>
        <Button variant="ghost" onClick={onEditMission}>
          미션
        </Button>
        <Button variant="ghost" onClick={onEditSpotAssets}>
          이미지
        </Button>
        <Button variant="danger" onClick={onDelete}>
          삭제
        </Button>
      </div>
    </div>
  );
}

type GuideStepForm = {
  stepTitle: string;
  nextAction: string;
  lines: GuideLineRequest[];
};

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
    queryFn: () => fetchGuideSteps(tourId, spotId),
  });
  const saveMutation = useMutation({
    mutationFn: (body: GuideStepsSaveRequest) => saveGuideSteps(tourId, spotId, body),
    onSuccess: () => {
      showSuccess('컨텐츠가 저장되었습니다.');
      onSuccess();
      onClose();
    },
  });
  const { showSuccess, showError } = useToast();

  const [steps, setSteps] = useState<GuideStepForm[]>([]);
  const [forceCreateMode, setForceCreateMode] = useState(false);
  const [assetUploading, setAssetUploading] = useState(false);
  const [pendingAddAsset, setPendingAddAsset] = useState<{ stepIdx: number; lineIdx: number } | null>(null);
  const [previewOpen, setPreviewOpen] = useState(false);
  const assetFileInputRef = useRef<HTMLInputElement>(null);

  const { data: spotGuidePreview } = useQuery({
    queryKey: ['api', 'spot', spotId, 'guide'],
    queryFn: () => fetchSpotGuide(spotId),
    enabled: previewOpen && !!spotId,
  });

  useEffect(() => {
    if (!guide && !isError) return;
    const stepsArr = guide?.steps;
    if (stepsArr && stepsArr.length > 0) {
      setSteps(
        stepsArr.map((s) => ({
          stepTitle: s.stepTitle || spotTitle,
          nextAction: s.nextAction ?? 'NEXT',
          lines:
            (s.lines?.length ?? 0) > 0
              ? (s.lines ?? []).map((l) => ({
                  text: l.text,
                  assets: (l.assets ?? []).map((a) => ({
                    url: a.url,
                    assetType: a.assetType as 'IMAGE' | 'AUDIO',
                    usage: a.usage as 'ILLUSTRATION' | 'SCRIPT_AUDIO',
                  })),
                }))
              : [{ text: '', assets: [] }],
        }))
      );
    } else {
      setSteps([{ stepTitle: spotTitle, nextAction: 'NEXT', lines: [{ text: '', assets: [] }] }]);
    }
  }, [guide, spotTitle, isError]);

  const addStep = () =>
    setSteps((prev) => [...prev, { stepTitle: spotTitle, nextAction: 'NEXT', lines: [{ text: '', assets: [] }] }]);
  const removeStep = (stepIdx: number) => setSteps((prev) => prev.filter((_, i) => i !== stepIdx));
  const updateStep = (stepIdx: number, patch: Partial<GuideStepForm>) =>
    setSteps((prev) => prev.map((s, i) => (i === stepIdx ? { ...s, ...patch } : s)));

  const addLine = (stepIdx: number) =>
    setSteps((prev) =>
      prev.map((s, i) =>
        i === stepIdx ? { ...s, lines: [...s.lines, { text: '', assets: [] }] } : s
      )
    );
  const removeLine = (stepIdx: number, lineIdx: number) =>
    setSteps((prev) =>
      prev.map((s, i) =>
        i === stepIdx ? { ...s, lines: s.lines.filter((_, j) => j !== lineIdx) } : s
      )
    );
  const updateLineText = (stepIdx: number, lineIdx: number, text: string) =>
    setSteps((prev) =>
      prev.map((s, i) =>
        i === stepIdx
          ? { ...s, lines: s.lines.map((l, j) => (j === lineIdx ? { ...l, text } : l)) }
          : s
      )
    );
  const addAsset = (stepIdx: number, lineIdx: number, asset: GuideAssetRequest) =>
    setSteps((prev) =>
      prev.map((s, i) =>
        i === stepIdx
          ? {
              ...s,
              lines: s.lines.map((l, j) =>
                j === lineIdx ? { ...l, assets: [...l.assets, asset] } : l
              ),
            }
          : s
      )
    );
  const updateAssetUrl = (stepIdx: number, lineIdx: number, assetIdx: number, url: string) =>
    setSteps((prev) =>
      prev.map((s, i) =>
        i === stepIdx
          ? {
              ...s,
              lines: s.lines.map((l, j) =>
                j === lineIdx
                  ? { ...l, assets: l.assets.map((a, k) => (k === assetIdx ? { ...a, url } : a)) }
                  : l
              ),
            }
          : s
      )
    );
  const removeAsset = (stepIdx: number, lineIdx: number, assetIdx: number) =>
    setSteps((prev) =>
      prev.map((s, i) =>
        i === stepIdx
          ? {
              ...s,
              lines: s.lines.map((l, j) =>
                j === lineIdx ? { ...l, assets: l.assets.filter((_, k) => k !== assetIdx) } : l
              ),
            }
          : s
      )
    );

  const handleAddAssetFile = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    const target = pendingAddAsset;
    e.target.value = '';
    setPendingAddAsset(null);
    if (!file || target === null) return;
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
      addAsset(target.stepIdx, target.lineIdx, { url, assetType, usage });
    } catch (err) {
      showError(err instanceof Error ? err.message : '업로드 실패');
    } finally {
      setAssetUploading(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const stepsToSave: GuideStepSaveRequest[] = [];
    for (const step of steps) {
      const validLines = step.lines.filter((l) => l.text.trim());
      if (validLines.length === 0) {
        showError(`컨텐츠 ${stepsToSave.length + 1}: 최소 1개 문장이 필요합니다.`);
        return;
      }
      stepsToSave.push({
        stepTitle: step.stepTitle.trim() || spotTitle,
        nextAction: step.nextAction?.trim() || 'NEXT',
        lines: validLines.map((l) => ({
          text: l.text.trim(),
          assets: l.assets.filter((a) => a.url.trim()),
        })),
      });
    }
    saveMutation.mutate({ language: 'ko', steps: stepsToSave });
  };

  if (isLoading && !guide && !forceCreateMode) return <p>로딩 중...</p>;

  if (isError && !forceCreateMode) {
    return (
      <div className={styles.guideEditor}>
        <h3>컨텐츠 편집: {spotTitle}</h3>
        <p className={styles.guideError}>
          컨텐츠를 불러오지 못했습니다. 처음부터 작성하시겠습니까?
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
              setSteps([{ stepTitle: spotTitle, nextAction: 'NEXT', lines: [{ text: '', assets: [] }] }]);
              setForceCreateMode(true);
            }}
          >
            컨텐츠 추가
          </Button>
        </div>
      </div>
    );
  }

  if (steps.length === 0 && !forceCreateMode) {
    return (
      <div className={styles.guideEditor}>
        <h3>컨텐츠 편집: {spotTitle}</h3>
        <p>로딩 중...</p>
      </div>
    );
  }

  return (
    <div className={styles.guideEditor}>
      <div className={styles.guideEditorHeader}>
        <h3>컨텐츠 편집: {spotTitle}</h3>
        <div className={styles.guideEditorActions}>
          <Button variant="ghost" onClick={() => setPreviewOpen((p) => !p)}>
            {previewOpen ? '미리보기 숨기기' : '미리보기'}
          </Button>
        </div>
      </div>
      {previewOpen && (
        <div className={styles.guidePreviewPanel}>
          <h4>사용자 화면 미리보기</h4>
          {spotGuidePreview ? (
            <div className={styles.guidePreviewContent}>
              {spotGuidePreview.segments?.map((seg) => (
                <div key={seg.id} className={styles.guidePreviewSegment}>
                  <p>{seg.textEn}</p>
                  {seg.media?.length ? (
                    <div className={styles.guidePreviewMedia}>
                      {seg.media.map((m) =>
                        m.url.match(/\.(mp3|wav|ogg|m4a)(\?|$)/i) ? (
                          <audio key={m.id} controls src={m.url} className={styles.guidePreviewAudio} />
                        ) : (
                          <img key={m.id} src={m.url} alt="" className={styles.guidePreviewImg} referrerPolicy="no-referrer" />
                        )
                      )}
                    </div>
                  ) : null}
                </div>
              ))}
            </div>
          ) : (
            <p className={styles.guidePreviewHint}>
              저장된 후 사용자 API에서 미리보기를 불러옵니다. 아직 저장하지 않았다면 아래 편집 폼 내용을 기준으로 확인하세요.
            </p>
          )}
          {!spotGuidePreview && steps.length > 0 && (
            <div className={styles.guidePreviewContent}>
              {steps.map((step, si) => (
                <div key={si} className={styles.guidePreviewSegment}>
                  <strong>{step.stepTitle}</strong>
                  {step.lines.map((line, li) => (
                    <div key={li}>
                      <p>{line.text}</p>
                      {line.assets?.length ? (
                        <div className={styles.guidePreviewMedia}>
                          {line.assets.map((a, ai) =>
                            a.assetType === 'AUDIO' ? (
                              <audio key={ai} controls src={a.url} className={styles.guidePreviewAudio} />
                            ) : (
                              <img key={ai} src={a.url} alt="" className={styles.guidePreviewImg} referrerPolicy="no-referrer" />
                            )
                          )}
                        </div>
                      ) : null}
                    </div>
                  ))}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
      <form onSubmit={handleSubmit} className={styles.guideForm}>
        <input
          ref={assetFileInputRef}
          type="file"
          accept="image/*,audio/*"
          className={styles.hiddenFileInput}
          onChange={handleAddAssetFile}
        />
        {steps.map((step, stepIdx) => (
          <div key={stepIdx} className={styles.guideStepBlock}>
            <div className={styles.guideStepHeader}>
              <h4 className={styles.guideStepTitle}>컨텐츠 {stepIdx + 1}</h4>
              <Button
                type="button"
                variant="ghost"
                onClick={() => removeStep(stepIdx)}
                disabled={steps.length <= 1}
              >
                컨텐츠 삭제
              </Button>
            </div>
            <Input
              label="컨텐츠 제목"
              value={step.stepTitle}
              onChange={(e) => updateStep(stepIdx, { stepTitle: e.target.value })}
              placeholder={spotTitle}
            />
            <Select
              label="컨텐츠 후 버튼"
              value={step.nextAction}
              onChange={(e) => updateStep(stepIdx, { nextAction: e.target.value })}
              options={[
                { value: 'NEXT', label: 'NEXT - 다음 컨텐츠' },
                { value: 'MISSION_CHOICE', label: 'MISSION_CHOICE - 게임 스타트/스킵' },
              ]}
            />
            <label className={styles.formSectionLabel}>컨텐츠 문장</label>
            <div className={styles.guideLineList}>
              {step.lines.map((line, lineIdx) => (
                <div key={lineIdx} className={styles.guideLine}>
                  <div className={styles.guideLineHeader}>
                    <span className={styles.guideLineNum}>{lineIdx + 1}</span>
                    <Button
                      type="button"
                      variant="ghost"
                      onClick={() => removeLine(stepIdx, lineIdx)}
                      disabled={step.lines.length <= 1}
                    >
                      삭제
                    </Button>
                  </div>
                  <Textarea
                    value={line.text}
                    onChange={(e) => updateLineText(stepIdx, lineIdx, e.target.value)}
                    rows={2}
                    placeholder="컨텐츠 문장 입력"
                  />
                  <div className={styles.guideAssets}>
                    {line.assets.map((asset, assetIdx) => (
                      <div key={assetIdx} className={styles.guideAssetRow}>
                        <FileUploadInput
                          type={asset.assetType.toLowerCase() as 'image' | 'audio'}
                          value={asset.url}
                          onChange={(url) => updateAssetUrl(stepIdx, lineIdx, assetIdx, url)}
                          onClear={deleteUploadedFile}
                          placeholder="URL 또는 파일 업로드"
                        />
                        <Button
                          type="button"
                          variant="ghost"
                          onClick={async () => {
                            const url = line.assets[assetIdx]?.url;
                            if (url?.trim()) {
                              try {
                                await deleteUploadedFile(url);
                              } catch (e) {
                                showError(e instanceof Error ? e.message : 'S3 삭제 실패');
                                return;
                              }
                            }
                            removeAsset(stepIdx, lineIdx, assetIdx);
                          }}
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
                          setPendingAddAsset({ stepIdx, lineIdx });
                          assetFileInputRef.current?.click();
                        }}
                      >
                        {assetUploading ? '업로드 중…' : '+ 에셋 추가'}
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            <Button
              type="button"
              variant="secondary"
              className={styles.guideAddLineButton}
              onClick={() => addLine(stepIdx)}
            >
              + 문장 추가
            </Button>
          </div>
        ))}
        <Button type="button" variant="secondary" className={styles.guideAddStepButton} onClick={addStep}>
          + 컨텐츠 추가
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

const MISSION_TYPES = ['QUIZ', 'INPUT', 'PHOTO_CHECK'] as const;

function MissionEditor({
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
  const { showSuccess, showError } = useToast();
  const queryClient = useQueryClient();
  const { data: steps, isLoading } = useQuery({
    queryKey: ['admin', 'mission-steps', tourId, spotId],
    queryFn: () => fetchMissionSteps(tourId, spotId),
  });
  const [addFormOpen, setAddFormOpen] = useState(false);
  const [editingStep, setEditingStep] = useState<MissionStepResponse | null>(null);

  const createMutation = useMutation({
    mutationFn: (body: MissionStepCreateRequest) =>
      createMissionStep(tourId, spotId, body),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['admin', 'mission-steps', tourId, spotId],
      });
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours'] });
      setAddFormOpen(false);
      showSuccess('미션이 추가되었습니다.');
      onSuccess();
    },
    onError: (e: Error) => showError(e.message),
  });
  const updateMutation = useMutation({
    mutationFn: ({
      stepId,
      body,
    }: {
      stepId: number;
      body: MissionStepUpdateRequest;
    }) => updateMissionStep(tourId, spotId, stepId, body),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['admin', 'mission-steps', tourId, spotId],
      });
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours'] });
      setEditingStep(null);
      showSuccess('미션이 수정되었습니다.');
      onSuccess();
    },
    onError: (e: Error) => showError(e.message),
  });
  const deleteMutation = useMutation({
    mutationFn: (stepId: number) =>
      deleteMissionStep(tourId, spotId, stepId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['admin', 'mission-steps', tourId, spotId],
      });
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours'] });
      showSuccess('미션이 삭제되었습니다.');
      onSuccess();
    },
    onError: (e: Error) => showError(e.message),
  });

  const [previewOpen, setPreviewOpen] = useState(false);

  return (
    <div className={styles.guideEditor}>
      <div className={styles.guideEditorHeader}>
        <h3>미션 관리: {spotTitle}</h3>
        <div className={styles.guideEditorActions}>
          <Button variant="ghost" onClick={() => setPreviewOpen((p) => !p)}>
            {previewOpen ? '미리보기 숨기기' : '미리보기'}
          </Button>
          <Button variant="ghost" onClick={onClose}>
            닫기
          </Button>
        </div>
      </div>
      {previewOpen && (steps ?? []).length > 0 && (
        <div className={styles.missionPreviewPanel}>
          <h4>미션 미리보기</h4>
          <div className={styles.missionPreviewList}>
            {(steps ?? []).map((s) => (
              <div key={s.stepId} className={styles.missionPreviewCard}>
                <strong>{s.stepIndex}. {s.title || '(제목 없음)'}</strong> — {s.missionType}
                <p>{s.prompt}</p>
                {Array.isArray(s.optionsJson?.choices) ? (
                  <ul>
                    {(s.optionsJson.choices as Array<{ id: string; text: string }>).map((c) => (
                      <li key={String(c.id)}>{String(c.id)}: {String(c.text)}</li>
                    ))}
                  </ul>
                ) : null}
              </div>
            ))}
          </div>
        </div>
      )}
      {isLoading ? (
        <p>로딩 중...</p>
      ) : (
        <>
          <ul className={styles.stepList}>
            {(steps ?? []).map((s) => (
              <li key={s.stepId} className={styles.spotItem}>
                <div className={styles.spotContent}>
                  <span className={styles.spotLabel}>
                    <span className={styles.spotTypeBadge}>
                      {s.stepIndex}. {s.title || '(제목 없음)'} — {s.missionType}
                    </span>
                  </span>
                  <div className={styles.spotActions}>
                    <Button
                      variant="ghost"
                      onClick={() => setEditingStep(s)}
                    >
                      수정
                    </Button>
                    <Button
                      variant="danger"
                      onClick={() => {
                        if (window.confirm(`${s.title}을(를) 삭제하시겠습니까?`)) {
                          deleteMutation.mutate(s.stepId);
                        }
                      }}
                      disabled={deleteMutation.isPending}
                    >
                      삭제
                    </Button>
                  </div>
                </div>
              </li>
            ))}
          </ul>
          {!addFormOpen ? (
            <Button
              variant="secondary"
              className={styles.missionAddButton}
              onClick={() => setAddFormOpen(true)}
            >
              + 미션 추가
            </Button>
          ) : (
            <MissionStepForm
              key="mission-add"
              missionType="QUIZ"
              prompt=""
              title=""
              optionsJson={{}}
              answerJson={{}}
              onSubmit={(body) => {
                createMutation.mutate(body as MissionStepCreateRequest);
              }}
              onCancel={() => setAddFormOpen(false)}
              submitLabel="추가"
              isPending={createMutation.isPending}
            />
          )}
        </>
      )}
      {editingStep && (
        <div className={styles.missionEditPanel}>
          <h4>미션 수정</h4>
          <MissionStepForm
            key={`mission-edit-${editingStep.stepId}`}
            missionType={editingStep.missionType as (typeof MISSION_TYPES)[number]}
            prompt={editingStep.prompt}
            title={editingStep.title}
            optionsJson={editingStep.optionsJson || {}}
            answerJson={editingStep.answerJson || {}}
            onSubmit={(body) => {
              updateMutation.mutate({
                stepId: editingStep.stepId,
                body: {
                  prompt: body.prompt,
                  title: body.title,
                  optionsJson: Object.keys(body.optionsJson || {}).length
                    ? body.optionsJson
                    : undefined,
                  answerJson: Object.keys(body.answerJson || {}).length
                    ? body.answerJson
                    : undefined,
                },
              });
            }}
            onCancel={() => setEditingStep(null)}
            submitLabel="수정"
            isPending={updateMutation.isPending}
            isEdit
          />
        </div>
      )}
    </div>
  );
}

type QuizChoice = { id: string; text: string; imageUrl: string };

function parseQuizOptions(json: Record<string, unknown>): {
  questionImageUrl: string;
  choices: QuizChoice[];
} {
  const choices: QuizChoice[] = [];
  if (Array.isArray(json.choices)) {
    for (const c of json.choices) {
      if (c && typeof c === 'object' && 'id' in c && 'text' in c) {
        choices.push({
          id: String((c as { id: unknown }).id ?? ''),
          text: String((c as { text: unknown }).text ?? ''),
          imageUrl: String((c as { imageUrl?: unknown }).imageUrl ?? ''),
        });
      }
    }
  }
  return {
    questionImageUrl: typeof json.questionImageUrl === 'string' ? json.questionImageUrl : '',
    choices: choices.length > 0 ? choices : [{ id: 'a', text: '', imageUrl: '' }],
  };
}

function MissionStepForm({
  missionType,
  prompt,
  title,
  optionsJson,
  answerJson,
  onSubmit,
  onCancel,
  submitLabel,
  isPending,
  isEdit = false,
}: {
  missionType: string;
  prompt: string;
  title: string;
  optionsJson: Record<string, unknown>;
  answerJson: Record<string, unknown>;
  onSubmit: (body: MissionStepCreateRequest | MissionStepUpdateRequest) => void;
  onCancel: () => void;
  submitLabel: string;
  isPending: boolean;
  isEdit?: boolean;
}) {
  const [mt, setMt] = useState(missionType);
  const [pr, setPr] = useState(prompt);
  const [tl, setTl] = useState(title);
  const [optUploading, setOptUploading] = useState(false);
  const [optUploadTarget, setOptUploadTarget] = useState<'question' | number | null>(null);
  const missionFileInputRef = useRef<HTMLInputElement>(null);
  const { showSuccess, showError } = useToast();

  // QUIZ: 보기 목록 + 문제 이미지
  const [quizQuestionImage, setQuizQuestionImage] = useState(() =>
    typeof optionsJson?.questionImageUrl === 'string' ? optionsJson.questionImageUrl : ''
  );
  const [quizChoices, setQuizChoices] = useState<QuizChoice[]>(() => {
    if (mt !== 'QUIZ') return [];
    return parseQuizOptions(optionsJson || {}).choices;
  });
  const [quizAnswer, setQuizAnswer] = useState(() =>
    String(answerJson?.answer ?? answerJson?.value ?? '')
  );

  // INPUT
  const [inputPlaceholder, setInputPlaceholder] = useState(() =>
    typeof optionsJson?.placeholder === 'string' ? optionsJson.placeholder : ''
  );
  const [inputHintImage, setInputHintImage] = useState(() =>
    typeof optionsJson?.hintImageUrl === 'string' ? optionsJson.hintImageUrl : ''
  );

  // PHOTO_CHECK
  const [photoExampleImage, setPhotoExampleImage] = useState(() =>
    typeof optionsJson?.exampleImageUrl === 'string' ? optionsJson.exampleImageUrl : ''
  );
  const [photoInstruction, setPhotoInstruction] = useState(() =>
    typeof optionsJson?.instruction === 'string' ? optionsJson.instruction : ''
  );

  // missionType 변경 시 choices 초기화
  useEffect(() => {
    if (mt === 'QUIZ' && quizChoices.length === 0) {
      setQuizChoices([{ id: 'a', text: '', imageUrl: '' }]);
    }
  }, [mt, quizChoices.length]);

  // 외부 optionsJson/answerJson 변경 시 폼 동기화 (수정 모드)
  useEffect(() => {
    if (mt === 'QUIZ') {
      const parsed = parseQuizOptions(optionsJson || {});
      setQuizQuestionImage(parsed.questionImageUrl);
      setQuizChoices(parsed.choices.length > 0 ? parsed.choices : [{ id: 'a', text: '', imageUrl: '' }]);
      setQuizAnswer(String(answerJson?.answer ?? answerJson?.value ?? ''));
    } else if (mt === 'INPUT') {
      setInputPlaceholder(typeof optionsJson?.placeholder === 'string' ? optionsJson.placeholder : '');
      setInputHintImage(typeof optionsJson?.hintImageUrl === 'string' ? optionsJson.hintImageUrl : '');
    } else if (mt === 'PHOTO_CHECK') {
      setPhotoExampleImage(typeof optionsJson?.exampleImageUrl === 'string' ? optionsJson.exampleImageUrl : '');
      setPhotoInstruction(typeof optionsJson?.instruction === 'string' ? optionsJson.instruction : '');
    }
  }, [mt, optionsJson, answerJson]);

  const handleMissionImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    const target = optUploadTarget;
    if (!file || !file.type.startsWith('image/') || target === null) return;

    setOptUploading(true);
    try {
      const url = await uploadFile(file, 'image', 'mission');
      if (target === 'question') {
        if (mt === 'QUIZ') setQuizQuestionImage(url);
        else if (mt === 'INPUT') setInputHintImage(url);
        else if (mt === 'PHOTO_CHECK') setPhotoExampleImage(url);
        showSuccess('이미지 URL이 삽입되었습니다.');
      } else {
        setQuizChoices((prev) => {
          const next = [...prev];
          while (next.length <= target) {
            next.push({ id: String.fromCharCode(97 + next.length), text: '', imageUrl: '' });
          }
          next[target] = { ...next[target], imageUrl: url };
          return next;
        });
        showSuccess(`보기 ${target + 1} 이미지가 업로드되었습니다.`);
      }
    } catch (err) {
      showError(err instanceof Error ? err.message : '업로드 실패');
    } finally {
      setOptUploading(false);
      setOptUploadTarget(null);
    }
  };

  const addChoice = () =>
    setQuizChoices((prev) => [
      ...prev,
      { id: String.fromCharCode(97 + prev.length), text: '', imageUrl: '' },
    ]);
  const removeChoice = (idx: number) =>
    setQuizChoices((prev) => prev.filter((_, i) => i !== idx));
  const updateChoice = (idx: number, patch: Partial<QuizChoice>) =>
    setQuizChoices((prev) => prev.map((c, i) => (i === idx ? { ...c, ...patch } : c)));

  const buildOptionsAndAnswer = (): {
    optionsJson: Record<string, unknown> | undefined;
    answerJson: Record<string, unknown> | undefined;
  } => {
    if (mt === 'QUIZ') {
      const choices = quizChoices
        .filter((c) => c.text.trim())
        .map((c) => ({ id: c.id.trim() || 'a', text: c.text.trim(), imageUrl: c.imageUrl.trim() || undefined }));
      const optionsJson: Record<string, unknown> = {
        choices: choices.map((c) => ({ ...c, imageUrl: c.imageUrl ?? '' })),
      };
      if (quizQuestionImage.trim()) optionsJson.questionImageUrl = quizQuestionImage.trim();
      const answerJson = quizAnswer.trim() ? { answer: quizAnswer.trim() } : undefined;
      return { optionsJson: choices.length > 0 ? optionsJson : undefined, answerJson };
    }
    if (mt === 'INPUT') {
      const o: Record<string, unknown> = {};
      if (inputPlaceholder.trim()) o.placeholder = inputPlaceholder.trim();
      if (inputHintImage.trim()) o.hintImageUrl = inputHintImage.trim();
      return { optionsJson: Object.keys(o).length ? o : undefined, answerJson: undefined };
    }
    if (mt === 'PHOTO_CHECK') {
      const o: Record<string, unknown> = {};
      if (photoExampleImage.trim()) o.exampleImageUrl = photoExampleImage.trim();
      if (photoInstruction.trim()) o.instruction = photoInstruction.trim();
      return { optionsJson: Object.keys(o).length ? o : undefined, answerJson: undefined };
    }
    return { optionsJson: undefined, answerJson: undefined };
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (mt === 'QUIZ') {
      const valid = quizChoices.filter((c) => c.text.trim());
      if (valid.length === 0) {
        showError('최소 1개의 보기를 입력해주세요.');
        return;
      }
      if (!quizAnswer.trim()) {
        showError('정답을 선택해주세요.');
        return;
      }
      if (!valid.some((c) => c.id === quizAnswer.trim())) {
        showError(`정답은 보기 ID(a, b, c...) 중 하나여야 합니다.`);
        return;
      }
    }
    const { optionsJson: opt, answerJson: ans } = buildOptionsAndAnswer();
    onSubmit({
      missionType: mt,
      prompt: pr.trim(),
      title: tl.trim() || undefined,
      optionsJson: opt,
      answerJson: ans,
    });
  };

  return (
    <form onSubmit={handleSubmit} className={styles.missionStepForm}>
      <input
        ref={missionFileInputRef}
        type="file"
        accept="image/*"
        className={styles.hiddenFileInput}
        onChange={handleMissionImageUpload}
      />
      {!isEdit && (
        <Select
          label="미션 유형"
          value={mt}
          onChange={(e) => setMt(e.target.value)}
          options={MISSION_TYPES.map((t) => ({ value: t, label: t }))}
        />
      )}
      {isEdit && (
        <p className={styles.formHint}>유형: {mt} (수정 불가)</p>
      )}
      <Input
        label="제목"
        value={tl}
        onChange={(e) => setTl(e.target.value)}
        placeholder="미션 제목"
      />
      <Textarea
        label="문제 (prompt)"
        value={pr}
        onChange={(e) => setPr(e.target.value)}
        rows={2}
        placeholder="질문 또는 지시문"
      />

      {/* QUIZ: 보기 + 정답 */}
      {mt === 'QUIZ' && (
        <>
          <div className={styles.missionImageUpload}>
            <label className={styles.formSectionLabel}>문제 이미지</label>
            <div className={styles.missionImageUploadBtns}>
              <Button
                type="button"
                variant="ghost"
                disabled={optUploading}
                onClick={() => {
                  setOptUploadTarget('question');
                  missionFileInputRef.current?.click();
                }}
              >
                {optUploading && optUploadTarget === 'question' ? '업로드 중…' : '문제 이미지 업로드'}
              </Button>
            </div>
            {quizQuestionImage && (
              <div className={styles.missionImagePreview} style={{ marginTop: 8 }}>
                <div className={styles.missionImagePreviewItem}>
                  <span className={styles.missionImagePreviewLabel}>문제 이미지</span>
                  <img
                    src={quizQuestionImage}
                    alt="문제 이미지"
                    className={styles.missionImagePreviewThumb}
                    referrerPolicy="no-referrer"
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    className={styles.missionImageRemove}
                    onClick={async () => {
                      if (quizQuestionImage.trim()) {
                        try {
                          await deleteUploadedFile(quizQuestionImage);
                        } catch (e) {
                          showError(e instanceof Error ? e.message : 'S3 삭제 실패');
                          return;
                        }
                        setQuizQuestionImage('');
                      }
                    }}
                  >
                    삭제
                  </Button>
                </div>
              </div>
            )}
          </div>
          <div className={styles.missionOptionsSection}>
            <label className={styles.formSectionLabel}>보기</label>
            {quizChoices.map((choice, idx) => (
              <div key={idx} className={styles.quizChoiceRow}>
                <Input
                  label=""
                  value={choice.id}
                  onChange={(e) => updateChoice(idx, { id: e.target.value })}
                  placeholder="a"
                  className={styles.quizChoiceId}
                />
                <Input
                  label=""
                  value={choice.text}
                  onChange={(e) => updateChoice(idx, { text: e.target.value })}
                  placeholder="보기 텍스트"
                  className={styles.quizChoiceText}
                />
                <Button
                  type="button"
                  variant="ghost"
                  disabled={optUploading}
                  onClick={() => {
                    setOptUploadTarget(idx);
                    missionFileInputRef.current?.click();
                  }}
                >
                  {optUploading && optUploadTarget === idx ? '업로드…' : '이미지'}
                </Button>
                {choice.imageUrl && (
                  <>
                    <img
                      src={choice.imageUrl}
                      alt={`보기 ${idx + 1}`}
                      className={styles.quizChoiceThumb}
                      referrerPolicy="no-referrer"
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      className={styles.missionImageRemoveSmall}
                      onClick={async () => {
                        if (choice.imageUrl.trim()) {
                          try {
                            await deleteUploadedFile(choice.imageUrl);
                          } catch (e) {
                            showError(e instanceof Error ? e.message : 'S3 삭제 실패');
                            return;
                          }
                          updateChoice(idx, { imageUrl: '' });
                        }
                      }}
                    >
                      ×
                    </Button>
                  </>
                )}
                <Button
                  type="button"
                  variant="ghost"
                  onClick={() => removeChoice(idx)}
                  disabled={quizChoices.length <= 1}
                >
                  삭제
                </Button>
              </div>
            ))}
            <Button type="button" variant="secondary" onClick={addChoice}>
              + 보기 추가
            </Button>
            <div className={styles.missionAnswerSelect}>
              <Select
                label="정답"
                value={quizAnswer}
                onChange={(e) => setQuizAnswer(e.target.value)}
                options={[
                  { value: '', label: '(선택)' },
                  ...quizChoices
                    .filter((c) => c.id.trim())
                    .map((c) => ({ value: c.id, label: `${c.id}: ${c.text.slice(0, 20)}${c.text.length > 20 ? '…' : ''}` })),
                ]}
              />
            </div>
          </div>
        </>
      )}

      {/* INPUT */}
      {mt === 'INPUT' && (
        <div className={styles.missionOptionsSection}>
          <Input
            label="placeholder"
            value={inputPlaceholder}
            onChange={(e) => setInputPlaceholder(e.target.value)}
            placeholder="답을 입력하세요"
          />
          <div className={styles.missionImageUpload}>
            <label className={styles.formSectionLabel}>힌트 이미지</label>
            <div className={styles.missionImageUploadBtns}>
            <Button
              type="button"
              variant="ghost"
              disabled={optUploading}
              onClick={() => {
                setOptUploadTarget('question');
                missionFileInputRef.current?.click();
              }}
            >
              {optUploading ? '업로드 중…' : '이미지 업로드'}
            </Button>
            {inputHintImage && (
              <span className={styles.missionImageWithRemove}>
                <img
                  src={inputHintImage}
                  alt="힌트"
                  className={styles.quizChoiceThumb}
                  referrerPolicy="no-referrer"
                />
                <Button
                  type="button"
                  variant="ghost"
                  className={styles.missionImageRemoveSmall}
                  onClick={async () => {
                    if (inputHintImage.trim()) {
                      try {
                        await deleteUploadedFile(inputHintImage);
                      } catch (e) {
                        showError(e instanceof Error ? e.message : 'S3 삭제 실패');
                        return;
                      }
                      setInputHintImage('');
                    }
                  }}
                >
                  ×
                </Button>
              </span>
            )}
            </div>
          </div>
        </div>
      )}

      {/* PHOTO_CHECK */}
      {mt === 'PHOTO_CHECK' && (
        <div className={styles.missionOptionsSection}>
          <Textarea
            label="instruction"
            value={photoInstruction}
            onChange={(e) => setPhotoInstruction(e.target.value)}
            rows={2}
            placeholder="이 장소를 찍어주세요"
          />
          <div className={styles.missionImageUpload}>
            <label className={styles.formSectionLabel}>예시 이미지</label>
            <div className={styles.missionImageUploadBtns}>
            <Button
              type="button"
              variant="ghost"
              disabled={optUploading}
              onClick={() => {
                setOptUploadTarget('question');
                missionFileInputRef.current?.click();
              }}
            >
              {optUploading ? '업로드 중…' : '이미지 업로드'}
            </Button>
            {photoExampleImage && (
              <span className={styles.missionImageWithRemove}>
                <img
                  src={photoExampleImage}
                  alt="예시"
                  className={styles.quizChoiceThumb}
                  referrerPolicy="no-referrer"
                />
                <Button
                  type="button"
                  variant="ghost"
                  className={styles.missionImageRemoveSmall}
                  onClick={async () => {
                    if (photoExampleImage.trim()) {
                      try {
                        await deleteUploadedFile(photoExampleImage);
                      } catch (e) {
                        showError(e instanceof Error ? e.message : 'S3 삭제 실패');
                        return;
                      }
                      setPhotoExampleImage('');
                    }
                  }}
                >
                  ×
                </Button>
              </span>
            )}
            </div>
          </div>
        </div>
      )}

      <div className={styles.formActions}>
        <Button type="button" variant="secondary" onClick={onCancel}>
          취소
        </Button>
        <Button type="submit" disabled={isPending}>
          {submitLabel}
        </Button>
      </div>
    </form>
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
  const [mapOpen, setMapOpen] = useState(false);
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
  const [editingMissionSpot, setEditingMissionSpot] = useState<SpotAdminResponse | null>(null);
  const [spotAssetsSpot, setSpotAssetsSpot] = useState<SpotAdminResponse | null>(null);
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
      setEditingMissionSpot(null);
      setSpotAssetsSpot(null);
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

  const inSpotListMode =
    spots.length > 0 &&
    !editingGuideSpot &&
    !editingMissionSpot &&
    !spotAssetsSpot &&
    !formOpen;
  const showMapSection = inSpotListMode && mapOpen;

  return (
    <div className={styles.drawer}>
      <div className={styles.drawerBackdrop} onClick={onClose} />
        <div
          className={
            showMapSection
              ? `${styles.spotsDrawerPanel} ${styles.spotsDrawerPanelWithMap}`
              : styles.spotsDrawerPanel
          }
        >
        <div className={styles.spotsDrawerMain}>
          <div className={styles.spotsDrawerHeader}>
            <h2>Spots</h2>
            <div className={styles.spotsHeaderActions}>
              {onRagSync && (
                <Button
                  variant="ghost"
                  onClick={onRagSync}
                  disabled={ragSyncPending}
                >
                  {ragSyncPending ? '동기화 중...' : '지식 동기화'}
                </Button>
              )}
              {inSpotListMode && (
                <Button variant="secondary" onClick={() => setMapOpen((prev) => !prev)}>
                  {mapOpen ? '지도 숨기기' : '지도 보기'}
                </Button>
              )}
              {inSpotListMode && <Button onClick={handleAddSpot}>Spot 추가</Button>}
              <Button
                variant="ghost"
                className={styles.spotsHeaderClose}
                onClick={onClose}
                aria-label="닫기"
                title="닫기"
              >
                <X size={16} />
              </Button>
            </div>
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
        ) : spotAssetsSpot ? (
          <SpotAssetsDrawer
            tourId={tourId}
            spotId={spotAssetsSpot.id}
            spotTitle={spotAssetsSpot.title}
            onClose={() => setSpotAssetsSpot(null)}
          />
        ) : editingMissionSpot ? (
          <MissionEditor
            tourId={tourId}
            spotId={editingMissionSpot.id}
            spotTitle={editingMissionSpot.title}
            onClose={() => setEditingMissionSpot(null)}
            onSuccess={() => {
              queryClient.invalidateQueries({ queryKey: ['admin', 'mission-steps', tourId] });
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
          ) : null
        ) : (
          <form onSubmit={handleSubmitSpot} className={styles.spotForm}>
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
            {pronunciationUrl.trim() && (
              <div className={styles.pronunciationPreview}>
                <audio controls src={pronunciationUrl.trim()} className={styles.pronunciationAudio} />
              </div>
            )}
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
        {!editingGuideSpot && !editingMissionSpot && !spotAssetsSpot && spots.length > 0 && (
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
                    onEditMission={() => setEditingMissionSpot(s)}
                    onEditSpotAssets={() => setSpotAssetsSpot(s)}
                    onDelete={() => setDeleteTarget(s)}
                  />
                ))}
              </ul>
            </SortableContext>
          </DndContext>
          </>
        )}
        </div>
        {showMapSection && (
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
