package com.xsenseams.fido.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Request to finish FIDO credential registration.
 * POST /api/fidomakecredentialresponse
 * credential_creation_response is the W3C PublicKeyCredential creation response from the authenticator.
 */
public class MakeCredentialFinishRequest {

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("credential_creation_response")
    private JsonNode credentialCreationResponse;

    public MakeCredentialFinishRequest() {
    }

    public MakeCredentialFinishRequest(String sessionId, JsonNode credentialCreationResponse) {
        this.sessionId = sessionId;
        this.credentialCreationResponse = credentialCreationResponse;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public JsonNode getCredentialCreationResponse() {
        return credentialCreationResponse;
    }

    public void setCredentialCreationResponse(JsonNode credentialCreationResponse) {
        this.credentialCreationResponse = credentialCreationResponse;
    }
}
