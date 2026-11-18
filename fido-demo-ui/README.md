# XSenseAMS FIDO Demo UI

Minimal WebAuthn frontend that demonstrates FIDO registration and login by calling the **java-fido-sample** Java backend, which in turn uses **xsenseams-java-fido-sdk** to talk to XSenseAMS.

## Prerequisites

- Node.js 18+
- XSenseAMS server running with FIDO configured and API key enabled (e.g. `testapikey`)
- Sample Java backend (**java-fido-sample**) running (e.g. on port 8080)
- A user created in XSenseAMS (for the username you will use in the demo)

## Configuration

Set the base URL of the sample Java backend:

- **Default:** `http://localhost:8080`
- **Override:** Create a `.env` file in this directory with:
  ```
  VITE_API_BASE=http://localhost:8080
  ```
  Or set the variable when running: `VITE_API_BASE=http://localhost:8080 npm run dev`

## Run

```bash
npm install
npm run dev
```

Open http://localhost:5173 in a browser. WebAuthn requires a secure context (HTTPS or localhost).

## Flow

The optional **Config id** is sent on every start call. Leave it blank to let AMS match this page's `Origin`, then the primary FIDO server configuration. Copy the id from **Settings → FIDO Configuration** when the tenant has more than one. Add this page origin (for example `http://localhost:5173`) to that configuration's **Origins** list. The host must match the configuration domain.

1. **Register FIDO:** Enter a username or email that already exists in AMS. The app calls `/api/demo/register/start`, passes `credential_creation.publicKey` to `navigator.credentials.create()` after decoding `challenge` and `user.id`, then posts the authenticator result to `/api/demo/register/finish`. Registration creates a resident key.
2. **Login with username:** Enter the same username or email. The app calls `/api/demo/login/start`, then `navigator.credentials.get()` with that user's `allowCredentials`, then `/api/demo/login/finish`.
3. **Login with security key:** Leave the username blank. The app calls `/api/demo/login/init` (no allow-list), then `navigator.credentials.get()`, then `/api/demo/login/initfinish`. AMS returns the username from the credential user handle.

The browser talks to the Java sample. The sample calls the six AMS FIDO routes and forwards this page's `Origin` header. A successful login does not issue an AMS access token. The sample only reports the AMS result.
