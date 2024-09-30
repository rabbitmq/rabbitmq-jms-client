package com.rabbitmq.jms.admin;

/**
 * Default destinations for RabbitMQ JMS.
 */
public class RMQDefaultDestinations {
    private static final String JMS_DURABLE_TOPIC_EXCHANGE_NAME = "jms.durable.topic";  // fixed topic exchange in RabbitMQ for jms traffic
    private static final String JMS_TEMP_TOPIC_EXCHANGE_NAME = "jms.temp.topic";        // fixed topic exchange in RabbitMQ for jms traffic

    private static final String JMS_DURABLE_QUEUE_EXCHANGE_NAME = "jms.durable.queues"; // fixed queue exchange in RabbitMQ for jms traffic
    private static final String JMS_TEMP_QUEUE_EXCHANGE_NAME = "jms.temp.queues";       // fixed queue exchange in RabbitMQ for jms traffic

    private static final String JMS_CONSUMER_QUEUE_NAME_PREFIX = "jms-cons-";
    private static final String JMS_DURABLE_TOPIC_SELECTOR_EXCHANGE_PREFIX = "jms-dutop-slx-";
    private static final String JMS_NON_DURABLE_TOPIC_SELECTOR_EXCHANGE_PREFIX = "jms-ndtop-slx-";

    private static final String JMS_TEMP_QUEUE_PREFIF = "jms-temp-queue-";
    private static final String JMS_TEMP_TOPIC_PREFIF = "jms-temp-topic-";

    private String durableTopicExchangeName;
    private String tempTopicExchangeName;
    private String durableQueueExchangeName;
    private String tempQueueExchangeName;
    private String consumerQueueNamePrefix;
    private String durableTopicSelectorExchangePrefix;
    private String nonDurableTopicSelectorExchangePrefix;
    private String tempQueuePrefix;
    private String tempTopicPrefix;

    private static RMQDefaultDestinations instance;

    private RMQDefaultDestinations() {
        this.durableTopicExchangeName = JMS_DURABLE_TOPIC_EXCHANGE_NAME;
        this.tempTopicExchangeName = JMS_TEMP_TOPIC_EXCHANGE_NAME;
        this.durableQueueExchangeName = JMS_DURABLE_QUEUE_EXCHANGE_NAME;
        this.tempQueueExchangeName = JMS_TEMP_QUEUE_EXCHANGE_NAME;
        this.consumerQueueNamePrefix = JMS_CONSUMER_QUEUE_NAME_PREFIX;
        this.durableTopicSelectorExchangePrefix = JMS_DURABLE_TOPIC_SELECTOR_EXCHANGE_PREFIX;
        this.nonDurableTopicSelectorExchangePrefix = JMS_NON_DURABLE_TOPIC_SELECTOR_EXCHANGE_PREFIX;
        this.tempQueuePrefix = JMS_TEMP_QUEUE_PREFIF;
        this.tempTopicPrefix = JMS_TEMP_TOPIC_PREFIF;
    }

    public static RMQDefaultDestinations getInstance() {
        if (instance == null) {
            instance = new RMQDefaultDestinations();
        }
        return instance;
    }

    public String getDurableTopicExchangeName() {
        return durableTopicExchangeName;
    }

    public void setDurableTopicExchangeName(String durableTopicExchangeName) {
        this.durableTopicExchangeName = durableTopicExchangeName;
    }

    public String getTempTopicExchangeName() {
        return tempTopicExchangeName;
    }

    public void setTempTopicExchangeName(String tempTopicExchangeName) {
        this.tempTopicExchangeName = tempTopicExchangeName;
    }

    public String getDurableQueueExchangeName() {
        return durableQueueExchangeName;
    }

    public void setDurableQueueExchangeName(String durableQueueExchangeName) {
        this.durableQueueExchangeName = durableQueueExchangeName;
    }

    public String getTempQueueExchangeName() {
        return tempQueueExchangeName;
    }

    public void setTempQueueExchangeName(String tempQueueExchangeName) {
        this.tempQueueExchangeName = tempQueueExchangeName;
    }

    public String getConsumerQueueNamePrefix() {
        return consumerQueueNamePrefix;
    }

    public void setConsumerQueueNamePrefix(String consumerQueueNamePrefix) {
        this.consumerQueueNamePrefix = consumerQueueNamePrefix;
    }

    public String getDurableTopicSelectorExchangePrefix() {
        return durableTopicSelectorExchangePrefix;
    }

    public void setDurableTopicSelectorExchangePrefix(String durableTopicSelectorExchangePrefix) {
        this.durableTopicSelectorExchangePrefix = durableTopicSelectorExchangePrefix;
    }

    public String getNonDurableTopicSelectorExchangePrefix() {
        return nonDurableTopicSelectorExchangePrefix;
    }

    public void setNonDurableTopicSelectorExchangePrefix(String nonDurableTopicSelectorExchangePrefix) {
        this.nonDurableTopicSelectorExchangePrefix = nonDurableTopicSelectorExchangePrefix;
    }

    public String getTempQueuePrefix() {
        return tempQueuePrefix;
    }

    public void setTempQueuePrefix(String tempQueuePrefix) {
        this.tempQueuePrefix = tempQueuePrefix;
    }

    public String getTempTopicPrefix() {
        return tempTopicPrefix;
    }

    public void setTempTopicPrefix(String tempTopicPrefix) {
        this.tempTopicPrefix = tempTopicPrefix;
    }
}
