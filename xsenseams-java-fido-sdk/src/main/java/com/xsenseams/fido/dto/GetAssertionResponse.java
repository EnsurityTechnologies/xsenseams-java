package com.xsenseams.fido.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Response from start FIDO assertion (login).
 * Contains session_id and credential_assertion (W3C PublicKeyCredentialRequestOptions).
 */
public class GetAssertionResponse extends BaseResponse {

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("credential_assertion")
    private JsonNode credentialAssertion;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public JsonNode getCredentialAssertion() {
        return credentialAssertion;
    }

    public void setCredentialAssertion(JsonNode credentialAssertion) {
        this.credentialAssertion = credentialAssertion;
    }
}
