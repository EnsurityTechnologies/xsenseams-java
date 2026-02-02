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

1. **Register FIDO:** Enter a username that exists in XSenseAMS, click "Register FIDO". The app calls the Java backend `/api/demo/register/start`, gets credential creation options, calls `navigator.credentials.create()`, then sends the result to `/api/demo/register/finish`.
2. **Login with FIDO:** Enter the same username, click "Login with FIDO". The app calls `/api/demo/login/start`, gets assertion options, calls `navigator.credentials.get()`, then sends the result to `/api/demo/login/finish`.

End-to-end: **Browser (WebAuthn) → Java sample backend (xsenseams-java-fido-sdk) → XSenseAMS (four FIDO APIs).**
