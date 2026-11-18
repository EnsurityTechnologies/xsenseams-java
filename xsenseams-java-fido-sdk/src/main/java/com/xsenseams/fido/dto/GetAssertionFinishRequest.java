package com.xsenseams.fido.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Request to finish a FIDO assertion.
 * POST /api/fidogetassertionresponse and POST /api/fidogetassertioninitfinish.
 * credential_assertion_response is the WebAuthn assertion from the authenticator.
 * AMS does not read factor_index. Username on the finish body is ignored.
 * userHandle may be null for username login. Usernameless login requires it.
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
