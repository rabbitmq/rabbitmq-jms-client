# Java JMS Client Documentation Changes for RJMS 1.3.3

----

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

or a Wildfly xml configuration example:

```xml
<bindings>
    <object-factory name="java:global/jms/ConnectionFactory" module="org.jboss.genericjms.provider" class="com.rabbitmq.jms.admin.RMQObjectFactory">
        <environment>
            <property name="className" value="javax.jms.ConnectionFactory"/>
            <property name="username" value="guest"/>
            <property name="password" value="guest"/>
            <property name="virtualHost" value="/"/>
            <property name="host" value="localhost"/>
            <property name="ssl" value="true"/>
        </environment>
    </object-factory>
</bindings>
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
    <td>uri</td>
    <td>&nbsp;</td>
    <td>
      The AMQP URI string used to establish a RabbitMQ connection. The value can encode the <code>host</code>, <code>port</code>,
      <code>username</code>, <code>password</code> and <code>virtualHost</code> properties in a single string.
      Both ‘amqp’ and ‘amqps’ schemes are
      accepted, which set the <code>ssl</code> property.
      See the <a href="http://www.rabbitmq.com/uri-spec.html">amqp uri spec</a> on the public RabbitMQ site for details. <b><i>Note:</i></b> this
      property, if present, is applied first. The other attributes, if present, will override the ones set in <code>uri</code>.
      There is no default value, if not present it is not set.
    </td>
  </tr>
  <tr>
    <td>host</td>
    <td>&nbsp;</td>
    <td>Host on which RabbitMQ is running. The default is “localhost”.</td>
  </tr>
  <tr>
    <td>password</td>
    <td>&nbsp;</td>
    <td>Password to use to authenticate a connection with the RabbitMQ broker. The default is “guest”.</td>
  </tr>
  <tr>
    <td>port</td>
    <td>&nbsp;</td>
    <td>RabbitMQ port used for connections. The default is “5672” unless this is an SSL connection, in which case the default is “5671”.</td>
  </tr>
  <tr>
    <td>queueBrowserReadMax</td>
    <td>&nbsp;</td>
    <td>
      The maximum number of messages to read on a queue browser. Non-positive values are set to zero, which
      means there is no limit. The default is zero.
    </td>
  </tr>
  <tr>
    <td>ssl</td>
    <td>&nbsp;</td>
    <td>Whether to use an SSL connection to RabbitMQ. The default is “false”.</td>
  </tr>
  <tr>
    <td>terminationTimeout</td>
    <td>&nbsp;</td>
    <td>
      The time (in milliseconds) a <code>Connection.close()</code> should wait for threads/tasks/listeners to complete.
      This attribute is <i><b>not</b></i> preserved in a <code>Reference</code> object produced by <code>RMQConnectionFactory.getReference()</code>
      but it is used if found on a <code>Reference</code> object passed to <code>RMQObjectFactory</code>. The default is that
      obtained from the default constructor <code>RMQConnectionFactory()</code>.
    </td>
  </tr>
  <tr>
    <td>username</td>
    <td>&nbsp;</td>
    <td>Name to use to authenticate a connection with the RabbitMQ broker. The default is “guest”.</td>
  </tr>
  <tr>
    <td>virtualHost</td>
    <td>&nbsp;</td>
    <td>RabbitMQ virtual host within which the application will operate. The default is “/”.</td>
  </tr>
</table>

----
