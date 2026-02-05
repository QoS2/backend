import { useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '../../../api/admin';
import type {
  EffectCreateRequest,
  EffectUpdateRequest,
} from '../../../types/admin';

export function useEffectMutations(
  questId: string,
  nodeId: string,
  actionId: string
) {
  const queryClient = useQueryClient();

  const invalidateEffects = () => {
    queryClient.invalidateQueries({
      queryKey: ['admin', 'effects', questId, nodeId, actionId],
    });
    queryClient.invalidateQueries({
      queryKey: ['admin', 'actions', questId, nodeId],
    });
  };

  const create = useMutation({
    mutationFn: (body: EffectCreateRequest) =>
      adminApi.effects.create(questId, nodeId, actionId, body),
    onSuccess: invalidateEffects,
  });

  const update = useMutation({
    mutationFn: ({
      effectId,
      body,
    }: {
      effectId: string;
      body: EffectUpdateRequest;
    }) =>
      adminApi.effects.update(questId, nodeId, actionId, effectId, body),
    onSuccess: invalidateEffects,
  });

  const remove = useMutation({
    mutationFn: (effectId: string) =>
      adminApi.effects.delete(questId, nodeId, actionId, effectId),
    onSuccess: invalidateEffects,
  });

  return { create, update, remove };
}
