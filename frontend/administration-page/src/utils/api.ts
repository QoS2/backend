import { getAccessToken } from '../api/auth';
import { ADMIN_BASE } from '../config/constants';
import type { ErrorResponse } from '../types/admin';

const DEFAULT_HEADERS: HeadersInit = {
  'Content-Type': 'application/json',
};

function buildHeaders(overrides?: HeadersInit): HeadersInit {
  const token = getAccessToken();
  const headers: Record<string, string> = { ...DEFAULT_HEADERS } as Record<string, string>;
  if (token) headers['Authorization'] = `Bearer ${token}`;
  if (overrides) {
    const o = overrides as Record<string, string>;
    Object.keys(o).forEach((k) => {
      const v = o[k];
      if (v != null && v !== '') headers[k] = String(v);
    });
  }
  return headers;
}

/** Fetch with JWT (if stored) or session cookie; throws on non-ok with parsed ErrorResponse */
export async function fetchApi(
  path: string,
  options: RequestInit = {}
): Promise<Response> {
  const url = path.startsWith('http') ? path : `${ADMIN_BASE}${path}`;
  const res = await fetch(url, {
    ...options,
    credentials: 'include',
    headers: buildHeaders(options.headers as HeadersInit),
  });
  if (!res.ok) {
    let body: ErrorResponse | string;
    try {
      body = (await res.json()) as ErrorResponse;
    } catch {
      body = await res.text();
    }
    const message =
      typeof body === 'object' && body?.message ? body.message : String(body);
    throw new Error(message);
  }
  return res;
}

export async function getJson<T>(path: string): Promise<T> {
  const res = await fetchApi(path, { method: 'GET' });
  return res.json() as Promise<T>;
}

export async function postJson<T, B>(path: string, body: B): Promise<T> {
  const res = await fetchApi(path, {
    method: 'POST',
    body: JSON.stringify(body),
  });
  return res.json() as Promise<T>;
}

export async function patchJson<T, B>(path: string, body: B): Promise<T> {
  const res = await fetchApi(path, {
    method: 'PATCH',
    body: JSON.stringify(body),
  });
  return res.json() as Promise<T>;
}

export async function deleteNoContent(path: string): Promise<void> {
  await fetchApi(path, { method: 'DELETE' });
}
