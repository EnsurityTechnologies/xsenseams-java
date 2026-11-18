package com.xsenseams.fido.sample.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.xsenseams.fido.FidoApiException;
import com.xsenseams.fido.FidoClient;
import com.xsenseams.fido.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Proxies the demo UI to the six AMS FIDO routes.
 * The browser Origin is forwarded so AMS can select a FIDO server configuration
 * and so finish verification sees the same page origin the security key signed.
 */
@RestController
@RequestMapping("/api/demo")
public class FidoDemoController {

    private final FidoClient fidoClient;
    private final String defaultFidoServerConfigId;

    public FidoDemoController(
            FidoClient fidoClient,
            @Value("${ams.fido-server-config-id:}") String defaultFidoServerConfigId) {
        this.fidoClient = fidoClient;
        this.defaultFidoServerConfigId = defaultFidoServerConfigId;
    }

    @PostMapping("/register/start")
    public ResponseEntity<?> registerStart(
            @RequestHeader(value = "Origin", required = false) String origin,
            @RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank()) {
            return badRequest("username is required");
        }
        try {
            MakeCredentialRequest req = new MakeCredentialRequest(username.trim(), resolveConfigId(body));
            MakeCredentialResponse resp = fidoClient.makeCredentialRequest(req, origin);
            return ResponseEntity.ok(Map.of(
                    "session_id", resp.getSessionId() != null ? resp.getSessionId() : "",
                    "credential_creation", resp.getCredentialCreation() != null ? resp.getCredentialCreation() : Map.of()
            ));
        } catch (FidoApiException e) {
            return apiError(e);
        }
    }

    @PostMapping("/register/finish")
    public ResponseEntity<?> registerFinish(
            @RequestHeader(value = "Origin", required = false) String origin,
            @RequestBody RegisterFinishRequest body) {
        if (body.getSessionId() == null || body.getSessionId().isBlank()) {
            return badRequest("session_id is required");
        }
        if (body.getCredentialCreationResponse() == null) {
            return badRequest("credential_creation_response is required");
        }
        try {
            MakeCredentialFinishRequest req = new MakeCredentialFinishRequest(
                    body.getSessionId(),
                    body.getCredentialCreationResponse()
            );
            BaseResponse resp = fidoClient.makeCredentialResponse(req, origin);
            return ResponseEntity.ok(Map.of(
                    "status", resp.getStatus() != null ? resp.getStatus() : false,
                    "message", resp.getMessage() != null ? resp.getMessage() : ""
            ));
        } catch (FidoApiException e) {
            return apiError(e);
        }
    }

    @PostMapping("/login/start")
    public ResponseEntity<?> loginStart(
            @RequestHeader(value = "Origin", required = false) String origin,
            @RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank()) {
            return badRequest("username is required");
        }
        try {
            GetAssertionRequest req = new GetAssertionRequest(username.trim(), resolveConfigId(body));
            GetAssertionResponse resp = fidoClient.getAssertionRequest(req, origin);
            return ResponseEntity.ok(Map.of(
                    "session_id", resp.getSessionId() != null ? resp.getSessionId() : "",
                    "credential_assertion", resp.getCredentialAssertion() != null ? resp.getCredentialAssertion() : Map.of()
            ));
        } catch (FidoApiException e) {
            return apiError(e);
        }
    }

    @PostMapping("/login/init")
    public ResponseEntity<?> loginInit(
            @RequestHeader(value = "Origin", required = false) String origin,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            GetAssertionResponse resp = fidoClient.getAssertionInit(resolveConfigId(body), origin);
            return ResponseEntity.ok(Map.of(
                    "session_id", resp.getSessionId() != null ? resp.getSessionId() : "",
                    "credential_assertion", resp.getCredentialAssertion() != null ? resp.getCredentialAssertion() : Map.of()
            ));
        } catch (FidoApiException e) {
            return apiError(e);
        }
    }

    @PostMapping("/login/initfinish")
    public ResponseEntity<?> loginInitFinish(
            @RequestHeader(value = "Origin", required = false) String origin,
            @RequestBody LoginFinishRequest body) {
        if (body.getSessionId() == null || body.getSessionId().isBlank()) {
            return badRequest("session_id is required");
        }
        if (body.getCredentialAssertionResponse() == null) {
            return badRequest("credential_assertion_response is required");
        }
        try {
            GetAssertionFinishRequest req = new GetAssertionFinishRequest(
                    body.getSessionId(),
                    body.getCredentialAssertionResponse()
            );
            GetAssertionInitFinishResponse resp = fidoClient.getAssertionInitFinish(req, origin);
            return ResponseEntity.ok(Map.of(
                    "status", resp.getStatus() != null ? resp.getStatus() : false,
                    "message", resp.getMessage() != null ? resp.getMessage() : "",
                    "username", resp.getUsername() != null ? resp.getUsername() : ""
            ));
        } catch (FidoApiException e) {
            return apiError(e);
        }
    }

    @PostMapping("/login/finish")
    public ResponseEntity<?> loginFinish(
            @RequestHeader(value = "Origin", required = false) String origin,
            @RequestBody LoginFinishRequest body) {
        if (body.getSessionId() == null || body.getSessionId().isBlank()) {
            return badRequest("session_id is required");
        }
        if (body.getCredentialAssertionResponse() == null) {
            return badRequest("credential_assertion_response is required");
        }
        try {
            GetAssertionFinishRequest req = new GetAssertionFinishRequest(
                    body.getSessionId(),
                    body.getCredentialAssertionResponse()
            );
            BaseResponse resp = fidoClient.getAssertionResponse(req, origin);
            return ResponseEntity.ok(Map.of(
                    "status", resp.getStatus() != null ? resp.getStatus() : false,
                    "message", resp.getMessage() != null ? resp.getMessage() : ""
            ));
        } catch (FidoApiException e) {
            return apiError(e);
        }
    }

    /**
     * Request id wins. Otherwise the configured id is used. A blank value is omitted
     * so AMS can select by Origin, then the primary configuration.
     */
    private String resolveConfigId(Map<String, String> body) {
        String fromBody = body == null ? null : body.get("fido_server_config_id");
        if (fromBody != null && !fromBody.isBlank()) {
            return fromBody.trim();
        }
        if (defaultFidoServerConfigId != null && !defaultFidoServerConfigId.isBlank()) {
            return defaultFidoServerConfigId.trim();
        }
        return null;
    }

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    private ResponseEntity<Map<String, String>> apiError(FidoApiException e) {
        String message = e.getServerMessage() != null ? e.getServerMessage() : e.getMessage();
        if (message == null || message.isBlank()) {
            message = "Request failed";
        }
        int code = e.getHttpStatusCode();
        HttpStatus status;
        if (code >= 400 && code <= 599) {
            try {
                status = HttpStatus.valueOf(code);
            } catch (IllegalArgumentException ex) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        } else if (code == 200) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    public static class RegisterFinishRequest {
        @JsonProperty("username")
        private String username;
        @JsonProperty("session_id")
        private String sessionId;
        @JsonProperty("credential_creation_response")
        private JsonNode credentialCreationResponse;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public JsonNode getCredentialCreationResponse() { return credentialCreationResponse; }
        public void setCredentialCreationResponse(JsonNode credentialCreationResponse) { this.credentialCreationResponse = credentialCreationResponse; }
    }

    public static class LoginFinishRequest {
        @JsonProperty("session_id")
        private String sessionId;
        @JsonProperty("credential_assertion_response")
        private JsonNode credentialAssertionResponse;

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public JsonNode getCredentialAssertionResponse() { return credentialAssertionResponse; }
        public void setCredentialAssertionResponse(JsonNode credentialAssertionResponse) { this.credentialAssertionResponse = credentialAssertionResponse; }
    }
}
