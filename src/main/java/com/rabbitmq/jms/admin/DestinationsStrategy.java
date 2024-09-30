package com.rabbitmq.jms.admin;

import java.io.Serializable;

/**
 * Strategy for customise destinations name.
 *
 * @since 3.4.0
 */
public interface DestinationsStrategy extends Serializable {

    default String getDurableTopicExchangeName() {
        return "jms.durable.topic";
    }

    default String getTempTopicExchangeName() {
        return "jms.temp.topic";
    }

    default String getDurableQueueExchangeName() {
        return "jms.durable.queues";
    }

    default String getTempQueueExchangeName() {
        return "jms.temp.queues";
    }

    default String getConsumerQueueNamePrefix() {
        return "jms-cons-";
    }

    default String getDurableTopicSelectorExchangePrefix() {
        return "jms-dutop-slx-";
    }

    default String getNonDurableTopicSelectorExchangePrefix() {
        return "jms-ndtop-slx-";
    }

    default String getTempQueuePrefix() {
        return "jms-temp-queue-";
    }

    default String getTempTopicPrefix() {
        return "jms-temp-queue-";
    }
}
