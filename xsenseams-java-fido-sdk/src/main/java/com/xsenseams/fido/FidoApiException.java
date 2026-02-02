package com.xsenseams.fido;

/**
 * Thrown when the XSenseAMS FIDO API returns an HTTP error or response with status == false.
 */
public class FidoApiException extends RuntimeException {

    private final int httpStatusCode;
    private final String serverMessage;

    public FidoApiException(String message, int httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.serverMessage = message;
    }

    public FidoApiException(String message, int httpStatusCode, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
        this.serverMessage = message;
    }

    public FidoApiException(String message) {
        this(message, -1);
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getServerMessage() {
        return serverMessage;
    }
}
