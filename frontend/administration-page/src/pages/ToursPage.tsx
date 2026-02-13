import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
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
import { Textarea } from '../components/ui/Textarea';
import { FileUploadInput } from '../components/ui/FileUploadInput';
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
        <Button onClick={handleCreate}>투어 추가</Button>
      </div>

      {isLoading ? (
        <p>로딩 중...</p>
      ) : (
        <Table
          columns={[
            { key: 'externalKey', label: 'Key' },
            { key: 'titleEn', label: '제목' },
            {
              key: 'spots',
              label: 'Spots',
              render: (row: TourAdminResponse) => (
                <span>
                  Main: {row.mainCount} / Sub: {row.subCount} / Photo: {row.photoSpotsCount} / Treasure: {row.treasuresCount} / Missions: {row.missionsCount}
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
  isSubmitting,
}: {
  tour?: TourAdminResponse;
  onClose: () => void;
  onSubmit: (v: TourFormValues) => void;
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

function SpotItemWithGuide({
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

  return (
    <li className={styles.spotItem}>
      <div className={styles.spotRow}>
        <span>[{spot.type}] {spot.orderIndex}. {spot.title}{spot.latitude != null && ` (${spot.latitude}, ${spot.longitude})`}</span>
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
    </li>
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
  const [lines, setLines] = useState<GuideLineRequest[]>([]);
  const [forceCreateMode, setForceCreateMode] = useState(false);

  useEffect(() => {
    if (!guide && !isError) return;
    setStepTitle(guide?.stepTitle || spotTitle);
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
                  onClick={() =>
                    addAsset(lineIdx, {
                      url: '',
                      assetType: 'IMAGE',
                      usage: 'ILLUSTRATION',
                    })
                  }
                >
                  + 이미지
                </Button>
                <Button
                  type="button"
                  variant="ghost"
                  onClick={() =>
                    addAsset(lineIdx, {
                      url: '',
                      assetType: 'AUDIO',
                      usage: 'SCRIPT_AUDIO',
                    })
                  }
                >
                  + 오디오
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
}: {
  tourId: number;
  spots: SpotAdminResponse[];
  onClose: () => void;
}) {
  const [formOpen, setFormOpen] = useState(false);
  const [editingSpot, setEditingSpot] = useState<SpotAdminResponse | null>(null);
  const [type, setType] = useState<string>('MAIN');
  const [orderIndex, setOrderIndex] = useState(spots.length + 1);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [radiusM, setRadiusM] = useState('60');
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
    setDescription('');
    setLatitude('');
    setLongitude('');
    setRadiusM('60');
  }

  const handleAddSpot = () => {
    setEditingSpot(null);
    resetForm();
    setFormOpen(true);
  };
  const handleEditSpot = (spot: SpotAdminResponse) => {
    setEditingSpot(spot);
    setTitle(spot.title ?? '');
    setDescription(spot.description ?? '');
    setOrderIndex(spot.orderIndex);
    setLatitude(spot.latitude?.toString() ?? '');
    setLongitude(spot.longitude?.toString() ?? '');
    setRadiusM(spot.radiusM?.toString() ?? '60');
    setFormOpen(true);
  };
  const handleSubmitSpot = (e: React.FormEvent) => {
    e.preventDefault();
    if (editingSpot) {
      updateMutation.mutate({
        spotId: editingSpot.id,
        body: {
          title: title.trim(),
          description: description.trim() || undefined,
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
        description: description.trim() || undefined,
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
      <div className={styles.drawerPanel}>
        <h2>Spots</h2>
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
          <Button onClick={handleAddSpot}>Spot 추가</Button>
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
            <Input label="제목" value={title} onChange={(e) => setTitle(e.target.value)} required />
            <Input label="설명" value={description} onChange={(e) => setDescription(e.target.value)} />
            <Input label="Latitude" value={latitude} onChange={(e) => setLatitude(e.target.value)} />
            <Input label="Longitude" value={longitude} onChange={(e) => setLongitude(e.target.value)} />
            <Input label="Radius (m)" type="number" value={radiusM} onChange={(e) => setRadiusM(e.target.value)} />
            <div className={styles.formActions}>
              <Button type="button" variant="secondary" onClick={() => { setFormOpen(false); setEditingSpot(null); }}>취소</Button>
              <Button type="submit" disabled={createMutation.isPending || updateMutation.isPending}>
                {editingSpot ? '수정' : '추가'}
              </Button>
            </div>
          </form>
        )}
        {!editingGuideSpot && (
          <ul className={styles.stepList}>
            {spots.map((s) => (
              <SpotItemWithGuide
                key={s.id}
                spot={s}
                onEdit={() => handleEditSpot(s)}
                onEditGuide={() => setEditingGuideSpot(s)}
                onDelete={() => setDeleteTarget(s)}
              />
            ))}
          </ul>
        )}
        {deleteTarget && (
          <div className={styles.deleteConfirm}>
            <span>{deleteTarget.title} 삭제할까요?</span>
            <div>
              <Button variant="secondary" onClick={() => setDeleteTarget(null)}>취소</Button>
              <Button variant="danger" onClick={() => deleteMutation.mutate(deleteTarget.id)}>삭제</Button>
            </div>
          </div>
        )}
        <Button variant="secondary" onClick={onClose}>닫기</Button>
      </div>
    </div>
  );
}
