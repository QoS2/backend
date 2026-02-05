import {
  fetchApi,
  getJson,
  postJson,
  patchJson,
  deleteNoContent,
} from '../utils/api';
import type {
  Quest,
  QuestCreateRequest,
  QuestUpdateRequest,
  Node,
  NodeCreateRequest,
  NodeUpdateRequest,
  NodeReorderItem,
  Content,
  ContentCreateRequest,
  ContentUpdateRequest,
  Action,
  ActionCreateRequest,
  ActionUpdateRequest,
  Effect,
  EffectCreateRequest,
  EffectUpdateRequest,
  Transition,
  TransitionCreateRequest,
  TransitionUpdateRequest,
  PageResponse,
} from '../types/admin';

const quests = () => '/quests';
const quest = (id: string) => `/quests/${id}`;
const nodes = (questId: string) => `/quests/${questId}/nodes`;
const node = (questId: string, nodeId: string) =>
  `/quests/${questId}/nodes/${nodeId}`;
const contents = (questId: string, nodeId: string) =>
  `/quests/${questId}/nodes/${nodeId}/contents`;
const content = (questId: string, nodeId: string, contentId: string) =>
  `${contents(questId, nodeId)}/${contentId}`;
const actions = (questId: string, nodeId: string) =>
  `/quests/${questId}/nodes/${nodeId}/actions`;
const action = (questId: string, nodeId: string, actionId: string) =>
  `${actions(questId, nodeId)}/${actionId}`;
const effects = (questId: string, nodeId: string, actionId: string) =>
  `${action(questId, nodeId, actionId)}/effects`;
const effect = (
  questId: string,
  nodeId: string,
  actionId: string,
  effectId: string
) => `${effects(questId, nodeId, actionId)}/${effectId}`;
const transitions = (questId: string) => `/quests/${questId}/transitions`;
const transition = (questId: string, transitionId: string) =>
  `${transitions(questId)}/${transitionId}`;
const nodeTransitions = (questId: string, nodeId: string) =>
  `${node(questId, nodeId)}/transitions`;

