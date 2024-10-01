package com.rabbitmq.jms.admin;

import java.io.Serializable;

/**
 * Contract to control the name of AMQP entities (exchanges, queues).
 *
 * <p>Naming is considered an implementation details of RabbitMQ JMS, so this interface and its
 * methods can change at any time between releases.
 *
 * <p>A custom implementation can be used to customize the name of some entities (e.g. for security
 * reasons), but implementators must be prepared to API changes.
 *
 * @since 3.4.0
 * @see RMQConnectionFactory#setNamingStrategy(NamingStrategy)
 */
public interface NamingStrategy extends Serializable {

  NamingStrategy DEFAULT = new DefaultNamingStrategy();

  /**
   * Name of the exchange for non-temporary topics.
   *
   * @return exchange name
   */
  String topicExchangeName();

  /**
   * Name of the exchange for temporary topics.
   *
   * @return exchange name
   */
  String temporaryTopicExchangeName();

  /**
   * Name of the exchange for non-temporary queues.
   *
   * @return exchange name
   */
  String queueExchangeName();

  /**
   * Name of the exchange for temporary queues.
   *
   * @return exchange name
   */
  String temporaryQueueExchangeName();

  /**
   * Prefix for the AMQP queue name of topic subscribers.
   *
   * @return queue prefix
   */
  String topicSubscriberQueuePrefix();

  /**
   * Prefix for the selector exchange name of durable topic subscribers.
   *
   * @return exchange prefix
   */
  String durableSubscriberTopicSelectorExchangePrefix();

  /**
   * Prefix for the selector exchange name of non-durable topic subscribers.
   *
   * @return exchange prefix
   */
  String nonDurableSubscriberTopicSelectorExchangePrefix();

  /**
   * Prefix for the AMQP queue name of temporary queues.
   *
   * @return queue prefix
   */
  String temporaryQueuePrefix();

  /**
   * Prefix for the AMQP queue name of temporary topics.
   *
   * @return queue prefix
   */
  String temporaryTopicPrefix();
}
