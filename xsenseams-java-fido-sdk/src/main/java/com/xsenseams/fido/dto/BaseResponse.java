package com.xsenseams.fido.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base response fields from XSenseAMS (ensweb.BaseResponse).
 */
public class BaseResponse {

    @JsonProperty("status")
    private Boolean status;

    @JsonProperty("message")
    private String message;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
