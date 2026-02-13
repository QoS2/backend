import { postJson } from '../utils/api';

const RAG_BASE = '/rag';

export type RagSyncResponse = {
  embeddingsCount: number;
};

/** 투어 지식 벡터 동기화 */
export async function syncRag(tourId?: number): Promise<RagSyncResponse> {
  const url = tourId != null ? `${RAG_BASE}/sync?tourId=${tourId}` : `${RAG_BASE}/sync`;
  return postJson<RagSyncResponse, Record<string, never>>(url, {});
}
