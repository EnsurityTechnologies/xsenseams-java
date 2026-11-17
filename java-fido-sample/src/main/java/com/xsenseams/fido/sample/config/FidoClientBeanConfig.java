package com.xsenseams.fido.sample.config;

import com.xsenseams.fido.FidoClient;
import com.xsenseams.fido.FidoClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FidoClientBeanConfig {

    @Value("${ams.base-url}")
    private String baseUrl;

    @Value("${ams.api-key}")
    private String apiKey;

    @Value("${ams.tenant-header-name:}")
    private String tenantHeaderName;

    @Value("${ams.tenant-header-value:}")
    private String tenantHeaderValue;

    @Bean
    public FidoClient fidoClient() {
        FidoClientConfig.Builder builder = FidoClientConfig.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey);
        if (tenantHeaderName != null && !tenantHeaderName.isBlank()
                && tenantHeaderValue != null && !tenantHeaderValue.isBlank()) {
            builder.tenantHeader(tenantHeaderName, tenantHeaderValue);
        }
        return new FidoClient(builder.build());
    }
}
