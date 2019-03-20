# Java JMS Client Documentation Changes for RJMS 1.1.0

----

----

## Interoperability Feature

### Overview of interoperability feature

Introduced in RJMS Version 1.1.0 the interoperability feature allows JMS ‘amqp’ destinations to be defined which read and/or write to non-JMS RabbitMQ resources.

A JMS destination can be defined so that a JMS application can send `Message`s to a (pre-defined) RabbitMQ ‘destination’ (exchange/routing key) using the JMS API in the normal way. The messages are written ‘in the clear’, which means that any AMQP client can read them without having to understand the internal format of Java JMS messages. Only `BytesMessage`s and `TextMessage`s can be written in this way.

Similarly, a JMS destination can be defined which reads messages from a (pre-defined) RabbitMQ queue.  A JMS application can then read these messages using the JMS API.  The RJMS client packs them up into JMS `Message`s automatically.  Messages read in this way are, by default, `BytesMessage`s, but individual messages can be marked `TextMessage` which will interpret the byte-array payload as a UTF-8 encoded String and return them as `TextMessage`s.

A single ‘amqp’ Destination can be defined for both reading and writing.

The details are below.

### JMS ‘amqp’ `RMQDestination` constructor

The `com.rabbitmq.jms.admin` package contains the `RMQDestination` class, which implements `Destination` in the JMS interface. This is extended with a new constructor:

----

Creates a destination for RJMS mapped onto an AMQP resource.

`amqpExchangeName` and `amqpRoutingKey` must both be **`null`** if either is **`null`**, and `amqpQueueName` may be **`null`**, but at
least one of these three parameters must be non-**`null`**.

**Parameters**:

* **destinationName** - the name of the queue destination
* **amqpExchangeName** - the exchange name for the mapped resource
* **amqpRoutingKey** - the routing key for the mapped resource
* **amqpQueueName** - the queue name of the mapped resource

```java
public RMQDestination(String destName, String amqpExchangeName, String amqpRoutingKey, String amqpQueueName);
```

----

Applications that declare destinations in this way can use them directly, or store them in a JNDI provider for JMS applications to retrieve.

Such destinations are non-temporary, queue destinations.

### JMS ‘amqp’ Destination `xml` definitions

The `RMQDestination` object has new instance fields:

* `amqp` - **`boolean`**, which indicates if this is an AMQP destination (if **`true`**); the default is **`false`**;
* `amqpExchangeName` - `String`, the AMQP exchange name to use when sending messages to this destination, if `amqp` is **`true`**; the default is **`null`**;
* `amqpRoutingKey` - `String`, the AMQP routing key to use when sending messages to this destination, if `amqp` is **`true`**; the default is **`null`**;
* `amqpQueueName` - `String`, the AMQP queue name to use when reading messages from this destination, if `amqp` is **`true`**; the default is **`null`**.

and there are getters and setters for these which means that a JNDI `<Resource/>` definition or a Spring bean definition can use them, for example:

```xml
  <Resource   name="jms/Queue"
              type="javax.jms.Queue"
           factory="com.rabbitmq.jms.admin.RMQObjectFactory"
   destinationName="myQueue"
              amqp="true"
     amqpQueueName="rabbitQueueName"
  />
```

or a Spring bean example:

```xml
  <bean id="jmsDestination" class="com.rabbitmq.jms.admin.RMQDestination" >
    <property name="destinationName" value="myQueue" />
    <property name="amqp"            value="true" />
    <property name="amqpQueueName"   value="rabbitQueueName" />
  </bean>
```

Here is a *complete* list of the attributes/properties now available:

----

<table>
  <tr style="text-align:left;">
    <th>Attribute/Property&nbsp;</th>
    <th>JNDI&nbsp;only?&nbsp;&nbsp;</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>name</td>
    <td>JNDI only</td>
    <td>Name in JNDI.</td>
  </tr>
  <tr>
    <td>type</td>
    <td>JNDI only</td>
    <td>
      Name of the JMS interface the object implements, usually <code>javax.jms.Queue</code>. Other choices
      are <code>javax.jms.Topic</code> and <code>javax.jms.Destination</code>. You can also use the name of
      the (common) implementation class, <code>com.rabbitmq.jms.admin.RMQDestination</code>.
    </td>
  </tr>
  <tr>
    <td>factory</td>
    <td>JNDI only</td>
    <td>JMS Client for RabbitMQ <code>ObjectFactory</code> class, always <code>com.rabbitmq.jms.admin.RMQObjectFactory</code>.</td>
  </tr>
  <tr>
    <td>amqp</td>
    <td>&nbsp;</td>
    <td><code><strong>"true"</strong></code> means this is an ‘amqp’ destination. Default <code><strong>"false"</strong></code>.</td>
  </tr>
  <tr>
    <td>amqpExchangeName</td>
    <td>&nbsp;</td>
    <td>Name of the AMQP exchange to publish messages to when an ‘amqp’ destination. This exchange must exist when messages are published.</td>
  </tr>
  <tr>
    <td>amqpRoutingKey</td>
    <td>&nbsp;</td>
    <td>The routing key to use when publishing messages when an ‘amqp’ destination.</td>
  </tr>
  <tr>
    <td>amqpQueueName</td>
    <td>&nbsp;</td>
    <td>Name of the AMQP queue to receive messages from when an ‘amqp’ destination. This queue must exist when messages are received.</td>
  </tr>
  <tr>
    <td>destinationName</td>
    <td>&nbsp;</td>
    <td>Name of the JMS destination.</td>
  </tr>
