import { useQuery, type UseQueryResult } from '@tanstack/react-query';
import { adminApi } from '../../../api/admin';
import type { Effect } from '../../../types/admin';

export function useEffects(
  questId: string | null,
  nodeId: string | null,
  actionId: string | null,
  options?: { enabled?: boolean }
): UseQueryResult<Effect[], Error> {
  const enabled =
    options?.enabled !== false &&
    !!questId &&
    !!nodeId &&
    !!actionId;
  return useQuery({
    queryKey: ['admin', 'effects', questId, nodeId, actionId],
    queryFn: () =>
      adminApi.effects.list(questId!, nodeId!, actionId!),
    enabled,
  });
}
