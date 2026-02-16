import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Camera, CircleCheck, Ban } from 'lucide-react';
import {
  fetchPendingPhotoSubmissions,
  verifyPhotoSubmission,
  type PhotoSubmissionItem,
} from '../api/photoSubmission';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Modal } from '../components/ui/Modal';
import { useToast } from '../context/ToastContext';
import styles from './PhotoSubmissionsPage.module.css';

export function PhotoSubmissionsPage() {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useToast();
  const [rejectTarget, setRejectTarget] = useState<PhotoSubmissionItem | null>(null);
  const [rejectReason, setRejectReason] = useState('');

  const { data: submissions = [], isLoading } = useQuery({
    queryKey: ['admin', 'photo-submissions'],
    queryFn: fetchPendingPhotoSubmissions,
  });

  const verifyMutation = useMutation({
    mutationFn: ({
      submissionId,
      action,
      rejectReason: reason,
    }: {
      submissionId: number;
      action: 'APPROVE' | 'REJECT';
      rejectReason?: string;
    }) => verifyPhotoSubmission(submissionId, { action, rejectReason: reason }),
    onSuccess: (_, { action }) => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'photo-submissions'] });
      showSuccess(action === 'APPROVE' ? '사진이 승인되었습니다.' : '사진이 거절되었습니다.');
      setRejectTarget(null);
      setRejectReason('');
    },
    onError: (e: Error) => showError(e.message),
  });

  const handleApprove = (item: PhotoSubmissionItem) => {
    verifyMutation.mutate({ submissionId: item.submissionId, action: 'APPROVE' });
  };

  const handleRejectClick = (item: PhotoSubmissionItem) => {
    setRejectTarget(item);
    setRejectReason('');
  };

  const handleRejectConfirm = () => {
    if (!rejectTarget) return;
    verifyMutation.mutate({
      submissionId: rejectTarget.submissionId,
      action: 'REJECT',
      rejectReason: rejectReason.trim() || undefined,
    });
  };

  return (
    <div className={styles.page}>
      {isLoading ? (
        <p className={styles.loading}>목록을 불러오는 중입니다...</p>
      ) : submissions.length === 0 ? (
        <div className={styles.empty}>
          <CircleCheck size={48} className={styles.emptyIcon} strokeWidth={1.5} />
          <h3>검토 대기 제출이 없습니다</h3>
          <p>새로운 제출이 들어오면 이 화면에서 바로 확인할 수 있습니다.</p>
        </div>
      ) : (
        <div className={styles.grid}>
          {submissions.map((item) => (
            <article key={item.submissionId} className={styles.card}>
              <div className={styles.photoWrap}>
                <img
                  src={item.photoUrl}
                  alt={`제출 #${item.submissionId}`}
                  className={styles.photo}
                  referrerPolicy="no-referrer"
                />
                <span className={styles.idBadge}>#{item.submissionId}</span>
              </div>

              <div className={styles.meta}>
                <p className={styles.spotTitle}>{item.spotTitle}</p>
                <p className={styles.user}>@{item.userNickname || '(닉네임 없음)'}</p>
                <p className={styles.date}>{new Date(item.submittedAt).toLocaleString('ko-KR')}</p>
              </div>

              <div className={styles.actions}>
                <Button
                  variant="primary"
                  className={styles.compactApprove}
                  onClick={() => handleApprove(item)}
                  disabled={verifyMutation.isPending}
                >
                  <CircleCheck size={14} />
                  승인
                </Button>
                <Button
                  variant="danger"
                  className={styles.iconReject}
                  onClick={() => handleRejectClick(item)}
                  disabled={verifyMutation.isPending}
                  title="거절"
                  aria-label="거절"
                >
                  <Ban size={14} />
                </Button>
              </div>
            </article>
          ))}
        </div>
      )}

      {rejectTarget && (
        <Modal
          open
          title="거절 사유 입력"
          onClose={() => setRejectTarget(null)}
          footer={
            <>
              <Button variant="secondary" onClick={() => setRejectTarget(null)}>
                취소
              </Button>
              <Button
                variant="danger"
                onClick={handleRejectConfirm}
                disabled={verifyMutation.isPending}
              >
                {verifyMutation.isPending ? '처리 중…' : '거절 확정'}
              </Button>
            </>
          }
        >
          <p className={styles.modalSpot}>
            <Camera size={14} />
            {rejectTarget.spotTitle}
          </p>
          <Input
            label="거절 사유 (선택)"
            value={rejectReason}
            onChange={(e) => setRejectReason(e.target.value)}
            placeholder="예: 가이드라인과 맞지 않는 이미지"
          />
        </Modal>
      )}
    </div>
  );
}
