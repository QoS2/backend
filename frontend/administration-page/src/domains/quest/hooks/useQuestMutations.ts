import { useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '../../../api/admin';
import type {
  QuestCreateRequest,
  QuestUpdateRequest,
} from '../../../types/admin';

const QUERY_KEY_QUESTS = ['admin', 'quests'];

export function useQuestMutations() {
  const queryClient = useQueryClient();

  const invalidateQuests = () => {
    queryClient.invalidateQueries({ queryKey: QUERY_KEY_QUESTS });
  };

  const create = useMutation({
    mutationFn: (body: QuestCreateRequest) => adminApi.quests.create(body),
    onSuccess: invalidateQuests,
  });

  const update = useMutation({
    mutationFn: ({
      questId,
      body,
    }: {
      questId: string;
      body: QuestUpdateRequest;
    }) => adminApi.quests.update(questId, body),
    onSuccess: (_, { questId }) => {
      invalidateQuests();
      queryClient.invalidateQueries({ queryKey: ['admin', 'quest', questId] });
    },
  });

  const remove = useMutation({
    mutationFn: (questId: string) => adminApi.quests.delete(questId),
    onSuccess: invalidateQuests,
  });

  const setActive = useMutation({
    mutationFn: ({
      questId,
      active,
    }: {
      questId: string;
      active: boolean;
    }) => adminApi.quests.setActive(questId, active),
    onSuccess: (_, { questId }) => {
      invalidateQuests();
      queryClient.invalidateQueries({ queryKey: ['admin', 'quest', questId] });
    },
  });

  return { create, update, remove, setActive };
}
