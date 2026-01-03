package com.pubsub.utils;

import com.pubsub.config.SalesforceJwtConfig;
import com.pubsub.exceptions.SalesforceLoginException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesforceJwtTokenService {

    private static final String AUTH_ENDPOINT = "/services/oauth2/token";
    private static final String GRANT_TYPE = "grant_type";
    private static final String JWT_BEARER_GRANT = "urn:ietf:params:oauth:grant-type:jwt-bearer";

    private final SalesforceJwtConfig jwtConfig;
    private final SalesforceJwtGenerator jwtGenerator;
    private final RestClient.Builder restClientBuilder;

    public OAuthResponse exchangeToken() {
        try {
            RestClient restClient = restClientBuilder.build();
            String jwt = jwtGenerator.generateToken();

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add(GRANT_TYPE, JWT_BEARER_GRANT);
            body.add("assertion", jwt);

            return restClient.post()
                    .uri(jwtConfig.getLoginUrl() + AUTH_ENDPOINT)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (request, response) -> {
                        throw new SalesforceLoginException("JWT Login failed: " + response.getStatusCode());
                    })
                    .body(OAuthResponse.class);

        } catch (Exception e) {
            log.error("Failed to exchange JWT for access token", e);
            throw new SalesforceLoginException("Failed to exchange JWT for access token", e);
        }
    }
}
