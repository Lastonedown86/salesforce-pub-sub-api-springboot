package com.pubsub.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pubsub.config.SalesforceSubscribeConfig;
import com.pubsub.exceptions.SalesforceLoginException;
import io.grpc.CallCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesforceSessionTokenService {

    private static final String AUTH_ENDPOINT = "services/oauth2/token";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_CREDENTIALS = "client_credentials";

    private static final String ERROR_MISSING_BASE_URL = "Base URL is missing in the configuration.";
    private static final String ERROR_MISSING_CREDENTIALS = "Client ID or Client Secret is missing in the configuration.";
    private static final String ERROR_INVALID_URI = "Invalid URI syntax: {}";
    private static final String ERROR_LOGIN_INTERRUPTED = "Login process interrupted";
    private static final String ERROR_LOGIN_FAILED = "Error during Salesforce login";

    private static final String LOG_BEARER_TOKEN_SUCCESS = "Salesforce Bearer token acquired successfully.";
    private static final String LOG_BEARER_TOKEN_MISSING = "Salesforce connection parameters may be incorrect. Bearer token not returned.";
    private static final String LOG_LOGIN_FAILED = "Failed to acquire Salesforce bearer token. Status code: {}, Response: {}";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SalesforceSubscribeConfig salesforceSubscribeConfig;

    public CallCredentials login() {
        try {
            final String authUrl = buildAuthUrl();
            final Map<String, String> credentials = buildCredentials();

            HttpResponse<String> response = sendHttpRequest(authUrl, credentials);
            return handleResponse(response);
        } catch (IOException | InterruptedException e) {
            handleException(e);
            return null;
        }
    }

    private String buildAuthUrl() {
        final String baseUrl = salesforceSubscribeConfig.getDomainUrl();
        if (!StringUtils.hasText(baseUrl)) {
            throw new SalesforceLoginException(ERROR_MISSING_BASE_URL);
        }
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path(AUTH_ENDPOINT)
                .toUriString();
    }

    private Map<String, String> buildCredentials() {
        final String clientId = salesforceSubscribeConfig.getClientId();
        final String clientSecret = salesforceSubscribeConfig.getClientSecret();

        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)) {
            throw new SalesforceLoginException(ERROR_MISSING_CREDENTIALS);
        }

        return Map.of(
                GRANT_TYPE, CLIENT_CREDENTIALS,
                "client_id", clientId,
                "client_secret", clientSecret
        );
    }

    private HttpResponse<String> sendHttpRequest(String url, Map<String, String> formData) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .header(CONTENT_TYPE, FORM_URLENCODED)
                .POST(HttpRequest.BodyPublishers.ofString(encodeFormData(formData)))
                .uri(URI.create(url))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private CallCredentials handleResponse(HttpResponse<String> response) throws IOException {
        if (HttpStatus.valueOf(response.statusCode()).is2xxSuccessful()) {
            return parseSuccessfulResponse(response.body());
        } else {
            log.error(LOG_LOGIN_FAILED, response.statusCode(), response.body());
            return null;
        }
    }

    private CallCredentials parseSuccessfulResponse(String responseBody) throws IOException {
        OAuthResponse oAuthResponse = objectMapper.readValue(responseBody, OAuthResponse.class);

        if (!StringUtils.hasText(oAuthResponse.getAccessToken())) {
            log.warn(LOG_BEARER_TOKEN_MISSING);
            return null;
        }

        log.info(LOG_BEARER_TOKEN_SUCCESS);
        APISessionCredentials credentials = new APISessionCredentials();
        credentials.setInstanceURL(oAuthResponse.getInstanceUrl());
        credentials.setToken(oAuthResponse.getAccessToken());
        credentials.setTenantId(extractOrganizationId(oAuthResponse.getId()));
        return credentials;
    }

    private String extractOrganizationId(String url) {
        try {
            String[] segments = new URI(url).getPath().split("/");
            for (int i = 0; i < segments.length - 1; i++) {
                if ("id".equals(segments[i])) {
                    return segments[i + 1];
                }
            }
        } catch (URISyntaxException e) {
            log.error(ERROR_INVALID_URI, e.getMessage());
        }
        return null;
    }

    private String encodeFormData(Map<String, String> formData) {
        return formData.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private void handleException(Exception e) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            log.error(ERROR_LOGIN_INTERRUPTED, e);
            throw new SalesforceLoginException(ERROR_LOGIN_INTERRUPTED, e);
        } else {
            log.error(ERROR_LOGIN_FAILED, e);
            throw new SalesforceLoginException(ERROR_LOGIN_FAILED, e);
        }
    }
}