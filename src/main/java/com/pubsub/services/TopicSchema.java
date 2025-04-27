package com.pubsub.services;

import com.pubsub.utils.SalesforceSessionTokenService;
import com.salesforce.eventbus.protobuf.SchemaInfo;
import com.salesforce.eventbus.protobuf.TopicInfo;
import com.salesforce.eventbus.protobuf.TopicRequest;
import io.grpc.CallCredentials;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.springframework.stereotype.Service;

@Slf4j
@Data
@Service
@RequiredArgsConstructor
public class TopicSchema {
    private final IPubSubService pubSubService;
    private final SalesforceSessionTokenService salesforceSessionTokenService;

    public Schema getSchema(String topicName, CallCredentials callCredentials) {
        TopicInfo topicInfo = fetchTopicInfo(topicName, callCredentials);
        SchemaInfo schemaInfo = fetchSchemaInfo(topicInfo.getSchemaId(), callCredentials);

        Schema schema = new Schema.Parser().parse(schemaInfo.getSchemaJson());
        log.info("Schema of topic {}: {}", topicName, schema.toString());
        return schema;
    }

    public SchemaInfo getSchemaInfo(String topicName, CallCredentials callCredentials) {
        TopicInfo topicInfo = fetchTopicInfo(topicName, callCredentials);
        return fetchSchemaInfo(topicInfo.getSchemaId(), callCredentials);
    }

    private TopicInfo fetchTopicInfo(String topicName, CallCredentials callCredentials) {
        TopicInfo topicInfo = pubSubService.getTopicInfo(
                TopicRequest.newBuilder().setTopicName(topicName).build(), callCredentials);
        log.info("GetTopic Call RPC ID: {}", topicInfo.getRpcId());
        return topicInfo;
    }

    private SchemaInfo fetchSchemaInfo(String schemaId, CallCredentials callCredentials){
        SchemaInfo schemaInfo = pubSubService.getSchemaInfo(schemaId, callCredentials);
        log.info("GetSchema Call RPC ID: {}", schemaInfo.getRpcId());
        return schemaInfo;
    }
}
