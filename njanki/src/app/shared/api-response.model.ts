/**
 * Format uniforme de réponse API pour la plateforme Njangi (GEMINI.md)
 */
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errorCode?: string;
}

export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance?: string;
  invalidParams?: Array<{
    field: string;
    reason: string;
  }>;
}
