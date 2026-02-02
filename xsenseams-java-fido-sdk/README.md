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

- **Base URL**: Your XSenseAMS server base URL (e.g. `https://ams.example.com` or `https://ams.example.com/tenant/tenant-id` if using tenant-in-path).
- **API Key**: Value for the `X-AMS-API-Key` header (from your server config; default handler may expect `testapikey` in development).
- **Tenant** (optional): If your deployment uses a tenant header, set it via `tenantHeader(name, value)`.

## Usage

### 1. Create client

```java
import com.xsenseams.fido.FidoClient;
import com.xsenseams.fido.FidoClientConfig;

FidoClientConfig config = FidoClientConfig.builder()
    .baseUrl("https://ams.example.com")
    .apiKey("your-api-key")
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

MakeCredentialRequest req = new MakeCredentialRequest("username");
MakeCredentialResponse resp = client.makeCredentialRequest(req);

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
    "username",
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

GetAssertionRequest req = new GetAssertionRequest("username");
GetAssertionResponse resp = client.getAssertionRequest(req);

String sessionId = resp.getSessionId();
JsonNode credentialAssertion = resp.getCredentialAssertion();  // PublicKeyCredentialRequestOptions

// Your app: use WebAuthn to get credential_assertion_response from the authenticator
```

**Step 2 – Finish assertion:** send the authenticator’s response to the server.

```java
import com.xsenseams.fido.dto.GetAssertionFinishRequest;

// credentialAssertionResponse = result from authenticator (JsonNode)
// factor_index: 1 = FirstFactor, 2 = SecondFactor (FIDO as second factor typically uses 2)
GetAssertionFinishRequest finishReq = new GetAssertionFinishRequest(
    "username",
    sessionId,
    2,  // SecondFactor
    credentialAssertionResponse
);
BaseResponse finishResp = client.getAssertionResponse(finishReq);
// finishResp.getStatus() == true on success
```

### 4. Error handling

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
| `getAssertionResponse` | POST `/api/fidogetassertionresponse` | Finish FIDO login |

All requests require header: `X-AMS-API-Key: <your-api-key>`.
