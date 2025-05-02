package com.pubsub.utils;

import com.pubsub.config.SalesforceSubscribeConfig;
import com.pubsub.exceptions.SalesforceLoginException;
import io.grpc.CallCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesforceSessionTokenService {

    private static final String AUTH_ENDPOINT = "services/oauth2/token";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_CREDENTIALS = "client_credentials";

    private static final String ERROR_INVALID_URI = "Invalid URI syntax: {}";

    private static final String LOG_BEARER_TOKEN_SUCCESS = "Salesforce Bearer token acquired successfully.";
    private static final String LOG_BEARER_TOKEN_MISSING = "Salesforce connection parameters may be incorrect. Bearer token not returned.";
    private static final String LOG_LOGIN_FAILED = "Failed to acquire Salesforce bearer token. Status code: {}, Response: {}";

    private final SalesforceSubscribeConfig salesforceSubscribeConfig;
    private final RestClient salesforceRestClient;

    public CallCredentials login() {
        try {
            return handleResponse(salesforceRestClient.post().uri(AUTH_ENDPOINT).contentType(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON).body(buildCredentials()).retrieve().onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                throw new SalesforceLoginException(response.getBody().toString());
            }).toEntity(OAuthResponse.class));
        } catch (IOException e) {
            return null;
        }
    }

    private MultiValueMap<String, String> buildCredentials() {
        MultiValueMap<String, String> parts = new LinkedMultiValueMap<>();

        parts.add("client_id", salesforceSubscribeConfig.getClientId());
        parts.add("client_secret", salesforceSubscribeConfig.getClientSecret());
        parts.add(GRANT_TYPE, CLIENT_CREDENTIALS);

        return parts;
    }

    private CallCredentials handleResponse(ResponseEntity<OAuthResponse> response) throws IOException {
        if (HttpStatus.valueOf(response.getStatusCode().value()).is2xxSuccessful()) {
            assert response.getBody() != null;
            return parseSuccessfulResponse(response.getBody());
        } else {
            log.error(LOG_LOGIN_FAILED, response.getStatusCode(), response.getBody());
            return null;
        }
    }

    private CallCredentials parseSuccessfulResponse(OAuthResponse oAuthResponse) {

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

}