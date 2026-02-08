import { getAccessToken } from './auth';
import { UPLOAD_URL } from '../config/constants';

export interface UploadResponse {
  url: string;
}

/**
 * 파일을 S3에 업로드하고 URL을 반환합니다.
 * @param file 업로드할 파일 (이미지 또는 오디오)
 * @param type 파일 타입 (image | audio) - 생략 시 Content-Type으로 자동 판별
 */
export async function uploadFile(
  file: File,
  type?: 'image' | 'audio'
): Promise<string> {
  const token = getAccessToken();
  const formData = new FormData();
  formData.append('file', file);

  const url = type ? `${UPLOAD_URL}?type=${type}` : UPLOAD_URL;
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
