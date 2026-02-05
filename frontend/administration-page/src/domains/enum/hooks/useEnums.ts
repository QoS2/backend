import { useQuery, type UseQueryResult } from '@tanstack/react-query';
import { adminApi } from '../../../api/admin';

export function useEnums(enumName: string): UseQueryResult<string[], Error> {
  return useQuery({
    queryKey: ['admin', 'enums', enumName],
    queryFn: () => adminApi.enums.get(enumName),
    enabled: !!enumName,
  });
}
