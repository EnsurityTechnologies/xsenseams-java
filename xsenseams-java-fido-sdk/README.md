# XSenseAMS FIDO SDK (Java)

Java SDK for calling XSenseAMS FIDO (WebAuthn) APIs: credential registration and assertion (login).

## Requirements

- Java 11+
- Maven 3.6+

## Installation

Add the SDK as a dependency (or build and install locally):

```xml
<dependency>
    <groupId>com.xsenseams</groupId>
    <artifactId>xsenseams-java-fido-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

Or from the project root:

```bash
cd xsenseams-java-fido-sdk
mvn clean install
```

## Configuration

- **Base URL**: The AMS tenant host, for example `https://contoso.example.com`. In a single-tenant deployment this is the configured AMS domain. If the deployment is mounted under a URL prefix, include that prefix. The API paths stay `/api/...`.
- **API Key**: Value for the `X-AMS-API-Key` header. A missing, unknown, or expired key returns HTTP 401. The header is sent on every call, including the usernameless GET.
- **Origin** (optional): Page origin that calls `navigator.credentials.create` or `navigator.credentials.get` — scheme, host, and port, with no path and no trailing slash. Set a default with `origin(...)`. A non-blank origin passed on a call replaces that default. AMS uses this header to choose a FIDO server configuration when the start call does not include a configuration id.
- **Tenant** (optional): AMS normally resolves the tenant from the host. If a deployment still expects a tenant header, set it with `tenantHeader(name, value)`.

## Usage

### 1. Create client

```java
import com.xsenseams.fido.FidoClient;
import com.xsenseams.fido.FidoClientConfig;

FidoClientConfig config = FidoClientConfig.builder()
    .baseUrl("https://contoso.example.com")
    .apiKey("your-api-key")
    .origin("https://login.contoso.com")
    .fidoServerConfigId("your-config-id")
    .build();
    // Optional: .tenantHeader("X-Tenant-ID", "tenant-id")
    // Optional: .connectTimeoutSeconds(30).readTimeoutSeconds(60)

FidoClient client = new FidoClient(config);
```

### 2. Registration flow (make credential)

**Step 1 – Start registration:** get options from the server and pass them to your authenticator (browser or device).

```java
import com.xsenseams.fido.dto.MakeCredentialRequest;
import com.xsenseams.fido.dto.MakeCredentialResponse;
import com.fasterxml.jackson.databind.JsonNode;

MakeCredentialRequest req = new MakeCredentialRequest("jane.doe");
// Optional. Omit it to let AMS match Origin, then the primary configuration.
req.setFidoServerConfigId("optional-config-id");
MakeCredentialResponse resp = client.makeCredentialRequest(req);
// Or pass the page origin for this call: client.makeCredentialRequest(req, "https://login.contoso.com");

String sessionId = resp.getSessionId();
JsonNode credentialCreation = resp.getCredentialCreation();  // PublicKeyCredentialCreationOptions

// Your app: use a WebAuthn library or browser to get credential_creation_response from the authenticator
// (e.g. navigator.credentials.create(credentialCreation) in browser, or a Java WebAuthn library)
```

**Step 2 – Finish registration:** send the authenticator’s response to the server.

```java
import com.xsenseams.fido.dto.MakeCredentialFinishRequest;
import com.xsenseams.fido.dto.BaseResponse;

// credentialCreationResponse = result from authenticator (JsonNode or object serializable to W3C format)
MakeCredentialFinishRequest finishReq = new MakeCredentialFinishRequest(
    sessionId,
    credentialCreationResponse  // JsonNode
);
BaseResponse finishResp = client.makeCredentialResponse(finishReq);
// finishResp.getStatus() == true on success
```

### 3. Login flow (get assertion)

**Step 1 – Start assertion:** get options from the server and pass them to your authenticator.

```java
import com.xsenseams.fido.dto.GetAssertionRequest;
import com.xsenseams.fido.dto.GetAssertionResponse;

GetAssertionRequest req = new GetAssertionRequest("jane.doe", "optional-config-id");
GetAssertionResponse resp = client.getAssertionRequest(req);

String sessionId = resp.getSessionId();
JsonNode credentialAssertion = resp.getCredentialAssertion();  // PublicKeyCredentialRequestOptions

// Your app: use WebAuthn to get credential_assertion_response from the authenticator
```