export const adminApi = {
  quests: {
    list: (params: {
      isActive?: boolean;
      theme?: string;
      page?: number;
      size?: number;
    }): Promise<PageResponse<Quest>> => {
      const sp = new URLSearchParams();
      if (params.isActive !== undefined) sp.set('isActive', String(params.isActive));
      if (params.theme != null) sp.set('theme', params.theme);
      if (params.page != null) sp.set('page', String(params.page));
      if (params.size != null) sp.set('size', String(params.size));
      const q = sp.toString();
      return getJson<PageResponse<Quest>>(`${quests()}${q ? `?${q}` : ''}`);
    },
    get: (questId: string) => getJson<Quest>(quest(questId)),
    create: (body: QuestCreateRequest) =>
      postJson<Quest, QuestCreateRequest>(quests(), body),
    update: (questId: string, body: QuestUpdateRequest) =>
      patchJson<Quest, QuestUpdateRequest>(quest(questId), body),
    delete: (questId: string) => deleteNoContent(quest(questId)),
    setActive: (questId: string, active: boolean) =>
      patchJson<Quest, unknown>(`${quest(questId)}/active?active=${active}`, {}),
  },

  nodes: {
    list: (questId: string) => getJson<Node[]>(nodes(questId)),
    get: (questId: string, nodeId: string) =>
      getJson<Node>(node(questId, nodeId)),
    create: (questId: string, body: NodeCreateRequest) =>
      postJson<Node, NodeCreateRequest>(nodes(questId), body),
    update: (questId: string, nodeId: string, body: NodeUpdateRequest) =>
      patchJson<Node, NodeUpdateRequest>(node(questId, nodeId), body),
    delete: (questId: string, nodeId: string) =>
      deleteNoContent(node(questId, nodeId)),
    reorder: (questId: string, body: { nodes: NodeReorderItem[] }) =>
      fetchApi(`${nodes(questId)}/reorder`, {
        method: 'PATCH',
        body: JSON.stringify(body),
      }).then(() => undefined),
  },

  contents: {
    list: (questId: string, nodeId: string) =>
      getJson<Content[]>(contents(questId, nodeId)),
    get: (questId: string, nodeId: string, contentId: string) =>
      getJson<Content>(content(questId, nodeId, contentId)),
    create: (questId: string, nodeId: string, body: ContentCreateRequest) =>
      postJson<Content, ContentCreateRequest>(
        contents(questId, nodeId),
        body
      ),
    update: (
      questId: string,
      nodeId: string,
      contentId: string,
      body: ContentUpdateRequest
    ) =>
      patchJson<Content, ContentUpdateRequest>(
        content(questId, nodeId, contentId),
        body
      ),
    delete: (questId: string, nodeId: string, contentId: string) =>
      deleteNoContent(content(questId, nodeId, contentId)),
  },

  actions: {
    list: (questId: string, nodeId: string) =>
      getJson<Action[]>(actions(questId, nodeId)),
    get: (questId: string, nodeId: string, actionId: string) =>
      getJson<Action>(`${action(questId, nodeId, actionId)}?includeEffects=true`),
    create: (questId: string, nodeId: string, body: ActionCreateRequest) =>
      postJson<Action, ActionCreateRequest>(actions(questId, nodeId), body),
    update: (
      questId: string,
      nodeId: string,
      actionId: string,
      body: ActionUpdateRequest
    ) =>
      patchJson<Action, ActionUpdateRequest>(
        action(questId, nodeId, actionId),
        body
      ),
    delete: (questId: string, nodeId: string, actionId: string) =>
      deleteNoContent(action(questId, nodeId, actionId)),
  },

  effects: {
    list: (questId: string, nodeId: string, actionId: string) =>
      getJson<Effect[]>(effects(questId, nodeId, actionId)),
    get: (
      questId: string,
      nodeId: string,
      actionId: string,
      effectId: string
    ) => getJson<Effect>(effect(questId, nodeId, actionId, effectId)),
    create: (
      questId: string,
      nodeId: string,
      actionId: string,
      body: EffectCreateRequest
    ) =>
      postJson<Effect, EffectCreateRequest>(
        effects(questId, nodeId, actionId),
        body
      ),
    update: (
      questId: string,
      nodeId: string,
      actionId: string,
      effectId: string,
      body: EffectUpdateRequest
    ) =>
      patchJson<Effect, EffectUpdateRequest>(
        effect(questId, nodeId, actionId, effectId),
        body
      ),
    delete: (
      questId: string,
      nodeId: string,
      actionId: string,
      effectId: string
    ) => deleteNoContent(effect(questId, nodeId, actionId, effectId)),
  },

  transitions: {
    list: (questId: string) => getJson<Transition[]>(transitions(questId)),
    get: (questId: string, transitionId: string) =>
      getJson<Transition>(transition(questId, transitionId)),
    outgoing: (questId: string, nodeId: string) =>
      getJson<Transition[]>(`${nodeTransitions(questId, nodeId)}/outgoing`),
    incoming: (questId: string, nodeId: string) =>
      getJson<Transition[]>(`${nodeTransitions(questId, nodeId)}/incoming`),
    create: (questId: string, body: TransitionCreateRequest) =>
      postJson<Transition, TransitionCreateRequest>(
        transitions(questId),
        body
      ),
    update: (
      questId: string,
      transitionId: string,
      body: TransitionUpdateRequest
    ) =>
      patchJson<Transition, TransitionUpdateRequest>(
        transition(questId, transitionId),
        body
      ),
    delete: (questId: string, transitionId: string) =>
      deleteNoContent(transition(questId, transitionId)),
  },

  enums: {
    get: (enumName: string) =>
      getJson<string[]>(`/enums/${enumName}`) as Promise<string[]>,
  },
};
