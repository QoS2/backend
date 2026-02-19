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
  mapSpots: Array<{ spotId: number; type: string; title: string; lat: number; lng: number; thumbnailUrl: string | null; isHighlight: boolean }>;
  access: { status: string; hasAccess: boolean };
  currentRun: { runId: number; status: string; startedAt: string; progress: { completedCount: number; totalCount: number; completedSpotIds: number[] } } | null;
  actions: { primaryButton: string; secondaryButton: string | null; moreActions?: string[] | null };
  mainMissionPath?: Array<{
    spotId: number;
    spotTitle: string;
    orderIndex: number;
    missions: Array<{ stepId: number; missionId: number | null; title: string }>;
  }>;
};

export type SpotGuideResponse = {
  stepId: number;
  stepTitle: string;
  nextAction?: string | null;
  segments: Array<{
    id: number;
    segIdx: number;
    text: string;
    triggerKey: string | null;
    assets: Array<{ id: number; type: string; url: string; meta: unknown }>;
    delayMs: number;
  }>;
};

/** 사용자용 투어 디테일 (미리보기) */
export async function fetchTourDetail(tourId: number) {
  return getJson<TourDetailResponse>(`/tours/${tourId}`, { base: 'api' });
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
  titleKr: string | null;
  description: string | null;
  pronunciationUrl: string | null;
  address: string | null;
  latitude: number | null;
  longitude: number | null;
  radiusM: number | null;
  orderIndex: number;
};

export type SpotCreateRequest = {
  type: string;
  title: string;
  titleKr?: string;
  description?: string;
  pronunciationUrl?: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  orderIndex: number;
  radiusM?: number;
};

export type SpotUpdateRequest = {
  title?: string;
  titleKr?: string;
  description?: string;
  pronunciationUrl?: string;
  address?: string;
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

// --- 가이드 (Guide) API - N개 컨텐츠 블록 ---

export type GuideAssetRequest = {
  url: string;
  assetType: 'IMAGE' | 'AUDIO';
  usage: 'ILLUSTRATION' | 'SCRIPT_AUDIO';
};

export type GuideLineRequest = {
  text: string;
  assets: GuideAssetRequest[];
};

export type GuideStepSaveRequest = {
  stepTitle?: string;
  nextAction?: string;
  missionStepId?: number | null;
  lines: GuideLineRequest[];
};

export type GuideStepsSaveRequest = {
  language: string;
  steps: GuideStepSaveRequest[];
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

export type GuideStepAdminResponse = {
  stepId: number;
  stepIndex: number;
  stepTitle: string;
  nextAction?: string | null;
  missionStepId?: number | null;
  lines: GuideLineResponse[];
};

export type GuideStepsAdminResponse = {
  language: string;
  steps: GuideStepAdminResponse[];
};

export async function fetchGuideSteps(tourId: number, spotId: number) {
  return getJson<GuideStepsAdminResponse>(`${TOURS_BASE}/${tourId}/spots/${spotId}/guide`);
}

export async function saveGuideSteps(
  tourId: number,
  spotId: number,
  body: GuideStepsSaveRequest
) {
  return putJson<GuideStepsAdminResponse, GuideStepsSaveRequest>(
    `${TOURS_BASE}/${tourId}/spots/${spotId}/guide`,
    body
  );
}

// --- Tour Assets (투어 레벨 썸네일/이미지) ---

export type TourAssetResponse = {
  id: number;
  assetId: number;
  url: string;
  usage: string;
  sortOrder: number;
  caption: string | null;
};

export type TourAssetRequest = {
  url: string;
  usage: string;
  sortOrder?: number;
  caption?: string;
};

export async function fetchTourAssets(tourId: number) {
  return getJson<TourAssetResponse[]>(`${TOURS_BASE}/${tourId}/assets`);
}

export async function addTourAsset(tourId: number, body: TourAssetRequest) {
  return postJson<TourAssetResponse, TourAssetRequest>(
    `${TOURS_BASE}/${tourId}/assets`,
    body
  );
}

export async function deleteTourAsset(tourId: number, tourAssetId: number) {
  return deleteNoContent(`${TOURS_BASE}/${tourId}/assets/${tourAssetId}`);
}

// --- Mission Steps (MISSION 스텝) ---

export type MissionStepResponse = {
  stepId: number;
  missionId: number;
  missionType: string;
  prompt: string;
  optionsJson: Record<string, unknown>;
  answerJson: Record<string, unknown>;
  title: string;
  stepIndex: number;
};

export type MissionStepCreateRequest = {
  missionType: string;
  prompt: string;
  optionsJson?: Record<string, unknown>;
  answerJson?: Record<string, unknown>;
  title?: string;
};

export type MissionStepUpdateRequest = {
  prompt?: string;
  optionsJson?: Record<string, unknown>;
  answerJson?: Record<string, unknown>;
  title?: string;
};

export async function fetchMissionSteps(tourId: number, spotId: number) {
  return getJson<MissionStepResponse[]>(
    `${TOURS_BASE}/${tourId}/spots/${spotId}/mission-steps`
  );
}

export async function createMissionStep(
  tourId: number,
  spotId: number,
  body: MissionStepCreateRequest
) {
  return postJson<MissionStepResponse, MissionStepCreateRequest>(
    `${TOURS_BASE}/${tourId}/spots/${spotId}/mission-steps`,
    body
  );
}

export async function updateMissionStep(
  tourId: number,
  spotId: number,
  stepId: number,
  body: MissionStepUpdateRequest
) {
  return patchJson<MissionStepResponse, MissionStepUpdateRequest>(
    `${TOURS_BASE}/${tourId}/spots/${spotId}/mission-steps/${stepId}`,
    body
  );
}

export async function deleteMissionStep(
  tourId: number,
  spotId: number,
  stepId: number
) {
  return deleteNoContent(
    `${TOURS_BASE}/${tourId}/spots/${spotId}/mission-steps/${stepId}`
  );
}

// --- Spot Assets (스팟별 썸네일/히어로/갤러리) ---

export type SpotAssetResponse = {
  id: number;
  assetId: number;
  url: string;
  usage: string;
  sortOrder: number;
  caption: string | null;
};

export type SpotAssetRequest = {
  url: string;
  usage: string;
  sortOrder?: number;
  caption?: string;
};

export async function fetchSpotAssets(tourId: number, spotId: number) {
  return getJson<SpotAssetResponse[]>(
    `${TOURS_BASE}/${tourId}/spots/${spotId}/assets`
  );
}

export async function addSpotAsset(
  tourId: number,
  spotId: number,
  body: SpotAssetRequest
) {
  return postJson<SpotAssetResponse, SpotAssetRequest>(
    `${TOURS_BASE}/${tourId}/spots/${spotId}/assets`,
    body
  );
}

export async function deleteSpotAsset(
  tourId: number,
  spotId: number,
  spotAssetId: number
) {
  return deleteNoContent(
    `${TOURS_BASE}/${tourId}/spots/${spotId}/assets/${spotAssetId}`
  );
}
