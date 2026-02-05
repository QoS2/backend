import { useQuery, type UseQueryResult } from '@tanstack/react-query';
import { adminApi } from '../../../api/admin';
import type { Action } from '../../../types/admin';

export function useActions(
  questId: string | null,
  nodeId: string | null
): UseQueryResult<Action[], Error> {
  return useQuery({
    queryKey: ['admin', 'actions', questId, nodeId],
    queryFn: () => adminApi.actions.list(questId!, nodeId!),
    enabled: !!questId && !!nodeId,
  });
}
