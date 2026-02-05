import { useQuery, type UseQueryResult } from '@tanstack/react-query';
import { adminApi } from '../../../api/admin';
import type { Quest } from '../../../types/admin';
import type { PageResponse } from '../../../types/admin';
import { DEFAULT_PAGE_SIZE } from '../../../config/constants';

interface UseQuestsParams {
  isActive?: boolean;
  theme?: string;
  page: number;
  size?: number;
}

export function useQuests(params: UseQuestsParams): UseQueryResult<PageResponse<Quest>, Error> {
  const { isActive, theme, page, size = DEFAULT_PAGE_SIZE } = params;
  return useQuery({
    queryKey: ['admin', 'quests', isActive, theme, page, size],
    queryFn: () =>
      adminApi.quests.list({
        isActive,
        theme: theme ?? undefined,
        page,
        size,
      }),
  });
}
