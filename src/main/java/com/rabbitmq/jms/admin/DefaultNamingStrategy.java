package com.rabbitmq.jms.admin;

/**
 * Default implementation for {@link NamingStrategy}.
 */
final class DefaultNamingStrategy implements NamingStrategy {

  private final String topicExchangeName = "jms.durable.topic";

  private final String temporaryTopicExchangeName = "jms.temp.topic";

  private final String queueExchangeName = "jms.durable.queues";

  private final String temporaryQueueExchangeName = "jms.temp.queues";

  private final String topicSubscriberQueuePrefix = "jms-cons-";

  private final String durableSubscriberTopicSelectorExchangePrefix = "jms-dutop-slx-";

  private final String nonDurableSubscriberTopicSelectorExchangePrefix = "jms-ndtop-slx-";

  private final String temporaryQueuePrefix = "jms-temp-queue-";

  private final String temporaryTopicPrefix = "jms-temp-queue-";

  @Override
  public String topicExchangeName() {
    return this.topicExchangeName;
  }

  @Override
  public String temporaryTopicExchangeName() {
    return this.temporaryTopicExchangeName;
  }

  @Override
  public String queueExchangeName() {
    return this.queueExchangeName;
  }

  @Override
  public String temporaryQueueExchangeName() {
    return this.temporaryQueueExchangeName;
  }

  @Override
  public String topicSubscriberQueuePrefix() {
    return this.topicSubscriberQueuePrefix;
  }

  @Override
  public String durableSubscriberTopicSelectorExchangePrefix() {
    return this.durableSubscriberTopicSelectorExchangePrefix;
  }

  @Override
  public String nonDurableSubscriberTopicSelectorExchangePrefix() {
    return this.nonDurableSubscriberTopicSelectorExchangePrefix;
  }

  @Override
  public String temporaryQueuePrefix() {
    return this.temporaryQueuePrefix;
  }

  @Override
  public String temporaryTopicPrefix() {
    return this.temporaryTopicPrefix;
  }
}
