import { getAccessToken } from './auth';
import { UPLOAD_URL } from '../config/constants';

export interface UploadResponse {
  url: string;
}

/**
 * Upload file to S3 and return URL
 * @param file File to upload (image or audio)
 * @param type File type (image | audio) - auto-detected by Content-Type if omitted
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
