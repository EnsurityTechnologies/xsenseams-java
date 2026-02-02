package com.xsenseams.fido.sample.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.xsenseams.fido.FidoApiException;
import com.xsenseams.fido.FidoClient;
import com.xsenseams.fido.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class FidoDemoController {

    private final FidoClient fidoClient;

    public FidoDemoController(FidoClient fidoClient) {
        this.fidoClient = fidoClient;
    }

    @PostMapping("/register/start")
    public ResponseEntity<?> registerStart(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "username is required"));
        }
        try {
            MakeCredentialRequest req = new MakeCredentialRequest(username);
            MakeCredentialResponse resp = fidoClient.makeCredentialRequest(req);
            return ResponseEntity.ok(Map.of(
                    "session_id", resp.getSessionId() != null ? resp.getSessionId() : "",
                    "credential_creation", resp.getCredentialCreation() != null ? resp.getCredentialCreation() : Map.of()
            ));
        } catch (FidoApiException e) {
            int code = e.getHttpStatusCode();
            HttpStatus status = code >= 400 && code < 500 ? HttpStatus.valueOf(code) : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(Map.of("message", e.getServerMessage() != null ? e.getServerMessage() : e.getMessage()));
        }
    }

    @PostMapping("/register/finish")
    public ResponseEntity<?> registerFinish(@RequestBody RegisterFinishRequest body) {
        if (body.getUsername() == null || body.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "username is required"));
        }
        if (body.getSessionId() == null || body.getSessionId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "session_id is required"));
        }
        if (body.getCredentialCreationResponse() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "credential_creation_response is required"));
        }
        try {
            MakeCredentialFinishRequest req = new MakeCredentialFinishRequest(
                    body.getUsername(),
                    body.getSessionId(),
                    body.getCredentialCreationResponse()
            );
            BaseResponse resp = fidoClient.makeCredentialResponse(req);
            return ResponseEntity.ok(Map.of(
                    "status", resp.getStatus() != null ? resp.getStatus() : false,
                    "message", resp.getMessage() != null ? resp.getMessage() : ""
            ));
        } catch (FidoApiException e) {
            int code = e.getHttpStatusCode();
            HttpStatus status = code >= 400 && code < 500 ? HttpStatus.valueOf(code) : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(Map.of("message", e.getServerMessage() != null ? e.getServerMessage() : e.getMessage()));
        }
    }

    @PostMapping("/login/start")
    public ResponseEntity<?> loginStart(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "username is required"));
        }
        try {
            GetAssertionRequest req = new GetAssertionRequest(username);
            GetAssertionResponse resp = fidoClient.getAssertionRequest(req);
            return ResponseEntity.ok(Map.of(
                    "session_id", resp.getSessionId() != null ? resp.getSessionId() : "",
                    "credential_assertion", resp.getCredentialAssertion() != null ? resp.getCredentialAssertion() : Map.of()
            ));
        } catch (FidoApiException e) {
            int code = e.getHttpStatusCode();
            HttpStatus status = code >= 400 && code < 500 ? HttpStatus.valueOf(code) : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(Map.of("message", e.getServerMessage() != null ? e.getServerMessage() : e.getMessage()));
        }
    }

    @PostMapping("/login/finish")
    public ResponseEntity<?> loginFinish(@RequestBody LoginFinishRequest body) {
        if (body.getUsername() == null || body.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "username is required"));
        }
        if (body.getSessionId() == null || body.getSessionId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "session_id is required"));
        }
        if (body.getCredentialAssertionResponse() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "credential_assertion_response is required"));
        }
        try {
            int factorIndex = body.getFactorIndex() != null ? body.getFactorIndex() : 1;
            GetAssertionFinishRequest req = new GetAssertionFinishRequest(
                    body.getUsername(),
                    body.getSessionId(),
                    factorIndex,
                    body.getCredentialAssertionResponse()
            );
            BaseResponse resp = fidoClient.getAssertionResponse(req);
            return ResponseEntity.ok(Map.of(
                    "status", resp.getStatus() != null ? resp.getStatus() : false,
                    "message", resp.getMessage() != null ? resp.getMessage() : ""
            ));
        } catch (FidoApiException e) {
            int code = e.getHttpStatusCode();
            HttpStatus status = code >= 400 && code < 500 ? HttpStatus.valueOf(code) : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(Map.of("message", e.getServerMessage() != null ? e.getServerMessage() : e.getMessage()));
        }
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
        @JsonProperty("username")
        private String username;
        @JsonProperty("session_id")
        private String sessionId;
        @JsonProperty("factor_index")
        private Integer factorIndex;
        @JsonProperty("credential_assertion_response")
        private JsonNode credentialAssertionResponse;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public Integer getFactorIndex() { return factorIndex; }
        public void setFactorIndex(Integer factorIndex) { this.factorIndex = factorIndex; }
        public JsonNode getCredentialAssertionResponse() { return credentialAssertionResponse; }
        public void setCredentialAssertionResponse(JsonNode credentialAssertionResponse) { this.credentialAssertionResponse = credentialAssertionResponse; }
    }
}
