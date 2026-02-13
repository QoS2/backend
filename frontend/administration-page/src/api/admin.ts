import { getJson } from '../utils/api';

export const adminApi = {
  enums: {
    get: (enumName: string) =>
      getJson<string[]>(`/enums/${enumName}`) as Promise<string[]>,
  },
};
