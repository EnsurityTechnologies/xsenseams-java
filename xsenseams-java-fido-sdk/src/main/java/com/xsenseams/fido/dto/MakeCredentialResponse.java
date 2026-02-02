package com.xsenseams.fido.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Response from start FIDO credential registration.
 * Contains session_id and credential_creation (W3C PublicKeyCredentialCreationOptions).
 */
public class MakeCredentialResponse extends BaseResponse {

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("credential_creation")
    private JsonNode credentialCreation;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public JsonNode getCredentialCreation() {
        return credentialCreation;
    }

    public void setCredentialCreation(JsonNode credentialCreation) {
        this.credentialCreation = credentialCreation;
    }
}
