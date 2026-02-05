/** Admin API response/request types aligned */

export type QuestTheme =
  | 'HISTORY'
  | 'FOOD'
  | 'FUN'
  | 'PERSON'
  | 'ARCHITECTURE'
  | 'SENSORY';
export type QuestTone = 'SERIOUS' | 'FRIENDLY' | 'PLAYFUL' | 'EMOTIONAL';
export type Difficulty = 'EASY' | 'NORMAL' | 'DEEP';
export type NodeType =
  | 'LOCATION'
  | 'WALK'
  | 'VIEW'
  | 'EAT'
  | 'LISTEN'
  | 'REFLECTION';
export type ContentType = 'TEXT' | 'AUDIO' | 'AI_PROMPT';
export type ActionType =
  | 'CHOICE'
  | 'PHOTO'
  | 'TEXT_INPUT'
  | 'TIMER'
  | 'EAT_CONFIRM';
export type EffectType = 'TAG' | 'PROGRESS' | 'MEMORY' | 'SCORE';
export type Language = 'KO' | 'EN' | 'JP' | 'CN';
export type DisplayMode = 'PARAGRAPH' | 'SUBTITLE' | 'QUOTE';
export type TransitionMessageType = 'TEXT' | 'AUDIO' | 'AI_GENERATED';

export interface Quest {
  id: string;
  title: string;
  subtitle: string | null;
  theme: QuestTheme;
  tone: QuestTone;
  difficulty: Difficulty;
  estimatedMinutes: number | null;
  startLocationLongitude: number | null;
  startLocationLatitude: number | null;
  isActive: boolean;
  createdAt: string;
}

export interface QuestCreateRequest {
  title: string;
  subtitle?: string;
  theme: QuestTheme;
  tone: QuestTone;
  difficulty: Difficulty;
  estimatedMinutes?: number;
  startLocationLatitude?: number;
  startLocationLongitude?: number;
}

export interface QuestUpdateRequest {
  title?: string;
  subtitle?: string;
  theme?: QuestTheme;
  tone?: QuestTone;
  difficulty?: Difficulty;
  estimatedMinutes?: number;
  startLocationLatitude?: number;
  startLocationLongitude?: number;
  isActive?: boolean;
}

export interface Node {
  id: string;
  questId: string;
  nodeType: NodeType;
  title: string;
  orderIndex: number;
  geoLatitude: number | null;
  geoLongitude: number | null;
  unlockCondition: Record<string, unknown> | null;
  createdAt: string;
}

export interface NodeCreateRequest {
  nodeType: NodeType;
  title: string;
  orderIndex: number;
  geoLatitude?: number;
  geoLongitude?: number;
  unlockCondition?: Record<string, unknown>;
}

export interface NodeUpdateRequest {
  nodeType?: NodeType;
  title?: string;
  orderIndex?: number;
  geoLatitude?: number;
  geoLongitude?: number;
  unlockCondition?: Record<string, unknown>;
}

export interface NodeReorderItem {
  nodeId: string;
  orderIndex: number;
}

export interface Content {
  id: string;
  nodeId: string;
  contentOrder: number;
  contentType: ContentType;
  language: Language;
  body: string;
  audioUrl: string | null;
  voiceStyle: string | null;
  displayMode: DisplayMode;
  createdAt: string;
}

export interface ContentCreateRequest {
  contentOrder: number;
  contentType: ContentType;
  language: Language;
  body: string;
  audioUrl?: string;
  voiceStyle?: string;
  displayMode?: DisplayMode;
}

export interface ContentUpdateRequest {
  contentOrder?: number;
  contentType?: ContentType;
  language?: Language;
  body?: string;
  audioUrl?: string;
  voiceStyle?: string;
  displayMode?: DisplayMode;
}

export interface Action {
  id: string;
  nodeId: string;
  actionType: ActionType;
  prompt: string;
  options: Record<string, unknown> | null;
  createdAt: string;
  effects?: Effect[];
}

export interface ActionCreateRequest {
  actionType: ActionType;
  prompt: string;
  options?: Record<string, unknown>;
}

export interface ActionUpdateRequest {
  actionType?: ActionType;
  prompt?: string;
  options?: Record<string, unknown>;
}

export interface Effect {
  id: string;
  actionId: string;
  effectType: EffectType;
  effectValue: Record<string, unknown>;
  createdAt: string;
}

export interface EffectCreateRequest {
  effectType: EffectType;
  effectValue: Record<string, unknown>;
}

export interface EffectUpdateRequest {
  effectType?: EffectType;
  effectValue?: Record<string, unknown>;
}

export interface Transition {
  id: string;
  fromNodeId: string;
  toNodeId: string;
  transitionOrder: number;
  messageType: TransitionMessageType;
  textContent: string | null;
  audioUrl: string | null;
  language: Language;
  createdAt: string;
}

export interface TransitionCreateRequest {
  fromNodeId: string;
  toNodeId: string;
  transitionOrder: number;
  messageType: TransitionMessageType;
  textContent?: string;
  audioUrl?: string;
  language?: Language;
}

export interface TransitionUpdateRequest {
  transitionOrder?: number;
  messageType?: TransitionMessageType;
  textContent?: string;
  audioUrl?: string;
  language?: Language;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface ErrorResponse {
  error: string;
  errorCode: string;
  message: string;
  path?: string;
  errors?: Record<string, string>;
}
