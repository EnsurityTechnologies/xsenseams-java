package com.xsenseams.fido.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to start FIDO assertion (login).
 * POST /api/fidogetassertion
 */
public class GetAssertionRequest {

    @JsonProperty("username")
    private String username;

    public GetAssertionRequest() {
    }

    public GetAssertionRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
