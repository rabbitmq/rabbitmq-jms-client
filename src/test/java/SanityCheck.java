///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.rabbitmq.jms:rabbitmq-jms:${version}
//DEPS org.slf4j:slf4j-simple:1.7.36

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.QueueConnection;
import jakarta.jms.QueueReceiver;
import jakarta.jms.QueueSender;
import jakarta.jms.QueueSession;
import jakarta.jms.Session;
import org.slf4j.LoggerFactory;

public class SanityCheck {

  public static void main(String[] args) throws Exception {
    QueueConnection connection = null;
    try {
      connection = (QueueConnection) new RMQConnectionFactory().createConnection();
      connection.start();
      QueueSession queueSession = connection.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
      Queue queue = queueSession.createTemporaryQueue();

      QueueSender queueSender = queueSession.createSender(queue);
      queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
      Message message = queueSession.createTextMessage("Hello");
      queueSender.send(message);

      QueueReceiver queueReceiver = queueSession.createReceiver(queue);
      message = queueReceiver.receive(5000);
      if (message == null) throw new IllegalStateException("Didn't receive message in 5 seconds");
      LoggerFactory.getLogger("rabbitmq")
          .info(
              "Test succeeded with JMS Client {}",
              RMQConnectionFactory.class.getPackage().getImplementationVersion());
      System.exit(0);
    } catch (Exception e) {
      LoggerFactory.getLogger("rabbitmq")
          .info(
              "Test failed with JMS Client {}",
              RMQConnectionFactory.class.getPackage().getImplementationVersion(),
              e);
      if (connection != null) {
        connection.close();
      }
      System.exit(1);
    }
  }
}
