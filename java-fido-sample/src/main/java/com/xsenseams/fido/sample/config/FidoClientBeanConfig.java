package com.xsenseams.fido.sample.config;

import com.xsenseams.fido.FidoClient;
import com.xsenseams.fido.FidoClientConfig;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Configuration
public class FidoClientBeanConfig {

    @Value("${ams.base-url}")
    private String baseUrl;

    @Value("${ams.api-key}")
    private String apiKey;

    @Value("${server.origin-url:}")
    private String originUrl;

    @Value("${ams.tenant-header-name:}")
    private String tenantHeaderName;

    @Value("${ams.tenant-header-value:}")
    private String tenantHeaderValue;

    @PostConstruct
    public void disableGlobalSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            SSLContext.setDefault(sslContext);
        } catch (Exception e) {
            // ignore
        }
    }

    @Bean
    public FidoClient fidoClient() {
        FidoClientConfig.Builder builder = FidoClientConfig.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
            .originUrl(originUrl)
                .customHttpClient(createTrustAllOkHttpClient());

        if (tenantHeaderName != null && !tenantHeaderName.isBlank()
                && tenantHeaderValue != null && !tenantHeaderValue.isBlank()) {
            builder.tenantHeader(tenantHeaderName, tenantHeaderValue);
        }
        return new FidoClient(builder.build());
    }

    private OkHttpClient createTrustAllOkHttpClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create trust-all OkHttpClient for FIDO SDK", e);
        }
    }
}
