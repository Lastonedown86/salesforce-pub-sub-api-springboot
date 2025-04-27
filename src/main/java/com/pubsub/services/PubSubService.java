package com.pubsub.services;

import com.pubsub.utils.SalesforceSessionTokenService;
import com.pubsub.utils.XClientTraceIdClientInterceptor;
import com.salesforce.eventbus.protobuf.PubSubGrpc;
import com.salesforce.eventbus.protobuf.PublishRequest;
import com.salesforce.eventbus.protobuf.PublishResponse;
import com.salesforce.eventbus.protobuf.SchemaInfo;
import com.salesforce.eventbus.protobuf.SchemaRequest;
import com.salesforce.eventbus.protobuf.TopicInfo;
import com.salesforce.eventbus.protobuf.TopicRequest;
import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.AbstractStub;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@Service
public class PubSubService implements IPubSubService {
    private static final String GRPC_HOST = "api.pubsub.salesforce.com";
    private static final int GRPC_PORT = 7443;

    private final SalesforceSessionTokenService salesforceSessionTokenService;
    private ManagedChannel managedChannel;

    @PostConstruct
    private void initializeChannel() {
        managedChannel = createManagedChannel();
    }

    @Override
    public void checkSubscriptionStatus(String topicName, CallCredentials callCredentials) {
        CallCredentials credentials = Optional.ofNullable(callCredentials)
                .orElseGet(salesforceSessionTokenService::login);

        try {
            TopicRequest request = TopicRequest.newBuilder().setTopicName(topicName).build();
            TopicInfo topicInfo = pubSubBlockingStub(credentials).getTopic(request);

            if (topicInfo.getCanSubscribe()) {
                log.info("Subscription available for topic: {} (RPC Id: {})", topicInfo.getTopicName(), topicInfo.getRpcId());
            } else {
                throw new IllegalArgumentException("Topic " + topicInfo.getTopicName() + " is not available for subscription");
            }
        } catch (Exception e) {
            logError("Error fetching topic details for topic: " + topicName, e);
        }
    }

    @Override
    public String getSchemaJson(String schemaId, CallCredentials callCredentials) {
        try {
            SchemaRequest request = SchemaRequest.newBuilder().setSchemaId(schemaId).build();
            return pubSubBlockingStub(callCredentials).getSchema(request).getSchemaJson();
        } catch (Exception e) {
            logError("Error fetching schema for schemaId: " + schemaId, e);
            return null;
        }
    }

    @Override
    public PubSubGrpc.PubSubBlockingStub pubSubBlockingStub(CallCredentials callCredentials) {
        return createStub(PubSubGrpc::newBlockingStub, callCredentials);
    }

    @Override
    public PubSubGrpc.PubSubStub pubSubAsyncStub(CallCredentials callCredentials) {
        return createStub(PubSubGrpc::newStub, callCredentials);
    }

    private <T extends AbstractStub<T>> T createStub(Function<Channel, T> stubFactory, CallCredentials callCredentials) {
        Channel interceptedChannel = ClientInterceptors.intercept(getOrCreateManagedChannel(), new XClientTraceIdClientInterceptor());
        return stubFactory.apply(interceptedChannel).withCallCredentials(callCredentials);
    }

    private ManagedChannel getOrCreateManagedChannel() {
        if (managedChannel == null || managedChannel.isShutdown() || managedChannel.isTerminated()) {
            managedChannel = createManagedChannel();
        }
        return managedChannel;
    }

    private ManagedChannel createManagedChannel() {
        return ManagedChannelBuilder.forAddress(GRPC_HOST, GRPC_PORT).build();
    }

    @Override
    public void logError(String context, Exception e) {
        log.error(context, e);
        if (e instanceof StatusRuntimeException statusRuntimeException) {
            Optional.ofNullable(statusRuntimeException.getTrailers())
                    .ifPresent(trailers -> trailers.keys().forEach(key ->
                            log.error("[Trailer] Key: {}, Value: {}", key,
                                    trailers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)))));
        }
    }

    @Override
    public Boolean isChannelShutdown() {
        return managedChannel != null && managedChannel.isShutdown();
    }

    @Override
    public PublishResponse publish(PublishRequest publishRequest, CallCredentials callCredentials) {
        try {
            return pubSubBlockingStub(callCredentials).publish(publishRequest);
        } catch (Exception e) {
            log.error("Error publishing message", e);
            return null;
        }
    }

    @Override
    public TopicInfo getTopicInfo(TopicRequest topicName, CallCredentials callCredentials) {
        return pubSubBlockingStub(callCredentials).getTopic(topicName);
    }

    @Override
    public SchemaInfo getSchemaInfo(String schemaId, CallCredentials callCredentials) {
        return pubSubBlockingStub(callCredentials).getSchema(SchemaRequest.newBuilder().setSchemaId(schemaId).build());
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    private void checkChannelState() {

        log.info("Current channel status: {}", getOrCreateManagedChannel().getState(true));
    }
}
