import { useQuery, type UseQueryResult } from '@tanstack/react-query';
import { adminApi } from '../../../api/admin';
import type { Quest } from '../../../types/admin';

export function useQuest(questId: string | null): UseQueryResult<Quest | null, Error> {
  return useQuery({
    queryKey: ['admin', 'quest', questId],
    queryFn: async () => {
      if (!questId) return null;
      return adminApi.quests.get(questId);
    },
    enabled: !!questId,
  });
}
