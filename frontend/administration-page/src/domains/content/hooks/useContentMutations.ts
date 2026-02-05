import { useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '../../../api/admin';
import type {
  ContentCreateRequest,
  ContentUpdateRequest,
} from '../../../types/admin';

export function useContentMutations(questId: string, nodeId: string) {
  const queryClient = useQueryClient();

  const invalidateContents = () => {
    queryClient.invalidateQueries({
      queryKey: ['admin', 'contents', questId, nodeId],
    });
  };

  const create = useMutation({
    mutationFn: (body: ContentCreateRequest) =>
      adminApi.contents.create(questId, nodeId, body),
    onSuccess: invalidateContents,
  });

  const update = useMutation({
    mutationFn: ({
      contentId,
      body,
    }: {
      contentId: string;
      body: ContentUpdateRequest;
    }) =>
      adminApi.contents.update(questId, nodeId, contentId, body),
    onSuccess: invalidateContents,
  });

  const remove = useMutation({
    mutationFn: (contentId: string) =>
      adminApi.contents.delete(questId, nodeId, contentId),
    onSuccess: invalidateContents,
  });

  return { create, update, remove };
}
