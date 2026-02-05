import {
  AUTH_LOGIN_URL,
  AUTH_ME_URL,
  AUTH_REGISTER_URL,
  AUTH_TOKEN_URL,
} from '../config/constants';

const ACCESS_TOKEN_KEY = 'quest_admin_access_token';

export interface AuthMeResponse {
  userId: string;
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

/** Fetch current user; throws on non-2xx (e.g. 401 unauthenticated). */
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

/** JWT 로그인 (이메일 + 비밀번호). 성공 시 토큰 저장 후 반환. */
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

/** 회원가입. 성공 시 토큰 저장 후 반환. */
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

/** OAuth2 로그인 후 세션으로 JWT 발급. */
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
