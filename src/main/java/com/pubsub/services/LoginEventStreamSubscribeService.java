package com.pubsub.services;

import com.pubsub.config.SalesforceSubscribeConfig;
import com.pubsub.utils.SalesforceSessionTokenService;
import com.salesforce.eventbus.protobuf.ReplayPreset;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginEventStreamSubscribeService {
    private final Subscribe subscribe;
    private final SalesforceSubscribeConfig salesforceSubscribeConfig;
    private final SalesforceSessionTokenService salesforceSessionTokenService;

    private static final String TOPIC = "/event/LoginEventStream";
    private static final int NUMBER_OF_EVENTS_TO_SUBSCRIBE_IN_EACH_FETCH_REQUEST = 5;

    @PostConstruct
    @ConditionalOnProperty(value = "salesforce-subscribe-config.event-listening-on", havingValue = "true")
    public void startStreamEventSubscription() {
        if (isTopicActive()) {
            subscribe.startSubscription(TOPIC, NUMBER_OF_EVENTS_TO_SUBSCRIBE_IN_EACH_FETCH_REQUEST, ReplayPreset.LATEST, salesforceSessionTokenService.login());
        } else {
            log.warn("Topic {} is not active in the configuration. Subscription will not start.", TOPIC);
        }
    }

    private boolean isTopicActive() {
        return salesforceSubscribeConfig.getActiveEvents().contains(TOPIC);
    }
}
