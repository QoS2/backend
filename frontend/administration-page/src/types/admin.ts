/** Admin API 공통 타입 */

export interface ErrorResponse {
  error: string;
  errorCode: string;
  message: string;
  path?: string;
  errors?: Record<string, string>;
}
