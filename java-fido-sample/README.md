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
| `ams.base-url` | AMS tenant host. Include a URL prefix when the deployment is mounted under one. | `https://contoso.example.com` |
| `ams.api-key` | API key (header `X-AMS-API-Key`) | |
| `ams.origin` | Page origin used when the incoming request has no `Origin` header | `http://localhost:5173` |
| `ams.fido-server-config-id` | Configuration id from Settings → FIDO Configuration. The Java app sends it on every start call that omits `fido_server_config_id`, including usernameless login. | |
| `ams.tenant-header-name` | Optional tenant header name. AMS normally selects the tenant from the host. | |
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

| Method | Path | Body | AMS call |
|--------|------|------|----------|
| POST | `/api/demo/register/start` | `{ "username", "fido_server_config_id"? }` | `POST /api/fidomakecredentialrequest` |
| POST | `/api/demo/register/finish` | `{ "session_id", "credential_creation_response" }` | `POST /api/fidomakecredentialresponse` |
| POST | `/api/demo/login/start` | `{ "username", "fido_server_config_id"? }` | `POST /api/fidogetassertion` |
| POST | `/api/demo/login/init` | `{ "fido_server_config_id"? }` | `GET /api/fidogetassertioninit` |
| POST | `/api/demo/login/initfinish` | `{ "session_id", "credential_assertion_response" }` | `POST /api/fidogetassertioninitfinish` |
| POST | `/api/demo/login/finish` | `{ "session_id", "credential_assertion_response" }` | `POST /api/fidogetassertionresponse` |

`username` on a finish body is ignored. A request `fido_server_config_id` overrides `ams.fido-server-config-id`. When neither is set, AMS selects the configuration from the forwarded `Origin` header, then the primary configuration.

The sample copies the browser `Origin` header onto the AMS call. That value is the UI page origin. Add that origin to **Origins** on the FIDO server configuration. A domain host match can select the configuration, and the finish call still fails when the full origin is missing from **Origins**. With more than one configuration, send `fido_server_config_id` on the start call.

CORS is enabled for `http://localhost:5173` and `http://localhost:3000` so the demo frontend can call this backend.

## Point the frontend to this backend

Set the demo UI base URL to this app (e.g. `http://localhost:8080`). See **xsenseams-fido-demo-ui** README.

## Troubleshooting

- **Finish fails after the browser accepts the security key** – Add the UI origin, exactly, to **Origins** on the FIDO configuration used for the ceremony. `http://localhost:5173` and `http://127.0.0.1:5173` are different origins. The page host must also match the configuration domain (the relying party id).
- **"Failed to connect to localhost/0:0:0:0:0:0:0:1:33300"** – The sample backend could not reach XSenseAMS. Fixes:
  1. Use **127.0.0.1** in `ams.base-url` (e.g. `http://127.0.0.1:33300`) so the client uses IPv4 instead of IPv6 localhost.
  2. Ensure **XSenseAMS is running** and listening on port 33300 (start the Go server before using the demo).
  3. If XSenseAMS runs on another host/port, set `ams.base-url` to that URL (e.g. `http://your-server:33300`).
