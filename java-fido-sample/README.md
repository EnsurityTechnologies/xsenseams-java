# XSenseAMS FIDO Sample (Java Backend)

Sample Spring Boot application that uses **xsenseams-java-fido-sdk** to proxy XSenseAMS FIDO APIs. The demo frontend calls this backend, which in turn calls XSenseAMS via the SDK.

## Prerequisites

- Java 11+
- Maven 3.6+
- XSenseAMS server running with FIDO configured and API key enabled (e.g. `testapikey` with default handler)

## Build the SDK first

Install the FIDO SDK into your local Maven repository:

```bash
cd ../xsenseams-java-fido-sdk
mvn clean install
cd ../java-fido-sample
```

## Configuration

Set in `src/main/resources/application.properties` or via environment variables:

| Property | Description | Example |
|----------|-------------|---------|
| `ams.base-url` | XSenseAMS server base URL | `http://127.0.0.1:33300` (use 127.0.0.1 to avoid IPv6 issues) |
| `ams.api-key` | API key (header `X-AMS-API-Key`) | `testapikey` |
| `ams.tenant-header-name` | Optional tenant header name | |
| `ams.tenant-header-value` | Optional tenant header value | |
| `server.port` | Port for this sample app | `8080` |

## Run

```bash
mvn spring-boot:run
```

Or build and run the JAR:

```bash
mvn clean package
java -jar target/xsenseams-fido-sample-1.0.0.jar
```

## API (proxy to XSenseAMS)

| Method | Path | Body | Returns |
|--------|------|------|---------|
| POST | `/api/demo/register/start` | `{ "username": "string" }` | `{ "session_id", "credential_creation" }` |
| POST | `/api/demo/register/finish` | `{ "username", "session_id", "credential_creation_response" }` | `{ "status", "message" }` |
| POST | `/api/demo/login/start` | `{ "username": "string" }` | `{ "session_id", "credential_assertion" }` |
| POST | `/api/demo/login/init` | `{}` | `{ "session_id", "credential_assertion" }` |
| POST | `/api/demo/login/initfinish` | `{ "session_id", "credential_assertion_response" }` | `{ "status", "message", "username" }` |
| POST | `/api/demo/login/finish` | `{ "session_id", "credential_assertion_response" }` | `{ "status", "message" }` |

CORS is enabled for `http://localhost:5173` and `http://localhost:3000` so the demo frontend can call this backend.

## Point the frontend to this backend

Set the demo UI base URL to this app (e.g. `http://localhost:8080`). See **xsenseams-fido-demo-ui** README.

## Troubleshooting

- **"Failed to connect to localhost/0:0:0:0:0:0:0:1:33300"** – The sample backend could not reach XSenseAMS. Fixes:
  1. Use **127.0.0.1** in `ams.base-url` (e.g. `http://127.0.0.1:33300`) so the client uses IPv4 instead of IPv6 localhost.
  2. Ensure **XSenseAMS is running** and listening on port 33300 (start the Go server before using the demo).
  3. If XSenseAMS runs on another host/port, set `ams.base-url` to that URL (e.g. `http://your-server:33300`).
