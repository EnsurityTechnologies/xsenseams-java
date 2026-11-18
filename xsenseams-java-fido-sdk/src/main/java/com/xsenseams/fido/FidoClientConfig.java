package com.xsenseams.fido;

import okhttp3.OkHttpClient;

import java.util.Objects;

/**
 * Configuration for FidoClient.
 * Base URL is the AMS tenant host (or a URL prefix in front of /api). The API key is sent
 * on every call. origin is the page origin AMS should see when a call does not pass one.
 * fidoServerConfigId is sent on a start call that does not include its own id.
 */
public class FidoClientConfig {

    public static final String DEFAULT_API_KEY_HEADER = "X-AMS-API-Key";
    public static final int DEFAULT_CONNECT_TIMEOUT_SEC = 30;
    public static final int DEFAULT_READ_TIMEOUT_SEC = 60;

    private final String baseUrl;
    private final String apiKey;
    private final String tenantHeaderName;
    private final String tenantHeaderValue;
    private final String origin;
    private final String fidoServerConfigId;
    private final int connectTimeoutSeconds;
    private final int readTimeoutSeconds;
    private final OkHttpClient customHttpClient;

    private FidoClientConfig(Builder builder) {
        this.baseUrl = normalizeBaseUrl(Objects.requireNonNull(builder.baseUrl, "baseUrl"));
        this.apiKey = Objects.requireNonNull(builder.apiKey, "apiKey");
        this.tenantHeaderName = builder.tenantHeaderName;
        this.tenantHeaderValue = builder.tenantHeaderValue;
        this.origin = normalizeOrigin(builder.origin);
        this.fidoServerConfigId = normalizeConfigId(builder.fidoServerConfigId);
        this.connectTimeoutSeconds = builder.connectTimeoutSeconds > 0 ? builder.connectTimeoutSeconds : DEFAULT_CONNECT_TIMEOUT_SEC;
        this.readTimeoutSeconds = builder.readTimeoutSeconds > 0 ? builder.readTimeoutSeconds : DEFAULT_READ_TIMEOUT_SEC;
        this.customHttpClient = builder.customHttpClient;
    }

    private static String normalizeBaseUrl(String url) {
        if (url == null) return null;
        String s = url.trim();
        if (s.endsWith("/")) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getTenantHeaderName() {
        return tenantHeaderName;
    }

    public String getTenantHeaderValue() {
        return tenantHeaderValue;
    }

    /**
     * Page origin sent as the HTTP Origin header when a call does not pass its own.
     * Scheme, host, and port, with no path and no trailing slash.
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * FIDO server configuration id used when a start call does not supply one.
     * Usernameless login has no request body, so this is how a Java app selects that configuration.
     */
    public String getFidoServerConfigId() {
        return fidoServerConfigId;
    }

    static String normalizeConfigId(String fidoServerConfigId) {
        if (fidoServerConfigId == null) {
            return null;
        }
        String value = fidoServerConfigId.trim();
        return value.isEmpty() ? null : value;
    }

    static String normalizeOrigin(String origin) {
        if (origin == null) {
            return null;
        }
        String value = origin.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value.isEmpty() ? null : value;
    }

    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public int getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    public OkHttpClient getCustomHttpClient() {
        return customHttpClient;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String baseUrl;
        private String apiKey;
        private String tenantHeaderName;
        private String tenantHeaderValue;
        private String origin;
        private String fidoServerConfigId;
        private int connectTimeoutSeconds = DEFAULT_CONNECT_TIMEOUT_SEC;
        private int readTimeoutSeconds = DEFAULT_READ_TIMEOUT_SEC;
        private OkHttpClient customHttpClient;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder tenantHeader(String name, String value) {
            this.tenantHeaderName = name;
            this.tenantHeaderValue = value;
            return this;
        }

        /**
         * Default page origin for the Origin header. A non-blank origin passed on a call wins.
         */
        public Builder origin(String origin) {
            this.origin = origin;
            return this;
        }

        /**
         * Configuration id from Settings → FIDO Configuration. Applied to a start call
         * that does not pass its own id, including usernameless login.
         */
        public Builder fidoServerConfigId(String fidoServerConfigId) {
            this.fidoServerConfigId = fidoServerConfigId;
            return this;
        }

        public Builder connectTimeoutSeconds(int seconds) {
            this.connectTimeoutSeconds = seconds;
            return this;
        }

        public Builder readTimeoutSeconds(int seconds) {
            this.readTimeoutSeconds = seconds;
            return this;
        }

        public Builder customHttpClient(OkHttpClient client) {
            this.customHttpClient = client;
            return this;
        }

        public FidoClientConfig build() {
            return new FidoClientConfig(this);
        }
    }
}
