/**
 * API client for the sample Java backend (xsenseams-fido-sample).
 * Base URL is read from VITE_API_BASE (e.g. http://localhost:8080).
 */

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080';

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const url = `${API_BASE.replace(/\/$/, '')}${path}`;
  const res = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    const message = (data as { message?: string })?.message ?? `HTTP ${res.status}`;
    throw new Error(message);
  }
  return data as T;
}

export interface RegisterStartResponse {
  session_id: string;
  credential_creation: {
    publicKey?: PublicKeyCredentialCreationOptionsJSON;
    [key: string]: unknown;
  };
}

export interface RegisterFinishResponse {
  status: boolean;
  message: string;
}

export interface LoginStartResponse {
  session_id: string;
  credential_assertion: {
    publicKey?: PublicKeyCredentialRequestOptionsJSON;
    mediation?: string;
    [key: string]: unknown;
  };
}

export interface LoginInitFinishResponse {
  status: boolean;
  message: string;
  username: string;
}

export interface LoginFinishResponse {
  status: boolean;
  message: string;
}

// Minimal types for WebAuthn options (browser uses standard types; these are for JSON shape)
interface PublicKeyCredentialCreationOptionsJSON {
  challenge: string;
  rp: { name: string; id?: string };
  user: { id: string; name: string; displayName: string };
  pubKeyCredParams?: Array<{ type: string; alg: number }>;
  [key: string]: unknown;
}

interface PublicKeyCredentialRequestOptionsJSON {
  rp: { name: string; id?: string };
  challenge: string;
  allowCredentials?: Array<{ type: string; id: string }>;
  [key: string]: unknown;
}

export const demoApi = {
  registerStart: (username: string) =>
    request<RegisterStartResponse>('/api/demo/register/start', {
      method: 'POST',
      body: JSON.stringify({ username }),
    }),

  registerFinish: (body: {
    username: string;
    session_id: string;
    credential_creation_response: Record<string, unknown>;
  }) =>
    request<RegisterFinishResponse>('/api/demo/register/finish', {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  loginStart: (username: string) =>
    request<LoginStartResponse>('/api/demo/login/start', {
      method: 'POST',
      body: JSON.stringify({ username }),
    }),

  loginInit: () =>
    request<LoginStartResponse>('/api/demo/login/init', {
      method: 'POST',
    }),
  
  loginInitFinish: (body: {
    session_id: string;
    credential_assertion_response: Record<string, unknown>;
  }) =>
    request<LoginInitFinishResponse>('/api/demo/login/initfinish', {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  loginFinish: (body: {
    session_id: string;
    credential_assertion_response: Record<string, unknown>;
  }) =>
    request<LoginFinishResponse>('/api/demo/login/finish', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
};
