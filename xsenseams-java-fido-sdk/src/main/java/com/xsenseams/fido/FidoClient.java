package com.xsenseams.fido;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xsenseams.fido.dto.*;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Java client for the six XSenseAMS FIDO (WebAuthn) routes.
 * Every call sends the tenant API key. Start calls can select a FIDO server configuration
 * by id, by the Origin header, or by leaving both unset so AMS uses the primary configuration.
 */
public class FidoClient {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final String API_KEY_HEADER = "X-AMS-API-Key";
    private static final String ORIGIN_HEADER = "Origin";
    private static final String CONFIG_ID_QUERY = "fido_server_config_id";

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
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
     * @return session_id and credential_creation for navigator.credentials.create
     */
    public MakeCredentialResponse makeCredentialRequest(MakeCredentialRequest request) throws FidoApiException {
        return makeCredentialRequest(request, null);
    }

    /**
     * Start FIDO credential registration and send this page origin on the Origin header.
     * A non-blank origin replaces the origin configured on {@link FidoClientConfig}.
     */
    public MakeCredentialResponse makeCredentialRequest(MakeCredentialRequest request, String origin) throws FidoApiException {
        applyConfiguredConfigId(request);
        return post(PATH_MAKE_CREDENTIAL_REQUEST, request, MakeCredentialResponse.class, origin);
    }

    /**
     * Finish FIDO credential registration.
     * POST /api/fidomakecredentialresponse
     * Does not send fido_server_config_id. AMS reads the configuration stored on the session.
     */
    public BaseResponse makeCredentialResponse(MakeCredentialFinishRequest request) throws FidoApiException {
        return makeCredentialResponse(request, null);
    }

    /**
     * Finish FIDO credential registration, sending the page origin on the Origin header.
     */
    public BaseResponse makeCredentialResponse(MakeCredentialFinishRequest request, String origin) throws FidoApiException {
        return post(PATH_MAKE_CREDENTIAL_RESPONSE, request, BaseResponse.class, origin);
    }

    /**
     * Start username FIDO login.
     * POST /api/fidogetassertion
     *
     * @return session_id and credential_assertion for navigator.credentials.get
     */
    public GetAssertionResponse getAssertionRequest(GetAssertionRequest request) throws FidoApiException {
        return getAssertionRequest(request, null);
    }

    /**
     * Start username FIDO login and send this page origin on the Origin header.
     */
    public GetAssertionResponse getAssertionRequest(GetAssertionRequest request, String origin) throws FidoApiException {
        applyConfiguredConfigId(request);
        return post(PATH_GET_ASSERTION, request, GetAssertionResponse.class, origin);
    }

    /**
     * Start usernameless FIDO login.
     * GET /api/fidogetassertioninit
     * Uses {@link FidoClientConfig#getFidoServerConfigId()} when the caller does not pass an id.
     * With no configured id, AMS selects from the Origin header, then the primary configuration.
     */
    public GetAssertionResponse getAssertionInit() throws FidoApiException {
        return getAssertionInit(null, null);
    }

    /**
     * Start usernameless FIDO login for one FIDO server configuration.
     * The id is sent as the {@code fido_server_config_id} query parameter. A blank id is omitted.
     */
    public GetAssertionResponse getAssertionInit(String fidoServerConfigId) throws FidoApiException {
        return getAssertionInit(fidoServerConfigId, null);
    }

    /**
     * Start usernameless FIDO login for one configuration and one page origin.
     * GET /api/fidogetassertioninit?fido_server_config_id=...
     */
    public GetAssertionResponse getAssertionInit(String fidoServerConfigId, String origin) throws FidoApiException {
        return get(PATH_GET_ASSERTION_INIT, resolveConfigId(fidoServerConfigId), origin, GetAssertionResponse.class);
    }

    /**
     * Finish usernameless FIDO login.
     * POST /api/fidogetassertioninitfinish
     * The response includes the AMS username taken from the assertion user handle.
     */
    public GetAssertionInitFinishResponse getAssertionInitFinish(GetAssertionFinishRequest request) throws FidoApiException {
        return getAssertionInitFinish(request, null);
    }

    /**
     * Finish usernameless FIDO login, sending the page origin on the Origin header.
     */
    public GetAssertionInitFinishResponse getAssertionInitFinish(GetAssertionFinishRequest request, String origin) throws FidoApiException {
        return post(PATH_GET_ASSERTION_INIT_FINISH, request, GetAssertionInitFinishResponse.class, origin);
    }

