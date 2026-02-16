import { getAccessToken } from './auth';
import { UPLOAD_URL } from '../config/constants';

export interface UploadResponse {
  url: string;
}

/**
 * Upload file to S3 and return URL
 * @param file File to upload (image or audio)
 * @param type File type (image | audio) - auto-detected by Content-Type if omitted
 * @param category Category for folder: tour, spot, mission, intro, ambient - default: general
 */
export async function uploadFile(
  file: File,
  type?: 'image' | 'audio',
  category?: string
): Promise<string> {
  const token = getAccessToken();
  const formData = new FormData();
  formData.append('file', file);

  const params = new URLSearchParams();
  if (type) params.set('type', type);
  if (category) params.set('category', category);
  const query = params.toString();
  const url = query ? `${UPLOAD_URL}?${query}` : UPLOAD_URL;
  const headers: HeadersInit = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(url, {
    method: 'POST',
    credentials: 'include',
    headers,
    body: formData,
  });

  if (!res.ok) {
    const err = (await res.json().catch(() => ({}))) as { message?: string };
    throw new Error(err?.message ?? `업로드 실패: ${res.status}`);
  }

  const data = (await res.json()) as UploadResponse;
  return data.url;
}

/** S3에 업로드된 파일을 URL로 삭제. 본 서비스 버킷 URL만 삭제 가능 */
export async function deleteUploadedFile(url: string): Promise<void> {
  const trimmed = url?.trim();
  if (!trimmed) return;
  // S3 URL이 아닌 경우(직접 입력 URL 등) 삭제 시도하지 않음
  if (!trimmed.includes('amazonaws.com') && !trimmed.includes('s3.')) return;

  const token = getAccessToken();
  const params = new URLSearchParams({ url: trimmed });
  const headers: HeadersInit = {};
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${UPLOAD_URL}?${params.toString()}`, {
    method: 'DELETE',
    credentials: 'include',
    headers,
  });

  if (!res.ok) {
    const err = (await res.json().catch(() => ({}))) as { message?: string };
    throw new Error(err?.message ?? `삭제 실패: ${res.status}`);
  }
}
