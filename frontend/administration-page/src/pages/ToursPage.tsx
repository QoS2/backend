import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchTours,
  fetchTour,
  createTour,
  updateTour,
  deleteTour,
  fetchSteps,
  createStep,
  type TourAdminResponse,
  type TourCreateRequest,
  type StepAdminResponse,
  type StepCreateRequest,
} from '../api/tour';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { Input } from '../components/ui/Input';
import { useToast } from '../context/ToastContext';
import styles from './ToursPage.module.css';

export function ToursPage() {
  const { showSuccess, showError } = useToast();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editingTourId, setEditingTourId] = useState<number | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<TourAdminResponse | null>(null);
  const [stepDrawerOpen, setStepDrawerOpen] = useState(false);
  const [selectedTourId, setSelectedTourId] = useState<number | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ['admin', 'tours', page],
    queryFn: () => fetchTours(page, 20),
  });
  const { data: tourDetail } = useQuery({
    queryKey: ['admin', 'tour', editingTourId],
    queryFn: () => (editingTourId ? fetchTour(editingTourId) : null),
    enabled: !!editingTourId,
  });
  const { data: steps } = useQuery({
    queryKey: ['admin', 'tours', selectedTourId, 'steps'],
    queryFn: () => (selectedTourId ? fetchSteps(selectedTourId) : []),
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

  const handleOpenSteps = (tour: TourAdminResponse) => {
    setSelectedTourId(tour.id);
    setStepDrawerOpen(true);
  };

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
              key: 'stepsCount',
              label: 'Steps',
              render: (row: TourAdminResponse) => (
                <span>
                  {row.stepsCount} / Way: {row.waypointsCount} / Photo: {row.photoSpotsCount} / Treasure: {row.treasuresCount}
                </span>
              ),
            },
            {
              key: 'actions',
              label: '작업',
              render: (row: TourAdminResponse) => (
                <div className={styles.actions}>
                  <Button onClick={() => handleEdit(row)}>수정</Button>
                  <Button variant="secondary" onClick={() => handleOpenSteps(row)}>
                    Steps
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

      {stepDrawerOpen && selectedTourId && (
        <StepsDrawer
          tourId={selectedTourId}
          steps={steps ?? []}
          onClose={() => {
            setStepDrawerOpen(false);
            setSelectedTourId(null);
          }}
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
          <div className={styles.formRow}>
            <label>설명</label>
            <textarea
              value={descriptionEn}
              onChange={(e) => setDescriptionEn(e.target.value)}
              rows={4}
            />
          </div>
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

function StepsDrawer({
  tourId,
  steps,
  onClose,
}: {
  tourId: number;
  steps: StepAdminResponse[];
  onClose: () => void;
}) {
  const [formOpen, setFormOpen] = useState(false);
  const [externalKey, setExternalKey] = useState('');
  const [stepOrder, setStepOrder] = useState(steps.length + 1);
  const [titleEn, setTitleEn] = useState('');
  const [shortDescEn, setShortDescEn] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useToast();

  const createMutation = useMutation({
    mutationFn: (body: StepCreateRequest) => createStep(tourId, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'tours', tourId, 'steps'] });
      setFormOpen(false);
      setExternalKey('');
      setTitleEn('');
      setShortDescEn('');
      showSuccess('Step이 추가되었습니다.');
    },
    onError: (e: Error) => showError(e.message),
  });

  const handleAddStep = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate({
      externalKey: externalKey.trim(),
      stepOrder,
      titleEn: titleEn.trim(),
      shortDescEn: shortDescEn.trim() || undefined,
      latitude: latitude ? parseFloat(latitude) : undefined,
      longitude: longitude ? parseFloat(longitude) : undefined,
    });
  };

  return (
    <div className={styles.drawer}>
      <div className={styles.drawerBackdrop} onClick={onClose} />
      <div className={styles.drawerPanel}>
        <h2>Steps</h2>
        {!formOpen ? (
          <Button onClick={() => setFormOpen(true)}>Step 추가</Button>
        ) : (
          <form onSubmit={handleAddStep}>
            <Input label="External Key" value={externalKey} onChange={(e) => setExternalKey(e.target.value)} required />
            <Input label="Order" type="number" value={String(stepOrder)} onChange={(e) => setStepOrder(parseInt(e.target.value, 10) || 1)} />
            <Input label="제목" value={titleEn} onChange={(e) => setTitleEn(e.target.value)} required />
            <Input label="Short Desc" value={shortDescEn} onChange={(e) => setShortDescEn(e.target.value)} />
            <Input label="Latitude" value={latitude} onChange={(e) => setLatitude(e.target.value)} />
            <Input label="Longitude" value={longitude} onChange={(e) => setLongitude(e.target.value)} />
            <div className={styles.formActions}>
              <Button type="button" variant="secondary" onClick={() => setFormOpen(false)}>취소</Button>
              <Button type="submit" disabled={createMutation.isPending}>추가</Button>
            </div>
          </form>
        )}
        <ul className={styles.stepList}>
          {steps.map((s) => (
            <li key={s.id}>
              {s.stepOrder}. {s.titleEn ?? s.externalKey}
              {s.latitude != null && ` (${s.latitude}, ${s.longitude})`}
            </li>
          ))}
        </ul>
        <Button variant="secondary" onClick={onClose}>닫기</Button>
      </div>
    </div>
  );
}
