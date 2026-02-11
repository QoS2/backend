/** API base URL */
const RAW_API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080/api/v1';
export const API_BASE = RAW_API_BASE.replace(/\/$/, '');

const API_ORIGIN = API_BASE.startsWith('http') ? new URL(API_BASE).origin : '';

export const ADMIN_BASE = `${API_BASE}/admin`;

/** File upload */
export const UPLOAD_URL = `${API_BASE}/upload`;

/** Auth: current user endpoint */
export const AUTH_ME_URL = `${API_BASE}/auth/me`;

/** Auth: JWT login */
export const AUTH_LOGIN_URL = `${API_BASE}/auth/login`;

/** Auth: register */
export const AUTH_REGISTER_URL = `${API_BASE}/auth/register`;

/** Auth: OAuth to JWT token */
export const AUTH_TOKEN_URL = `${API_BASE}/auth/token`;

/** OAuth: Google login redirect */
export const OAUTH_GOOGLE_URL = API_ORIGIN ? `${API_ORIGIN}/oauth2/authorization/google` : '/oauth2/authorization/google';

/** Logout: Spring Security default logout */
export const LOGOUT_URL = API_ORIGIN ? `${API_ORIGIN}/logout` : '/logout';

/** Spacing grid (8px base) */
export const SPACING = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
} as const;

/** Border radius */
export const RADIUS = {
  sm: 8,
  md: 12,
  lg: 16,
} as const;

/** Default page size for pagination */
export const DEFAULT_PAGE_SIZE = 20;

/** Animation duration for drawer/transitions */
export const DRAWER_ANIMATION_MS = 300;

/** Enum names for admin form dropdowns */
export const ENUM_NAMES = [
  'questTheme',
  'questTone',
  'difficulty',
  'nodeType',
  'contentType',
  'actionType',
  'effectType',
  'language',
  'displayMode',
  'transitionMessageType',
] as const;

export type EnumName = (typeof ENUM_NAMES)[number];