    /**
     * Finish username FIDO login.
     * POST /api/fidogetassertionresponse
     * This call does not issue an AMS access token. The caller decides what its own session does.
     */
    public BaseResponse getAssertionResponse(GetAssertionFinishRequest request) throws FidoApiException {
        return getAssertionResponse(request, null);
    }

    /**
     * Finish username FIDO login, sending the page origin on the Origin header.
     */
    public BaseResponse getAssertionResponse(GetAssertionFinishRequest request, String origin) throws FidoApiException {
        return post(PATH_GET_ASSERTION_RESPONSE, request, BaseResponse.class, origin);
    }

    private void applyConfiguredConfigId(MakeCredentialRequest request) {
        if (request != null && request.getFidoServerConfigId() == null) {
            request.setFidoServerConfigId(config.getFidoServerConfigId());
        }
    }

    private void applyConfiguredConfigId(GetAssertionRequest request) {
        if (request != null && request.getFidoServerConfigId() == null) {
            request.setFidoServerConfigId(config.getFidoServerConfigId());
        }
    }

    private String resolveConfigId(String fidoServerConfigId) {
        String explicit = FidoClientConfig.normalizeConfigId(fidoServerConfigId);
        return explicit != null ? explicit : config.getFidoServerConfigId();
    }

    private Request.Builder requestBuilder(HttpUrl url, String origin) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader(API_KEY_HEADER, config.getApiKey());
        String resolvedOrigin = FidoClientConfig.normalizeOrigin(origin);
        if (resolvedOrigin == null) {
            resolvedOrigin = config.getOrigin();
        }
        if (resolvedOrigin != null) {
            builder.addHeader(ORIGIN_HEADER, resolvedOrigin);
        }
        if (config.getTenantHeaderName() != null && config.getTenantHeaderValue() != null) {
            builder.addHeader(config.getTenantHeaderName(), config.getTenantHeaderValue());
        }
        return builder;
    }

    private HttpUrl urlFor(String path, String fidoServerConfigId) throws FidoApiException {
        HttpUrl parsed = HttpUrl.parse(config.getBaseUrl() + path);
        if (parsed == null) {
            throw new FidoApiException("Invalid AMS URL: " + config.getBaseUrl() + path, -1);
        }
        if (fidoServerConfigId == null || fidoServerConfigId.isBlank()) {
            return parsed;
        }
        return parsed.newBuilder()
                .addQueryParameter(CONFIG_ID_QUERY, fidoServerConfigId.trim())
                .build();
    }

    private <Resp> Resp get(String path, String fidoServerConfigId, String origin, Class<Resp> responseType) throws FidoApiException {
        Request request = requestBuilder(urlFor(path, fidoServerConfigId), origin)
                .get()
                .build();
        return execute(request, responseType);
    }

    private <Resp> Resp post(String path, Object body, Class<Resp> responseType, String origin) throws FidoApiException {
        byte[] jsonBytes;
        try {
            jsonBytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception e) {
            throw new FidoApiException("Failed to serialize request body", -1, e);
        }
        Request request = requestBuilder(urlFor(path, null), origin)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBytes, JSON_MEDIA_TYPE))
                .build();
        return execute(request, responseType);
    }

    private <Resp> Resp execute(Request request, Class<Resp> responseType) throws FidoApiException {
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            int code = response.code();

            if (!response.isSuccessful()) {
                throw new FidoApiException(parseErrorMessage(responseBody, code), code);
            }

            if (responseBody.trim().isEmpty()) {
                throw new FidoApiException("Empty response body", code);
            }

            try {
                Resp parsed = objectMapper.readValue(responseBody, responseType);
                if (parsed instanceof BaseResponse) {
                    BaseResponse br = (BaseResponse) parsed;
                    if (Boolean.FALSE.equals(br.getStatus())) {
                        String message = br.getMessage() != null ? br.getMessage() : "API returned status false";
                        throw new FidoApiException(message, code);
                    }
                }
                return parsed;
            } catch (FidoApiException e) {
                throw e;
            } catch (Exception e) {
                throw new FidoApiException("Failed to parse response: " + e.getMessage(), code, e);
            }
        } catch (FidoApiException e) {
            throw e;
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
