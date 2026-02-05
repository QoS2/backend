import { useQuery, type UseQueryResult } from '@tanstack/react-query';
import { adminApi } from '../../../api/admin';
import type { Transition } from '../../../types/admin';

export function useTransitions(
  questId: string | null
): UseQueryResult<Transition[], Error> {
  return useQuery({
    queryKey: ['admin', 'transitions', questId],
    queryFn: () => adminApi.transitions.list(questId!),
    enabled: !!questId,
  });
}

export function useOutgoingTransitions(
  questId: string | null,
  nodeId: string | null
): UseQueryResult<Transition[], Error> {
  return useQuery({
    queryKey: ['admin', 'transitions', 'outgoing', questId, nodeId],
    queryFn: () => adminApi.transitions.outgoing(questId!, nodeId!),
    enabled: !!questId && !!nodeId,
  });
}

export function useIncomingTransitions(
  questId: string | null,
  nodeId: string | null
): UseQueryResult<Transition[], Error> {
  return useQuery({
    queryKey: ['admin', 'transitions', 'incoming', questId, nodeId],
    queryFn: () => adminApi.transitions.incoming(questId!, nodeId!),
    enabled: !!questId && !!nodeId,
  });
}
