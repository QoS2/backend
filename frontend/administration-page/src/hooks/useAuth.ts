import { useQuery, type UseQueryResult } from '@tanstack/react-query';
import {
  clearAccessToken,
  fetchAuthMe,
  fetchAuthToken,
  getAccessToken,
  type AuthMeResponse,
} from '../api/auth';

const AUTH_QUERY_KEY = ['auth', 'me'] as const;

async function authQueryFn(): Promise<AuthMeResponse> {
  const token = getAccessToken();
  if (token) {
    try {
      const data = await fetchAuthMe();
      return data;
    } catch (e) {
      clearAccessToken();
      if (e instanceof Error && e.message !== 'UNAUTHORIZED') throw e;
    }
  }
  try {
    const data = await fetchAuthMe({ useCredentials: true });
    await fetchAuthToken();
    return data;
  } catch {
    throw new Error('UNAUTHORIZED');
  }
}

export function useAuth(): UseQueryResult<AuthMeResponse, Error> {
  return useQuery({
    queryKey: AUTH_QUERY_KEY,
    queryFn: authQueryFn,
    retry: false,
    staleTime: 5 * 60 * 1000,
  });
}

export { AUTH_QUERY_KEY };
