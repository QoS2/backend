import { getJson, postJson, patchJson, putJson, deleteNoContent } from '../utils/api';

// --- 사용자 API (base: 'api', 미리보기용) ---
export type TourDetailResponse = {
  tourId: number;
  title: string;
  description: string | null;
  tags: Array<{ id: number; name: string; slug: string }>;
  counts: { main: number; sub: number; photo: number; treasure: number; missions: number };
  info: Record<string, unknown> | null;
  goodToKnow: string[];
  startSpot: { spotId: number; title: string; lat: number; lng: number; radiusM: number } | null;
  mapSpots: Array<{ spotId: number; type: string; title: string; lat: number; lng: number }>;
  access: { status: string; hasAccess: boolean };
  currentRun: { runId: number; status: string; startedAt: string; progress: { completedSpots: number; totalSpots: number } } | null;
  actions: { primaryButton: string; secondaryButton: string | null; moreActions: string[] };
};

export type MarkerResponse = {
  id: number;
  type: string;
  title: string;
  latitude: number;
  longitude: number;
  radiusM: number;
  refId: number;
  stepOrder: number;
};

export type SpotGuideResponse = {
  stepId: number;
  stepTitle: string;
  segments: Array<{
    id: number;
    segIdx: number;
    textEn: string;
    triggerKey: string | null;
    media: Array<{ id: number; url: string; meta: unknown }>;
  }>;
};

/** 사용자용 투어 디테일 (미리보기) */
export async function fetchTourDetail(tourId: number) {
  return getJson<TourDetailResponse>(`/tours/${tourId}`, { base: 'api' });
}

/** 사용자용 마커 목록 (미리보기) */
export async function fetchMarkers(tourId: number, filter?: string) {
  const q = filter ? `?filter=${filter}` : '';
  return getJson<MarkerResponse[]>(`/tours/${tourId}/markers${q}`, { base: 'api' });
}

/** 사용자용 스팟 가이드 (미리보기) */
export async function fetchSpotGuide(spotId: number, lang?: string) {
  const q = lang ? `?lang=${lang}` : '';
  return getJson<SpotGuideResponse>(`/spots/${spotId}/guide${q}`, { base: 'api' });
}

// --- 관리자 API (base: 'admin') ---

export type TourAdminResponse = {
  id: number;
  externalKey: string;
  titleEn: string;
  descriptionEn: string | null;
  infoJson: Record<string, unknown> | null;
  goodToKnowJson: Record<string, unknown> | null;
  mainCount: number;
  subCount: number;
  photoSpotsCount: number;
  treasuresCount: number;
  missionsCount: number;
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

export type SpotAdminResponse = {
  id: number;
  tourId: number;
  type: string;
  title: string;
  description: string | null;
  latitude: number | null;
  longitude: number | null;
  radiusM: number | null;
  orderIndex: number;
};

export type SpotCreateRequest = {
  type: string;
  title: string;
  description?: string;
  latitude?: number;
  longitude?: number;
  orderIndex: number;
  radiusM?: number;
};

export type SpotUpdateRequest = {
  title?: string;
  description?: string;
  orderIndex?: number;
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

export async function fetchSpots(tourId: number) {
  return getJson<SpotAdminResponse[]>(`${TOURS_BASE}/${tourId}/spots`);
}

export async function createSpot(tourId: number, body: SpotCreateRequest) {
  return postJson<SpotAdminResponse, SpotCreateRequest>(`${TOURS_BASE}/${tourId}/spots`, body);
}

export async function updateSpot(
  tourId: number,
  spotId: number,
  body: SpotUpdateRequest
) {
  return patchJson<SpotAdminResponse, SpotUpdateRequest>(
    `${TOURS_BASE}/${tourId}/spots/${spotId}`,
    body
  );
}

export async function deleteSpot(tourId: number, spotId: number) {
  return deleteNoContent(`${TOURS_BASE}/${tourId}/spots/${spotId}`);
}

// --- 가이드 (Guide) API ---

export type GuideAssetRequest = {
  url: string;
  assetType: 'IMAGE' | 'AUDIO';
  usage: 'ILLUSTRATION' | 'SCRIPT_AUDIO';
};

export type GuideLineRequest = {
  text: string;
  assets: GuideAssetRequest[];
};

export type GuideSaveRequest = {
  language: string;
  stepTitle?: string;
  lines: GuideLineRequest[];
};

export type GuideAssetResponse = {
  id: number;
  url: string;
  assetType: string;
  usage: string;
};

export type GuideLineResponse = {
  id: number;
  seq: number;
  text: string;
  assets: GuideAssetResponse[];
};

export type GuideAdminResponse = {
  stepId: number | null;
  language: string;
  stepTitle: string;
  lines: GuideLineResponse[];
};

export async function fetchGuide(tourId: number, spotId: number) {
  return getJson<GuideAdminResponse>(`${TOURS_BASE}/${tourId}/spots/${spotId}/guide`);
}

export async function saveGuide(
  tourId: number,
  spotId: number,
  body: GuideSaveRequest
) {
  return putJson<GuideAdminResponse, GuideSaveRequest>(
    `${TOURS_BASE}/${tourId}/spots/${spotId}/guide`,
    body
  );
}
