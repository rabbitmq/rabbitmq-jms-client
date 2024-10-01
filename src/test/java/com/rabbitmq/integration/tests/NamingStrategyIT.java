package com.rabbitmq.integration.tests;

import static com.rabbitmq.TestUtils.name;
import static org.assertj.core.api.Assertions.assertThat;

import com.rabbitmq.TestUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.jms.admin.NamingStrategy;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import jakarta.jms.*;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class NamingStrategyIT {

  RMQConnectionFactory cf;
  Connection c;
  Session s;
  Set<String> events = ConcurrentHashMap.newKeySet();
  Channel amqpChannel;

  @BeforeEach
  void init() throws Exception {
    cf =
        (RMQConnectionFactory)
            AbstractTestConnectionFactory.getTestConnectionFactory().getConnectionFactory();
    NamingStrategy namingStrategy =
        (NamingStrategy)
            Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class<?>[] {NamingStrategy.class},
                (proxy, method, args) -> {
                  events.add(method.getName());
                  return method.invoke(NamingStrategy.DEFAULT, args);
                });
    cf.setNamingStrategy(namingStrategy);
    c = cf.createConnection();
    s = c.createSession();
    c.start();
    amqpChannel = TestUtils.amqpConnection(c).createChannel();
  }

  @AfterEach
  void tearDown() throws Exception {
    amqpChannel.close();
    s.close();
    c.close();
    events.clear();
  }

  @Test
  void topicExchangeName(TestInfo info) throws Exception {
    assertThat(events).isEmpty();
    String name = name(info);
    Topic topic = s.createTopic(name);
    assertThat(events).hasSize(1).contains("topicExchangeName");
    MessageConsumer consumer = s.createConsumer(topic);
    assertThat(events).hasSize(2).contains("topicSubscriberQueuePrefix");
    consumer.close();

    s.createConsumer(topic, "boolProp");
    assertThat(events).hasSize(3).contains("nonDurableSubscriberTopicSelectorExchangePrefix");

    name = name(info);
    consumer = s.createDurableSubscriber(topic, name, "boolProp", false);
    consumer.close();
    deleteQueue(name);
    assertThat(events).hasSize(4).contains("durableSubscriberTopicSelectorExchangePrefix");

    name = name(info);
    s.createQueue(name);
    deleteQueue(name);
    assertThat(events).hasSize(5).contains("queueExchangeName");

    s.createTemporaryTopic();
    assertThat(events).hasSize(7).contains("temporaryTopicPrefix", "temporaryTopicExchangeName");

    s.createTemporaryQueue();
    assertThat(events).hasSize(9).contains("temporaryQueuePrefix", "temporaryQueueExchangeName");
  }

  void deleteQueue(String name) throws IOException {
    this.amqpChannel.queueDelete(name);
  }
}
