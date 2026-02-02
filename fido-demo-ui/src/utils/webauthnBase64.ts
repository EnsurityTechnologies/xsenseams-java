/**
 * Base64url helpers for WebAuthn payloads (credential creation and assertion).
 */

export function base64urlToBase64(input: string): string {
  input = input.replace(/-/g, '+').replace(/_/g, '/');
  const pad = input.length % 4;
  if (pad) {
    if (pad === 1) {
      throw new Error('InvalidLengthError: Input base64url string is the wrong length to determine padding');
    }
    input += new Array(5 - pad).join('=');
  }
  return input;
}

export function bufferToBase64url(buffer: ArrayBuffer): string {
  const str = btoa(String.fromCharCode.apply(null, Array.from(new Uint8Array(buffer))));
  return str.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
}

export function base64urlToUint8Array(input: string): Uint8Array {
  const base64 = base64urlToBase64(input);
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes;
}
