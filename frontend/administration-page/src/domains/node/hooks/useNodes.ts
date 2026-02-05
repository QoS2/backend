import { useQuery, type UseQueryResult } from '@tanstack/react-query';
import { adminApi } from '../../../api/admin';
import type { Node } from '../../../types/admin';

export function useNodes(questId: string | null): UseQueryResult<Node[], Error> {
  return useQuery({
    queryKey: ['admin', 'nodes', questId],
    queryFn: () => adminApi.nodes.list(questId!),
    enabled: !!questId,
  });
}
