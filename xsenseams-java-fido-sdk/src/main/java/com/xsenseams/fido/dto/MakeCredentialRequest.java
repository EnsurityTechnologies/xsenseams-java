package com.xsenseams.fido.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to start FIDO credential registration.
 * POST /api/fidomakecredentialrequest
 * fido_server_config_id is optional. When it is omitted, AMS selects a configuration
 * from the Origin header, then the tenant primary configuration.
 */
public class MakeCredentialRequest {

    @JsonProperty("username")
    private String username;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("fido_server_config_id")
    private String fidoServerConfigId;

    public MakeCredentialRequest() {
    }

    public MakeCredentialRequest(String username) {
        this.username = username;
    }

    public MakeCredentialRequest(String username, String fidoServerConfigId) {
        this.username = username;
        setFidoServerConfigId(fidoServerConfigId);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFidoServerConfigId() {
        return fidoServerConfigId;
    }

    public void setFidoServerConfigId(String fidoServerConfigId) {
        this.fidoServerConfigId = blankToNull(fidoServerConfigId);
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
