package com.xsenseams.fido.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Request to finish FIDO assertion (login).
 * POST /api/fidogetassertionresponse
 * credential_assertion_response is the W3C assertion response from the authenticator.
 * factor_index: 1 = FirstFactor, 2 = SecondFactor (FIDO as second factor typically uses 2).
 */
public class GetAssertionFinishRequest {

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("credential_assertion_response")
    private JsonNode credentialAssertionResponse;

    public GetAssertionFinishRequest() {
    }

    public GetAssertionFinishRequest(String sessionId, JsonNode credentialAssertionResponse) {
        this.sessionId = sessionId;
        this.credentialAssertionResponse = credentialAssertionResponse;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public JsonNode getCredentialAssertionResponse() {
        return credentialAssertionResponse;
    }

    public void setCredentialAssertionResponse(JsonNode credentialAssertionResponse) {
        this.credentialAssertionResponse = credentialAssertionResponse;
    }
}
