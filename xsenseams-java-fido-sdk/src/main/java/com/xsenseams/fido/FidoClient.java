package com.xsenseams.fido;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xsenseams.fido.dto.*;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Java client for XSenseAMS FIDO (WebAuthn) APIs.
 * Calls the four endpoints: make credential request/response, get assertion request/response.
 */
public class FidoClient {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final String API_KEY_HEADER = "X-AMS-API-Key";

    private static final String PATH_MAKE_CREDENTIAL_REQUEST = "/api/fidomakecredentialrequest";
    private static final String PATH_MAKE_CREDENTIAL_RESPONSE = "/api/fidomakecredentialresponse";
    private static final String PATH_GET_ASSERTION = "/api/fidogetassertion";
    private static final String PATH_GET_ASSERTION_INIT = "/api/fidogetassertioninit";
    private static final String PATH_GET_ASSERTION_INIT_FINISH = "/api/fidogetassertioninitfinish";
    private static final String PATH_GET_ASSERTION_RESPONSE = "/api/fidogetassertionresponse";

    private final FidoClientConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FidoClient(FidoClientConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.httpClient = config.getCustomHttpClient() != null
                ? config.getCustomHttpClient()
                : defaultHttpClient(config);
        this.objectMapper = new ObjectMapper();
    }

    private static OkHttpClient defaultHttpClient(FidoClientConfig config) {
        return new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(config.getReadTimeoutSeconds(), TimeUnit.SECONDS)
                .build();
    }

    /**
     * Start FIDO credential registration.
     * POST /api/fidomakecredentialrequest
     *
     * @return MakeCredentialResponse with session_id and credential_creation for the authenticator
     */
    public MakeCredentialResponse makeCredentialRequest(MakeCredentialRequest request) throws FidoApiException {
        return post(PATH_MAKE_CREDENTIAL_REQUEST, request, MakeCredentialResponse.class);
    }

    /**
     * Finish FIDO credential registration.
     * POST /api/fidomakecredentialresponse
     *
     * @return BaseResponse (status, message); throws FidoApiException on error
     */
    public BaseResponse makeCredentialResponse(MakeCredentialFinishRequest request) throws FidoApiException {
        return post(PATH_MAKE_CREDENTIAL_RESPONSE, request, BaseResponse.class);
    }

    /**
     * Start FIDO assertion (login).
     * POST /api/fidogetassertion
     *
     * @return GetAssertionResponse with session_id and credential_assertion for the authenticator
     */
    public GetAssertionResponse getAssertionRequest(GetAssertionRequest request) throws FidoApiException {
        return post(PATH_GET_ASSERTION, request, GetAssertionResponse.class);
    }

     /**
     * Start FIDO assertion (login).
     * POST /api/fidogetassertioninit
     *
     * @return GetAssertionResponse with session_id and credential_assertion for the authenticator
     */
    public GetAssertionResponse getAssertionInit() throws FidoApiException {
        return get(PATH_GET_ASSERTION_INIT, GetAssertionResponse.class);
    }

     /**
     * Start FIDO assertion (login).
     * POST /api/fidogetassertioninitfinish
     *
     * @return GetAssertionResponse with session_id and credential_assertion for the authenticator
     */
    public GetAssertionInitFinishResponse getAssertionInitFinish(GetAssertionFinishRequest request) throws FidoApiException {
        return post(PATH_GET_ASSERTION_INIT_FINISH, request, GetAssertionInitFinishResponse.class);
    }

    /**
     * Finish FIDO assertion (login).
     * POST /api/fidogetassertionresponse
     *
     * @return BaseResponse (status, message); throws FidoApiException on error
     */
    public BaseResponse getAssertionResponse(GetAssertionFinishRequest request) throws FidoApiException {
        return post(PATH_GET_ASSERTION_RESPONSE, request, BaseResponse.class);
    }

    private <Resp> Resp get(String path, Class<Resp> responseType) throws FidoApiException {
        String url = config.getBaseUrl() + path;
        Request.Builder reqBuilder = new Request.Builder()
                .url(url)
                .addHeader(API_KEY_HEADER, config.getApiKey())
                .addHeader("Content-Type", "application/json");

        if (config.getTenantHeaderName() != null && config.getTenantHeaderValue() != null) {
            reqBuilder.addHeader(config.getTenantHeaderName(), config.getTenantHeaderValue());
        }

        Request request = reqBuilder
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            int code = response.code();

            if (!response.isSuccessful()) {
                String msg = parseErrorMessage(responseBody, code);
                throw new FidoApiException(msg, code);
            }

            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new FidoApiException("Empty response body", code);
            }

            try {
                Resp parsed = objectMapper.readValue(responseBody, responseType);
                if (parsed instanceof BaseResponse) {
                    BaseResponse br = (BaseResponse) parsed;
                    if (Boolean.FALSE.equals(br.getStatus())) {
                        throw new FidoApiException(br.getMessage() != null ? br.getMessage() : "API returned status false", code);
                    }
                }
                return parsed;
            } catch (Exception e) {
                throw new FidoApiException("Failed to parse response: " + e.getMessage(), code, e);
            }
        } catch (IOException e) {
            throw new FidoApiException("Request failed: " + e.getMessage(), -1, e);
        }
    }

    private <Req, Resp> Resp post(String path, Req body, Class<Resp> responseType) throws FidoApiException {
        String url = config.getBaseUrl() + path;
        Request.Builder reqBuilder = new Request.Builder()
                .url(url)
                .addHeader(API_KEY_HEADER, config.getApiKey())
                .addHeader("Content-Type", "application/json");

        if (config.getTenantHeaderName() != null && config.getTenantHeaderValue() != null) {
            reqBuilder.addHeader(config.getTenantHeaderName(), config.getTenantHeaderValue());
        }

        byte[] jsonBytes;
        try {
            jsonBytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception e) {
            throw new FidoApiException("Failed to serialize request body", -1, e);
        }

        Request request = reqBuilder
                .post(RequestBody.create(jsonBytes, JSON_MEDIA_TYPE))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            int code = response.code();

            if (!response.isSuccessful()) {
                String msg = parseErrorMessage(responseBody, code);
                throw new FidoApiException(msg, code);
            }

            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new FidoApiException("Empty response body", code);
            }

            try {
                Resp parsed = objectMapper.readValue(responseBody, responseType);
                if (parsed instanceof BaseResponse) {
                    BaseResponse br = (BaseResponse) parsed;
                    if (Boolean.FALSE.equals(br.getStatus())) {
                        throw new FidoApiException(br.getMessage() != null ? br.getMessage() : "API returned status false", code);
                    }
                }
                return parsed;
            } catch (Exception e) {
                throw new FidoApiException("Failed to parse response: " + e.getMessage(), code, e);
            }
        } catch (IOException e) {
            throw new FidoApiException("Request failed: " + e.getMessage(), -1, e);
        }
    }

    private String parseErrorMessage(String responseBody, int code) {
        if (responseBody != null && !responseBody.trim().isEmpty()) {
            try {
                BaseResponse err = objectMapper.readValue(responseBody, BaseResponse.class);
                if (err.getMessage() != null) {
                    return err.getMessage();
                }
            } catch (Exception ignored) {
                // use default
            }
        }
        return "HTTP " + code;
    }
}
