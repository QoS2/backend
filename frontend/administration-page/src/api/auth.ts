import {
  AUTH_REFRESH_URL,
  AUTH_LOGIN_URL,
  AUTH_ME_URL,
  AUTH_REGISTER_URL,
  AUTH_TOKEN_URL,
} from '../config/constants';

const ACCESS_TOKEN_KEY = 'quest_admin_access_token';

export interface AuthMeResponse {
  userId: string;
  role: 'ADMIN' | 'USER';
}

export interface AuthTokenResponse {
  accessToken: string;
  expiresIn: number;
  tokenType: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  nickname?: string;
}

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setAccessToken(token: string): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, token);
}

export function clearAccessToken(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
}

/** Fetch current user */
export async function fetchAuthMe(options?: { useCredentials?: boolean }): Promise<AuthMeResponse> {
  const useCredentials = options?.useCredentials ?? false;
  const token = getAccessToken();
  const headers: HeadersInit = { Accept: 'application/json' };
  if (!useCredentials && token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const res = await fetch(AUTH_ME_URL, {
    method: 'GET',
    credentials: 'include',
    headers,
  });
  if (!res.ok) {
    throw new Error(res.status === 401 ? 'UNAUTHORIZED' : `Auth check failed: ${res.status}`);
  }
  return res.json() as Promise<AuthMeResponse>;
}

/** JWT Login */
export async function fetchLogin(body: LoginRequest): Promise<AuthTokenResponse> {
  const res = await fetch(AUTH_LOGIN_URL, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({})) as { message?: string };
    throw new Error(err?.message ?? (res.status === 401 ? 'UNAUTHORIZED' : `로그인 실패: ${res.status}`));
  }
  const data = (await res.json()) as AuthTokenResponse;
  if (data.accessToken) {
    setAccessToken(data.accessToken);
  }
  return data;
}

/** Register */
export async function fetchRegister(body: RegisterRequest): Promise<AuthTokenResponse> {
  const res = await fetch(AUTH_REGISTER_URL, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({})) as { message?: string };
    throw new Error(err?.message ?? `회원가입 실패: ${res.status}`);
  }
  const data = (await res.json()) as AuthTokenResponse;
  if (data.accessToken) {
    setAccessToken(data.accessToken);
  }
  return data;
}

/** OAuth2 Login */
export async function fetchAuthToken(): Promise<AuthTokenResponse> {
  const res = await fetch(AUTH_TOKEN_URL, {
    method: 'POST',
    credentials: 'include',
    headers: { Accept: 'application/json' },
  });
  if (!res.ok) {
    throw new Error(res.status === 401 ? 'UNAUTHORIZED' : `Token issue failed: ${res.status}`);
  }
  const data = (await res.json()) as AuthTokenResponse;
  if (data.accessToken) {
    setAccessToken(data.accessToken);
  }
  return data;
}

/** Refresh access token with HttpOnly refresh cookie */
export async function fetchAuthRefresh(): Promise<AuthTokenResponse> {
  const res = await fetch(AUTH_REFRESH_URL, {
    method: 'POST',
    credentials: 'include',
    headers: { Accept: 'application/json' },
  });
  if (!res.ok) {
    clearAccessToken();
    throw new Error(res.status === 401 ? 'UNAUTHORIZED' : `Token refresh failed: ${res.status}`);
  }
  const data = (await res.json()) as AuthTokenResponse;
  if (data.accessToken) {
    setAccessToken(data.accessToken);
  }
  return data;
}
