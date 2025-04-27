package com.pubsub.services;

import com.pubsub.utils.SalesforceSessionTokenService;
import com.salesforce.eventbus.protobuf.PublishResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublishService {
    private final Publish publish;
    private final TopicSchema topicSchema;
    private final SalesforceSessionTokenService salesforceSessionTokenService;

    private static final String TOPIC = "/event/MyCustomEvent__e";

    public PublishResponse publishEvent () throws Exception {
        var callCredentials = salesforceSessionTokenService.login();
        var schema = topicSchema.getSchema(TOPIC, callCredentials);
        return publish.publishEvent(TOPIC, createEventMessage(schema), callCredentials);
    }
    private GenericRecord createEventMessage(Schema schema) {
        // Update CreatedById with the appropriate User Id from your org.
        return new GenericRecordBuilder(schema).set("CreatedDate", System.currentTimeMillis()).set("CreatedById", "0051P000003ZvNmQAK").build();
    }
}
