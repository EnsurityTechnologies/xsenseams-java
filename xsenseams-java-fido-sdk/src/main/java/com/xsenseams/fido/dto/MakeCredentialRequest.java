package com.xsenseams.fido.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to start FIDO credential registration.
 * POST /api/fidomakecredentialrequest
 */
public class MakeCredentialRequest {

    @JsonProperty("username")
    private String username;

    public MakeCredentialRequest() {
    }

    public MakeCredentialRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
