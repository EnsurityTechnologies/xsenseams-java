package com.xsenseams.fido;

import okhttp3.OkHttpClient;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for FidoClient.
 * Base URL (e.g. https://ams.example.com or tenant-in-path), API key, origin URL,
 * optional tenant header, timeouts.
 */
public class FidoClientConfig {

    public static final String DEFAULT_API_KEY_HEADER = "X-AMS-API-Key";
    public static final int DEFAULT_CONNECT_TIMEOUT_SEC = 30;
    public static final int DEFAULT_READ_TIMEOUT_SEC = 60;

    private final String baseUrl;
    private final String apiKey;
    private final String tenantHeaderName;
    private final String tenantHeaderValue;
    private final int connectTimeoutSeconds;
    private final int readTimeoutSeconds;
    private final String originUrl;
    private final OkHttpClient customHttpClient;

    private FidoClientConfig(Builder builder) {
        this.baseUrl = normalizeBaseUrl(Objects.requireNonNull(builder.baseUrl, "baseUrl"));
        this.apiKey = Objects.requireNonNull(builder.apiKey, "apiKey");
        this.originUrl = normalizeUrl(builder.originUrl);
        this.tenantHeaderName = builder.tenantHeaderName;
        this.tenantHeaderValue = builder.tenantHeaderValue;
        this.connectTimeoutSeconds = builder.connectTimeoutSeconds > 0 ? builder.connectTimeoutSeconds : DEFAULT_CONNECT_TIMEOUT_SEC;
        this.readTimeoutSeconds = builder.readTimeoutSeconds > 0 ? builder.readTimeoutSeconds : DEFAULT_READ_TIMEOUT_SEC;
        this.customHttpClient = builder.customHttpClient;
    }

    private static String normalizeBaseUrl(String url) {
        if (url == null) return null;
        return normalizeUrl(url);
    }

    private static String normalizeUrl(String url) {
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

    public String getOriginUrl() {
        return originUrl;
    }

    public String getTenantHeaderName() {
        return tenantHeaderName;
    }

    public String getTenantHeaderValue() {
        return tenantHeaderValue;
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
        private String originUrl;
        private String tenantHeaderName;
        private String tenantHeaderValue;
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

        public Builder originUrl(String originUrl) {
            this.originUrl = originUrl;
            return this;
        }

        public Builder tenantHeader(String name, String value) {
            this.tenantHeaderName = name;
            this.tenantHeaderValue = value;
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