</table>

----

----

## Updates for the `ssl` and `uri` attributes on `RMQConnectionFactory`

(The description below should replace part of the ‘Defining JMS Objects in JNDI’ section in ‘Configure JMS Applications to Use JMS Client for vFabric RabbitMQ’
which is in Chapter 3 of *Using the JMS Client for vFabric RabbitMQ*.  The rest of that section can be informed by the preceding description of `RMSDestination` definitions.)

### JMS ConnectionFactory `xml` definitions

The `RMQConnectionFactory` object has properties/attributes settable on `Resource` or Spring bean definitions.
These include the `host` and `port` and `virtualHost` values of the connections created.

For example, a JNDI `ConnectionFactory` `<Resource/>` could be defined as follows:

```xml
  <Resource name="jms/ConnectionFactory"
            type="javax.jms.ConnectionFactory"
         factory="com.rabbitmq.jms.admin.RMQObjectFactory"
        username="guest"
        password="guest"
     virtualHost="/"
            host="localhost"
             ssl="true"
  /> 
```

or a Spring bean example:

```xml
  <bean id="jmsConnectionFactory" class="com.rabbitmq.jms.admin.RMQConnectionFactory" >
    <property name="username" value="guest" />
    <property name="password" value="guest" />
    <property name="virtualHost" value="/" />
    <property name="host" value="localhost" />
    <property name="ssl" value="true" />
  </bean>
```

Here is a complete list of the attributes/properties available:

----

<table>
  <tr style="text-align:left;">
    <th>Attribute/Property&nbsp;</th>
    <th>JNDI&nbsp;only?&nbsp;&nbsp;</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>name</td>
    <td>JNDI only</td>
    <td>Name in JNDI.</td>
  </tr>
  <tr>
    <td>type</td>
    <td>JNDI only</td>
    <td>
      Name of the JMS interface the object implements, usually <code>javax.jms.ConnectionFactory</code>. Other choices
      are <code>javax.jms.QueueConnectionFactory</code> and <code>javax.jms.TopicConnectionFactory</code>. You can also use the name of
      the (common) implementation class, <code>com.rabbitmq.jms.admin.RMQConnectionFactory</code>.
    </td>
  </tr>
  <tr>
    <td>factory</td>
    <td>JNDI only</td>
    <td>JMS Client for RabbitMQ <code>ObjectFactory</code> class, always <code>com.rabbitmq.jms.admin.RMQObjectFactory</code>.</td>
  </tr>
  <tr>
    <td>username</td>
    <td>&nbsp;</td>
    <td>Name to use to authenticate a connection with the RabbitMQ broker. The default is “guest”.</td>
  </tr>
  <tr>
    <td>password</td>
    <td>&nbsp;</td>
    <td>Password to use to authenticate a connection with the RabbitMQ broker. The default is “guest”.</td>
  </tr>
  <tr>
    <td>virtualHost</td>
    <td>&nbsp;</td>
    <td>RabbitMQ virtual host within which the application will operate. The default is “/”.</td>
  </tr>
  <tr>
    <td>host</td>
    <td>&nbsp;</td>
    <td>Host on which RabbitMQ is running. The default is “localhost”.</td>
  </tr>
  <tr>
    <td>port</td>
    <td>&nbsp;</td>
    <td>RabbitMQ port used for connections. The default is “5672” unless this is an SSL connection, in which case the default is “5671”.</td>
  </tr>
  <tr>
    <td>ssl</td>
    <td>&nbsp;</td>
    <td>Whether to use an SSL connection to RabbitMQ. The default is “false”.</td>
  </tr>
  <tr>
    <td>uri</td>
    <td>&nbsp;</td>
    <td>
      The AMQP URI string used to establish a RabbitMQ connection. The value can encode the <code>host</code>, <code>port</code>,
      <code>userid</code>, <code>password</code> and <code>virtualHost</code> in a single string. Both ‘amqp’ and ‘amqps’ schemes are
      accepted.
      See the <a href="https://www.rabbitmq.com/uri-spec.html">amqp uri spec</a> on the public RabbitMQ site for details. Note: this
      property sets other properties and the set order is unspecified.
    </td>
  </tr>
</table>

----
