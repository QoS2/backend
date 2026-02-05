import { useQuery, type UseQueryResult } from '@tanstack/react-query';
import { adminApi } from '../../../api/admin';
import type { Content } from '../../../types/admin';

export function useContents(
  questId: string | null,
  nodeId: string | null
): UseQueryResult<Content[], Error> {
  return useQuery({
    queryKey: ['admin', 'contents', questId, nodeId],
    queryFn: () => adminApi.contents.list(questId!, nodeId!),
    enabled: !!questId && !!nodeId,
  });
}
