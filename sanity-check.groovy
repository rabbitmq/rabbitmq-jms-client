@GrabResolver(name = 'rabbitmq-bintray', root = 'https://dl.bintray.com/rabbitmq/maven')
@GrabResolver(name = 'rabbitmq-packagecloud-milestones', root = 'https://packagecloud.io/rabbitmq/maven-milestones/maven2')
@Grab(group = 'com.rabbitmq.jms', module = 'rabbitmq-jms', version = "${version}")
@Grab(group = 'org.slf4j', module = 'slf4j-simple', version = '1.7.25')
import com.rabbitmq.jms.admin.RMQConnectionFactory
import org.slf4j.LoggerFactory

import javax.jms.DeliveryMode
import javax.jms.QueueReceiver
import javax.jms.QueueSender
import javax.jms.QueueSession
import javax.jms.Session

def connection = new RMQConnectionFactory().createConnection()
try {
    connection.start();
    QueueSession queueSession = connection.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
    def queue = queueSession.createTemporaryQueue()

    QueueSender queueSender = queueSession.createSender(queue)
    queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT)
    def message = queueSession.createTextMessage("Hello")
    queueSender.send(message)

    QueueReceiver queueReceiver = queueSession.createReceiver(queue)
    message = queueReceiver.receive(5000)
    if (message == null)
        throw new IllegalStateException("Didn't receive message in 5 seconds")
    LoggerFactory.getLogger("rabbitmq").info("Test succeeded")
    System.exit 0
} catch (Exception e) {
    LoggerFactory.getLogger("rabbitmq").info("Test failed", e)
    System.exit 1
}