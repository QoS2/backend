import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Camera } from 'lucide-react';
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
      showSuccess(action === 'APPROVE' ? '승인되었습니다.' : '거절되었습니다.');
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
      <div className={styles.header}>
        <h1 className={styles.title}>포토 제출 검토</h1>
        <p className={styles.subtitle}>
          PHOTO 스팟에서 유저가 제출한 사진을 검토하여 승인 또는 거절합니다.
        </p>
      </div>

      {isLoading ? (
        <p className={styles.loading}>로딩 중...</p>
      ) : submissions.length === 0 ? (
        <div className={styles.empty}>
          <Camera size={48} className={styles.emptyIcon} strokeWidth={1.5} />
          <h3>검토 대기 중인 제출이 없습니다</h3>
          <p>유저가 PHOTO 스팟에 사진을 제출하면 여기에 표시됩니다.</p>
        </div>
      ) : (
        <div className={styles.grid}>
          {submissions.map((item) => (
            <div key={item.submissionId} className={styles.card}>
              <img
                src={item.photoUrl}
                alt={`제출 #${item.submissionId}`}
                className={styles.photo}
                referrerPolicy="no-referrer"
              />
              <div className={styles.meta}>
                <p className={styles.spotTitle}>{item.spotTitle}</p>
                <p className={styles.user}>@{item.userNickname || '(닉네임 없음)'}</p>
                <p className={styles.date}>
                  {new Date(item.submittedAt).toLocaleString('ko-KR')}
                </p>
              </div>
              <div className={styles.actions}>
                <Button
                  variant="primary"
                  onClick={() => handleApprove(item)}
                  disabled={verifyMutation.isPending}
                >
                  승인
                </Button>
                <Button
                  variant="danger"
                  onClick={() => handleRejectClick(item)}
                  disabled={verifyMutation.isPending}
                >
                  거절
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}

      {rejectTarget && (
        <Modal
          open
          title="거절 사유"
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
                {verifyMutation.isPending ? '처리 중…' : '거절'}
              </Button>
            </>
          }
        >
          <p className={styles.modalSpot}>{rejectTarget.spotTitle}</p>
          <Input
            label="거절 사유 (선택)"
            value={rejectReason}
            onChange={(e) => setRejectReason(e.target.value)}
            placeholder="거절 이유를 입력하세요 (유저에게 전달됨)"
          />
        </Modal>
      )}
    </div>
  );
}