**Step 2 – Finish assertion:** send the authenticator’s response to the server.

```java
import com.xsenseams.fido.dto.GetAssertionFinishRequest;

// credentialAssertionResponse = result from authenticator (JsonNode)
GetAssertionFinishRequest finishReq = new GetAssertionFinishRequest(
    sessionId,
    credentialAssertionResponse
);
BaseResponse finishResp = client.getAssertionResponse(finishReq);
// finishResp.getStatus() == true on success
```

### 4. Username-less / discoverable login flow (assertion init)

**Step 1 – Init assertion:** fetch challenge and options without a username. `allowCredentials` is omitted.

```java
import com.xsenseams.fido.dto.GetAssertionResponse;

// No id argument: the client sends FidoClientConfig.fidoServerConfigId.
// Pass an id here to override that backend value for this call.
GetAssertionResponse initResp = client.getAssertionInit();

String sessionId = initResp.getSessionId();
JsonNode credentialAssertion = initResp.getCredentialAssertion();

// Your app: use WebAuthn to get credential_assertion_response from the authenticator
```

**Step 2 – Init finish assertion:** submit authenticator response and receive resolved username.

```java
import com.xsenseams.fido.dto.GetAssertionFinishRequest;
import com.xsenseams.fido.dto.GetAssertionInitFinishResponse;

GetAssertionFinishRequest initFinishReq = new GetAssertionFinishRequest(
    sessionId,
    credentialAssertionResponse
);
GetAssertionInitFinishResponse initFinishResp = client.getAssertionInitFinish(initFinishReq);

boolean status = Boolean.TRUE.equals(initFinishResp.getStatus());
String username = initFinishResp.getUsername();
```

### 5. Error handling

On HTTP errors or when the server returns `status: false`, the client throws `FidoApiException`:

```java
import com.xsenseams.fido.FidoApiException;

try {
    MakeCredentialResponse resp = client.makeCredentialRequest(new MakeCredentialRequest("user"));
    // ...
} catch (FidoApiException e) {
    int code = e.getHttpStatusCode();
    String msg = e.getServerMessage();
    // handle error
}
```

## WebAuthn payloads

The SDK uses **Jackson `JsonNode`** for WebAuthn fields (`credential_creation`, `credential_creation_response`, `credential_assertion`, `credential_assertion_response`) so your application (or a Java WebAuthn library such as [webauthn4j](https://webauthn4j.github.io/webauthn4j/)) can build and parse them. The SDK only transports JSON to and from the XSenseAMS server.

## API endpoints (reference)

| Method | Path | Purpose |
|--------|------|---------|
| `makeCredentialRequest` | POST `/api/fidomakecredentialrequest` | Start FIDO registration |
| `makeCredentialResponse` | POST `/api/fidomakecredentialresponse` | Finish FIDO registration |
| `getAssertionRequest` | POST `/api/fidogetassertion` | Start FIDO login |
| `getAssertionInit` | GET `/api/fidogetassertioninit` | Start username-less/discoverable FIDO login |
| `getAssertionInitFinish` | POST `/api/fidogetassertioninitfinish` | Finish username-less/discoverable FIDO login and resolve username |
| `getAssertionResponse` | POST `/api/fidogetassertionresponse` | Finish FIDO login |

All requests require header: `X-AMS-API-Key: <your-api-key>`.

Registration and username login send an optional `fido_server_config_id` in the JSON body. Usernameless login sends that same id as a query parameter. Finish calls do not send it; AMS reads the configuration it stored on the session. When a start call omits the id, the client sends `FidoClientConfig.fidoServerConfigId`. A blank value is omitted.

AMS chooses the configuration in this order: the supplied id, then a unique match of the `Origin` header, then the tenant primary configuration. An unknown id returns HTTP 404 and AMS does not fall through. Finish verification checks the origin the security key signed against that configuration's origins list.

A successful ceremony is HTTP 200 with `"status": true`. Some ceremony failures are also HTTP 200, with `"status": false` and a `message`. The client throws `FidoApiException` for those, and for HTTP 400, 401, 404, and 500. `getMessage()` is the AMS `message` when the body has one.

These calls do not issue an AMS access token or device token. After `"status": true`, the caller decides what its own session does. Usernameless finish also returns the AMS `username`.
