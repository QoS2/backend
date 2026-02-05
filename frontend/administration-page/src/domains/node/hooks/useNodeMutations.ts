import { useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '../../../api/admin';
import type { NodeCreateRequest, NodeUpdateRequest, NodeReorderItem } from '../../../types/admin';

export function useNodeMutations(questId: string) {
  const queryClient = useQueryClient();

  const invalidateNodes = () => {
    queryClient.invalidateQueries({ queryKey: ['admin', 'nodes', questId] });
  };

  const create = useMutation({
    mutationFn: (body: NodeCreateRequest) => adminApi.nodes.create(questId, body),
    onSuccess: invalidateNodes,
  });

  const update = useMutation({
    mutationFn: ({
      nodeId,
      body,
    }: {
      nodeId: string;
      body: NodeUpdateRequest;
    }) => adminApi.nodes.update(questId, nodeId, body),
    onSuccess: invalidateNodes,
  });

  const remove = useMutation({
    mutationFn: (nodeId: string) => adminApi.nodes.delete(questId, nodeId),
    onSuccess: invalidateNodes,
  });

  const reorder = useMutation({
    mutationFn: (body: { nodes: NodeReorderItem[] }) =>
      adminApi.nodes.reorder(questId, body),
    onSuccess: invalidateNodes,
  });

  return { create, update, remove, reorder };
}
