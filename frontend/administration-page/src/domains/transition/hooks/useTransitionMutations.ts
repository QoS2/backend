import { useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '../../../api/admin';
import type {
  TransitionCreateRequest,
  TransitionUpdateRequest,
} from '../../../types/admin';

const QUERY_KEY_TRANSITIONS = ['admin', 'transitions'];

export function useTransitionMutations(questId: string) {
  const queryClient = useQueryClient();

  const invalidateTransitions = () => {
    queryClient.invalidateQueries({
      queryKey: QUERY_KEY_TRANSITIONS,
    });
  };

  const create = useMutation({
    mutationFn: (body: TransitionCreateRequest) =>
      adminApi.transitions.create(questId, body),
    onSuccess: invalidateTransitions,
  });

  const update = useMutation({
    mutationFn: ({
      transitionId,
      body,
    }: {
      transitionId: string;
      body: TransitionUpdateRequest;
    }) =>
      adminApi.transitions.update(questId, transitionId, body),
    onSuccess: invalidateTransitions,
  });

  const remove = useMutation({
    mutationFn: (transitionId: string) =>
      adminApi.transitions.delete(questId, transitionId),
    onSuccess: invalidateTransitions,
  });

  return { create, update, remove };
}
