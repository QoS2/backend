import { getJson, postJson, patchJson, deleteNoContent } from '../utils/api';

export type TourAdminResponse = {
  id: number;
  externalKey: string;
  titleEn: string;
  descriptionEn: string | null;
  infoJson: Record<string, unknown> | null;
  goodToKnowJson: Record<string, unknown> | null;
  stepsCount: number;
  waypointsCount: number;
  photoSpotsCount: number;
  treasuresCount: number;
  quizzesCount: number;
};

export type TourCreateRequest = {
  externalKey: string;
  titleEn: string;
  descriptionEn?: string;
  infoJson?: Record<string, unknown>;
  goodToKnowJson?: Record<string, unknown>;
};

export type TourUpdateRequest = {
  titleEn?: string;
  descriptionEn?: string;
  infoJson?: Record<string, unknown>;
  goodToKnowJson?: Record<string, unknown>;
};

export type StepAdminResponse = {
  id: number;
  externalKey: string;
  tourId: number;
  stepOrder: number;
  titleEn: string | null;
  shortDescEn: string | null;
  latitude: number | null;
  longitude: number | null;
  radiusM: number | null;
};

export type StepCreateRequest = {
  externalKey: string;
  stepOrder: number;
  titleEn: string;
  shortDescEn?: string;
  latitude?: number;
  longitude?: number;
  radiusM?: number;
};

export type StepUpdateRequest = {
  stepOrder?: number;
  titleEn?: string;
  shortDescEn?: string;
  latitude?: number;
  longitude?: number;
  radiusM?: number;
};

const TOURS_BASE = '/tours';

export async function fetchTours(page = 0, size = 20) {
  const res = await getJson<{
    content: TourAdminResponse[];
    totalPages: number;
    totalElements: number;
  }>(`${TOURS_BASE}?page=${page}&size=${size}`);
  return res;
}

export async function fetchTour(tourId: number) {
  return getJson<TourAdminResponse>(`${TOURS_BASE}/${tourId}`);
}

export async function createTour(body: TourCreateRequest) {
  return postJson<TourAdminResponse, TourCreateRequest>(TOURS_BASE, body);
}

export async function updateTour(tourId: number, body: TourUpdateRequest) {
  return patchJson<TourAdminResponse, TourUpdateRequest>(`${TOURS_BASE}/${tourId}`, body);
}

export async function deleteTour(tourId: number) {
  return deleteNoContent(`${TOURS_BASE}/${tourId}`);
}

export async function fetchSteps(tourId: number) {
  return getJson<StepAdminResponse[]>(`${TOURS_BASE}/${tourId}/steps`);
}

export async function createStep(tourId: number, body: StepCreateRequest) {
  return postJson<StepAdminResponse, StepCreateRequest>(`${TOURS_BASE}/${tourId}/steps`, body);
}

export async function updateStep(
  tourId: number,
  stepId: number,
  body: StepUpdateRequest
) {
  return patchJson<StepAdminResponse, StepUpdateRequest>(
    `${TOURS_BASE}/${tourId}/steps/${stepId}`,
    body
  );
}

export async function deleteStep(tourId: number, stepId: number) {
  return deleteNoContent(`${TOURS_BASE}/${tourId}/steps/${stepId}`);
}

/** Mobile Preview AI Chat */
export async function previewChat(
  tourId: number,
  text: string,
  history: Array<{ role: string; content: string }> = []
) {
  return postJson<{ aiText: string }, { text: string; history: Array<{ role: string; content: string }> }>(
    `${TOURS_BASE}/${tourId}/preview/chat`,
    {
      text,
      history: history.map((h) => ({ role: h.role, content: h.content })),
    }
  );
}
