import { getJson, fetchApi } from '../utils/api';

export type PhotoSubmissionItem = {
  submissionId: number;
  spotId: number;
  spotTitle: string;
  photoUrl: string;
  status: string;
  submittedAt: string;
  userNickname: string;
};

export async function fetchPendingPhotoSubmissions() {
  return getJson<PhotoSubmissionItem[]>('/photo-submissions');
}

export async function verifyPhotoSubmission(
  submissionId: number,
  body: { action: 'APPROVE' | 'REJECT'; rejectReason?: string }
): Promise<void> {
  await fetchApi(`/photo-submissions/${submissionId}`, {
    method: 'PATCH',
    body: JSON.stringify(body),
  });
}
