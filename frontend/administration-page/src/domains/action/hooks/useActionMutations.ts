import { useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '../../../api/admin';
import type {
  ActionCreateRequest,
  ActionUpdateRequest,
} from '../../../types/admin';

export function useActionMutations(questId: string, nodeId: string) {
  const queryClient = useQueryClient();

  const invalidateActions = () => {
    queryClient.invalidateQueries({
      queryKey: ['admin', 'actions', questId, nodeId],
    });
  };

  const create = useMutation({
    mutationFn: (body: ActionCreateRequest) =>
      adminApi.actions.create(questId, nodeId, body),
    onSuccess: invalidateActions,
  });

  const update = useMutation({
    mutationFn: ({
      actionId,
      body,
    }: {
      actionId: string;
      body: ActionUpdateRequest;
    }) =>
      adminApi.actions.update(questId, nodeId, actionId, body),
    onSuccess: invalidateActions,
  });

  const remove = useMutation({
    mutationFn: (actionId: string) =>
      adminApi.actions.delete(questId, nodeId, actionId),
    onSuccess: invalidateActions,
  });

  return { create, update, remove };
}
