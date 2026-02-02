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

    @JsonProperty("username")
    private String username;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("factor_index")
    private Integer factorIndex;

    @JsonProperty("credential_assertion_response")
    private JsonNode credentialAssertionResponse;

    public GetAssertionFinishRequest() {
    }

    public GetAssertionFinishRequest(String username, String sessionId, int factorIndex, JsonNode credentialAssertionResponse) {
        this.username = username;
        this.sessionId = sessionId;
        this.factorIndex = factorIndex;
        this.credentialAssertionResponse = credentialAssertionResponse;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getFactorIndex() {
        return factorIndex;
    }

    public void setFactorIndex(Integer factorIndex) {
        this.factorIndex = factorIndex;
    }

    public JsonNode getCredentialAssertionResponse() {
        return credentialAssertionResponse;
    }

    public void setCredentialAssertionResponse(JsonNode credentialAssertionResponse) {
        this.credentialAssertionResponse = credentialAssertionResponse;
    }
}
