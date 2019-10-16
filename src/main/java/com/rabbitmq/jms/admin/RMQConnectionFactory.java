/* Copyright (c) 2013-2019 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.admin;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MetricsCollector;
import com.rabbitmq.jms.client.*;
import com.rabbitmq.jms.util.RMQJMSException;
import com.rabbitmq.jms.util.RMQJMSSecurityException;
import com.rabbitmq.jms.util.WhiteListObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.rabbitmq.jms.util.UriCodec.*;

/**
 * RabbitMQ Implementation of JMS {@link ConnectionFactory}
 * TODO - implement SslContext option
 */
public class RMQConnectionFactory implements ConnectionFactory, Referenceable, Serializable, QueueConnectionFactory,
                                 TopicConnectionFactory {
    private final Logger logger = LoggerFactory.getLogger(RMQConnectionFactory.class);

    private static final long serialVersionUID = -4953157213762979615L;

    private static final int DEFAULT_RABBITMQ_SSL_PORT = com.rabbitmq.client.ConnectionFactory.DEFAULT_AMQP_OVER_SSL_PORT;

    private static final int DEFAULT_RABBITMQ_PORT = com.rabbitmq.client.ConnectionFactory.DEFAULT_AMQP_PORT;

    /** Default username to RabbitMQ broker */
    private String username = "guest";
    /** Default password to RabbitMQ broker */
    private String password = "guest";
    /** Default virtualhost */
    private String virtualHost = "/";
    /** Default host to RabbitMQ broker */
    private String host = "localhost";
    /** Default port NOT SET - determined by the type of connection (ssl or non-ssl) */
    private int port = -1;
    /** How long to wait for onMessage to return, in milliseconds */
    private int onMessageTimeoutMs = 2000;
    /**
     * Whether {@link MessageProducer} properties (delivery mode,
     * priority, TTL) take precedence over respective {@link Message}
     * properties or not.
     * Default is true (which is compliant to the JMS specification).
     */
    private boolean preferProducerMessageProperty = true;

    /**
     * Whether requeue message on {@link RuntimeException} in the
     * {@link javax.jms.MessageListener} or not.
     * Default is false.
     */
    private boolean requeueOnMessageListenerException = false;

    /**
     * Whether to commit nack on rollback or not.
     * Default is false.
     */
    private boolean commitNackOnRollback = false;

    /**
     * Whether using auto-delete for server-named queues for non-durable topics.
     * If set to true, those queues will be deleted when the session is closed.
     * If set to false, queues will be deleted when the owning connection is closed.
     * Default is false.
     *
     * @since 1.8.0
     */
    private boolean cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose = false;

    /**
     * Callback to customise properties of outbound AMQP messages.
     *
     * @since 1.9.0
     */
    private BiFunction<AMQP.BasicProperties.Builder, Message, AMQP.BasicProperties.Builder> amqpPropertiesCustomiser;

    /**
     * Collector for AMQP-client metrics.
     *
     * @since 1.10.0
     */
    private MetricsCollector metricsCollector = new NoOpMetricsCollector();

    /**
     * For post-processing the {@link com.rabbitmq.client.ConnectionFactory} before creating the AMQP connection.
     *
     * @since 1.10.0
     */
    private Consumer<com.rabbitmq.client.ConnectionFactory> amqpConnectionFactoryPostProcessor = new NoOpSerializableConsumer<>();

    /**
     * Callback before sending a message.
     *
     * @since 1.11.0
     */
    private SendingContextConsumer sendingContextConsumer = new NoOpSerializableSendingContextConsumer();

    /**
     * Callback before receiving a message.
     *
     * @since 1.11.0
     */
    private ReceivingContextConsumer receivingContextConsumer = new NoOpSerializableReceivingContextConsumer();

    /**
     * Callback to be notified of publisher confirms.
     * <p>
     * When this property is set, publisher confirms are enabled for all
     * the underlying AMQP {@link com.rabbitmq.client.Channel}s created by
     * this {@link ConnectionFactory}.
     *
     * @see <a href="https://www.rabbitmq.com/confirms.html#publisher-confirms">Publisher Confirms</a>
     * @see <a href="https://www.rabbitmq.com/publishers.html#data-safety">Publisher Guide</a>
     * @see ConfirmListener
     * @since 1.13.0
     */
    private ConfirmListener confirmListener;


    /** Default not to use ssl */
    private boolean ssl = false;
    private String tlsProtocol;
    private SSLContext sslContext;
    private boolean useDefaultSslContext = false;

    /**
     * Whether to use hostname verification when TLS is on.
     *
     * @since 1.10.0
     */
    private boolean hostnameVerification = false;


    /** The maximum number of messages to read on a queue browser, which must be non-negative;
     *  0 means unlimited and is the default; negative values are interpreted as 0. */
    private int queueBrowserReadMax = Math.max(0, Integer.getInteger("rabbit.jms.queueBrowserReadMax", 0));

    /** The time to wait for threads/messages to terminate during {@link Connection#close()} */
    private volatile long terminationTimeout = Long.getLong("rabbit.jms.terminationTimeout", 15000);

    /**
     * QoS setting for channels created by this connection factory.
     *
     * @see com.rabbitmq.client.Channel#basicQos(int)
     */
    private int channelsQos = RMQConnection.NO_CHANNEL_QOS;

    /**
     * Classes in these packages can be transferred via ObjectMessage.
     *
     * @see WhiteListObjectInputStream
     */
    private List<String> trustedPackages = WhiteListObjectInputStream.DEFAULT_TRUSTED_PACKAGES;

    /**
     * List of nodes URIs to connect to.
     *
     * @since 1.10.0
     */
    private List<URI> uris = Collections.EMPTY_LIST;

    /**
     * Whether <code>replyTo</code> destination for consumed messages should be declared.
     *
     * @since 1.11.0
     */
    private boolean declareReplyToDestination = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection createConnection() throws JMSException {
        return this.createConnection(username, password);
    }

    public Connection createConnection(List<Address> endpoints) throws JMSException {
        return this.createConnection(username, password, endpoints);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection createConnection(String username, String password) throws JMSException {
        if (this.uris == null || this.uris.isEmpty()) {
            return createConnection(username, password, cf -> cf.newConnection());
        } else {
            List<Address> addresses = this.uris.stream().map(uri -> {
                String host = uri.getHost();
                int port = uri.getPort();
                if (port == -1) {
                    port = isSsl() ? DEFAULT_RABBITMQ_SSL_PORT :
                        DEFAULT_RABBITMQ_PORT;
                }
                return new Address(host, port);
            }).collect(Collectors.toList());
            return createConnection(username, password, cf -> cf.newConnection(addresses));
        }
    }

    public Connection createConnection(String username, String password, List<Address> endpoints)
        throws JMSException {
        return createConnection(username, password, cf -> cf.newConnection(endpoints));
    }

    protected Connection createConnection(String username, String password, ConnectionCreator connectionCreator) throws JMSException {
        logger.trace("Creating a connection for username '{}', password 'xxxxxxxx'.", username);
        this.username = username;
        this.password = password;
        com.rabbitmq.client.ConnectionFactory cf = createConnectionFactory();
        maybeEnableTLS(cf);
        setRabbitUri(logger, this, cf, getUri());
        maybeEnableHostnameVerification(cf);
        cf.setMetricsCollector(this.metricsCollector);

        if (this.amqpConnectionFactoryPostProcessor != null) {
            this.amqpConnectionFactoryPostProcessor.accept(cf);
        }

        com.rabbitmq.client.Connection rabbitConnection = instantiateNodeConnection(cf, connectionCreator);

        ReceivingContextConsumer rcc;
        if (this.declareReplyToDestination) {
            rcc = this.receivingContextConsumer;
        } else {
            rcc = ctx -> RMQMessage.doNotDeclareReplyToDestination(ctx.getMessage());
            if (this.receivingContextConsumer != null) {
                rcc = rcc.andThen(this.receivingContextConsumer);
            }
        }

        RMQConnection conn = new RMQConnection(new ConnectionParams()
            .setRabbitConnection(rabbitConnection)
            .setTerminationTimeout(getTerminationTimeout())
            .setQueueBrowserReadMax(getQueueBrowserReadMax())
            .setOnMessageTimeoutMs(getOnMessageTimeoutMs())
            .setChannelsQos(channelsQos)
            .setPreferProducerMessageProperty(preferProducerMessageProperty)
            .setRequeueOnMessageListenerException(requeueOnMessageListenerException)
            .setCommitBackOnRollback(commitNackOnRollback)
            .setCleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose(this.cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose)
            .setAmqpPropertiesCustomiser(amqpPropertiesCustomiser)
            .setSendingContextConsumer(sendingContextConsumer)
            .setReceivingContextConsumer(rcc)
            .setConfirmListener(confirmListener)
        );
        conn.setTrustedPackages(this.trustedPackages);
        logger.debug("Connection {} created.", conn);
        return conn;
    }

    protected com.rabbitmq.client.ConnectionFactory createConnectionFactory() {
        return new com.rabbitmq.client.ConnectionFactory();
    }

    private com.rabbitmq.client.Connection instantiateNodeConnection(com.rabbitmq.client.ConnectionFactory cf, ConnectionCreator connectionCreator)
        throws JMSException {
        try {
            return connectionCreator.create(cf);
        } catch (SSLException ssle) {
            throw new RMQJMSSecurityException("SSL Exception establishing RabbitMQ Connection", ssle);
        } catch (Exception x) {
            if (x instanceof IOException) {
                IOException ioe = (IOException) x;
                String msg = ioe.getMessage();
                if (msg!=null) {
                    if (msg.contains("authentication failure") || msg.contains("refused using authentication"))
                        throw new RMQJMSSecurityException(ioe);
                    else if (msg.contains("Connection refused"))
                        throw new RMQJMSException("RabbitMQ connection was refused. RabbitMQ broker may not be available.", ioe);
                }
                throw new RMQJMSException(ioe);
            } else if (x instanceof TimeoutException) {
                TimeoutException te = (TimeoutException) x;
                throw new RMQJMSException("Timed out establishing RabbitMQ Connection", te);
            } else {
                throw new RMQJMSException("Unexpected exception thrown by newConnection()", x);
            }
        }
    }

    /**
     * Returns the current factory connection parameters in a URI String.
     * @return URI for RabbitMQ connection (as a coded String)
     */
    public String getUri() {
        StringBuilder sb = new StringBuilder(scheme(isSsl())).append("://");
        sb.append(uriUInfoEscape(this.username, this.password)).append('@');
        sb.append(uriHostEscape(this.host)).append(':').append(this.getPort()).append("/");
        sb.append(uriVirtualHostEscape(this.virtualHost));

        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RMQConnectionFactory{");
        return (this.isSsl() ? sb.append("SSL, ") : sb)
        .append("user='").append(this.username)
        .append("', password").append(this.password!=null ? "=xxxxxxxx" : " not set")
        .append(", host='").append(this.host)
        .append("', port=").append(this.getPort())
        .append(", virtualHost='").append(this.virtualHost)
        .append("', onMessageTimeoutMs=").append(this.onMessageTimeoutMs)
        .append(", queueBrowserReadMax=").append(this.queueBrowserReadMax)
        .append('}').toString();
    }

    /**
     * Set connection factory parameters by URI String.
     * @param uriString URI to use for instantiated connection
     * @throws JMSException if connection URI is invalid
     */
    public void setUri(String uriString) throws JMSException {
        logger.trace("Set connection factory parameters by URI '{}'", uriString);
        if (uriString != null && !uriString.trim().isEmpty()) {
            // Create a temp factory and set the properties by uri
            com.rabbitmq.client.ConnectionFactory factory = createConnectionFactory();
            // Generates a TrustEverythingTrustManager warning which can be ignored as the SSLContext is not copied over
            setRabbitUri(logger, this, factory, uriString);
            // Now extract our properties from this factory, leaving the rest unchanged.
            this.host = factory.getHost();
            this.password = factory.getPassword();
            this.port = factory.getPort();
            this.ssl = factory.isSSL();
            this.username = factory.getUsername();
            this.virtualHost = factory.getVirtualHost();
        } else {
            this.host = null;
            this.password = null;
            this.port = -1;
            this.ssl = false;
            this.username = null;
            this.virtualHost = null;
        }
    }

    /**
     * Sets the nodes URIs to connect to.
     *
     * @param urisAsStrings
     * @throws JMSException
     * @since 1.10.0
     */
    public void setUris(List<String> urisAsStrings) throws JMSException {
        if (urisAsStrings != null && !urisAsStrings.isEmpty()) {
            this.uris = urisAsStrings.stream().map(uriAsString -> {
                try {
                    URI uri = new URI(uriAsString);
                    if (uri.getScheme() == null || (!"amqp".equals(uri.getScheme()) && !"amqps".equals(uri.getScheme()))) {
                        throw new IllegalArgumentException("Wrong scheme in AMQP URI: " + uri.getScheme());
                    }
                    return uri;
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Invalid URI: " + uriAsString);
                }
            }).collect(Collectors.toList());
            this.setUri(urisAsStrings.get(0));
        } else {
            this.uris = Collections.EMPTY_LIST;
            setUri(null);
        }
    }

    /**
     * @param value list of trusted package prefixes
     */
    public void setTrustedPackages(List<String> value) {
        this.trustedPackages = value;
    }

    /**
     * @return list of package prefixes that are whitelisted for transfer over {@link javax.jms.ObjectMessage}
     */
    public List<String> getTrustedPackages() {
        return trustedPackages;
    }

    private static void setRabbitUri(Logger logger, RMQConnectionFactory rmqFactory, com.rabbitmq.client.ConnectionFactory factory, String uriString) throws RMQJMSException {
        if (uriString != null) { // we get the defaults if the uri is null
            try {
                factory.setUri(uriString);
            } catch (Exception e) {
                logger.error("Could not set URI on {}", rmqFactory, e);
                throw new RMQJMSException("Could not set URI on RabbitMQ connection factory.", e);
            }
        }
    }

    private void maybeEnableTLS(com.rabbitmq.client.ConnectionFactory factory) {
        if (this.ssl)
            try {
                if(this.useDefaultSslContext) {
                    factory.useSslProtocol(SSLContext.getDefault());
                } else {
                    if (this.sslContext != null) {
                        factory.useSslProtocol(this.sslContext);
                    } else if (this.tlsProtocol != null) {
                        factory.useSslProtocol(this.tlsProtocol);
                    } else {
                        factory.useSslProtocol();
                    }
                }
            } catch (Exception e) {
                this.logger.warn("Could not set SSL protocol on connection factory, {}. SSL set off.", this, e);
                this.ssl = false;
            }
    }

    private void maybeEnableHostnameVerification(com.rabbitmq.client.ConnectionFactory factory) {
        if (hostnameVerification) {
            if (this.ssl) {
                factory.enableHostnameVerification();
            } else {
                logger.warn("Hostname verification enabled, but not TLS, please enable TLS too.");
            }
        }
    }

    public boolean isSsl() {
        return this.ssl;
    }

    /**
     * @deprecated Use {@link #useSslProtocol()}, {@link #useSslProtocol(String)}
     *             or {@link #useSslProtocol(SSLContext)}.
     * @param ssl if true, enables TLS for connections opened
     */
    @Deprecated
    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    /**
     * Enables TLS on opened connections with the highest TLS
     * version available.
     * @throws NoSuchAlgorithmException see {@link NoSuchAlgorithmException}
     */
    public void useSslProtocol() throws NoSuchAlgorithmException {
        this.useSslProtocol(
            com.rabbitmq.client.ConnectionFactory.computeDefaultTlsProtocol(
                SSLContext.getDefault().getSupportedSSLParameters().getProtocols()));
    }

    /**
     * Enables TLS on opened connections using the provided TLS protocol
     * version.
     * @param protocol TLS or SSL protocol version.
     * @see <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/SunProviders.html#SunJSSEProvider">JDK documentation on protocol names</a>
     */
    public void useSslProtocol(String protocol)
    {
        this.tlsProtocol = protocol;
        this.ssl = true;
    }

    /**
     * Enables TLS on opened connections using the provided {@link SSLContext}.
     * @param context {@link SSLContext} to use
     */
    public void useSslProtocol(SSLContext context) {
        this.sslContext = context;
        this.ssl = true;
    }

    /**
     * Whether to use the default {@link SSLContext} or not.
     * Default is false.
     *
     * When this option is enabled, the default {@link SSLContext}
     * will always be used and will override any other {@link SSLContext}
     * set.
     *
     * @param useDefaultSslContext
     * @see SSLContext#getDefault()
     */
    public void useDefaultSslContext(boolean useDefaultSslContext) {
        this.useDefaultSslContext = useDefaultSslContext;
        this.ssl = true;
    }

    /**
     * Whether to use the default {@link SSLContext} or not.
     *
     * @see SSLContext#getDefault()
     */
    public boolean isUseDefaultSslContext() {
        return useDefaultSslContext;
    }

    /**
     * Whether to use the default {@link SSLContext} or not.
     * Default is false.
     *
     * When this option is enabled, the default {@link SSLContext}
     * will always be used and will override any other {@link SSLContext}
     * set.
     *
     * @param useDefaultSslContext
     * @see SSLContext#getDefault()
     */
    public void setUseDefaultSslContext(boolean useDefaultSslContext) {
        this.useDefaultSslContext(useDefaultSslContext);
    }

    private static String scheme(boolean isSsl) {
        return (isSsl ? "amqps" : "amqp");
    }

    private static String uriUInfoEscape(String user, String pass) {
        if (null == user) return null;
        if (null == pass) return encUserinfo(user, "UTF-8");
        return encUserinfo(user + ":" + pass, "UTF-8");
    }

    private static String uriHostEscape(String host) {
        return encHost(host, "UTF-8");
    }

    private static String uriVirtualHostEscape(String vHost) {
        return encSegment(vHost, "UTF-8");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reference getReference() throws NamingException {
        Reference ref = new Reference(RMQConnectionFactory.class.getName());
        addStringRefProperty(ref, "uri", this.getUri());
        addIntegerRefProperty(ref, "queueBrowserReadMax", this.getQueueBrowserReadMax());
        addIntegerRefProperty(ref, "onMessageTimeoutMs", this.getOnMessageTimeoutMs());
        return ref;
    }

    /**
     * Adds a String valued property to a Reference (as a RefAddr)
     * @param ref - the reference to contain the value
     * @param propertyName - the name of the property
     * @param value - the value to store with the property
     */
    private static void addStringRefProperty(Reference ref,
                                             String propertyName,
                                             String value) {
        if (value==null || propertyName==null) return;
        RefAddr ra = new StringRefAddr(propertyName, value);
        ref.add(ra);
    }

    /**
     * Adds an integer valued property to a Reference (as a RefAddr).
     * @param ref - the reference to contain the value
     * @param propertyName - the name of the property
     * @param value - the value to store with the property
     */
    private static void addIntegerRefProperty(Reference ref,
                                              String propertyName,
                                              Integer value) {
        if (value == null || propertyName == null) return;
        RefAddr ra = new StringRefAddr(propertyName, String.valueOf(value));
        ref.add(ra);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicConnection createTopicConnection() throws JMSException {
        return (TopicConnection) this.createConnection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicConnection createTopicConnection(String userName, String password) throws JMSException {
        return (TopicConnection) this.createConnection(userName, password);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueConnection createQueueConnection() throws JMSException {
        return (QueueConnection) this.createConnection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueConnection createQueueConnection(String userName, String password) throws JMSException {
        return (QueueConnection) this.createConnection(userName, password);
    }

    /**
     * Returns the configured username used when creating a connection If
     * {@link RMQConnectionFactory#setUsername(String)} has not been called the default value of 'guest' is returned.
     *
     * @return a string representing the username for a RabbitMQ connection
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the <i>username</i> to be used when creating a connection to the RabbitMQ broker.
     * If the parameter is <code>null</code> the current <i>username</i> is not changed.
     *
     * @param username - username to be used when creating a connection to the RabbitMQ broker
     */
    public void setUsername(String username) {
        if (username != null) this.username = username;
        else this.logger.warn("Cannot set username to null (on {})", this);
    }

    /**
     * Returns the configured password used when creating a connection If
     * {@link RMQConnectionFactory#setPassword(String)} has not been called the default value of 'guest' is returned.
     *
     * @return a string representing the password for a Rabbit connection
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the <i>password</i> to be used when creating a connection to the RabbitMQ broker
     *
     * @param password - password to be used when creating a connection to the RabbitMQ broker
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the virtual host used when creating a connection.  If
     * {@link RMQConnectionFactory#setVirtualHost(String)} has not been called the default value of '/' is returned.
     *
     * @return a String representing the virtual host for a RabbitMQ connection
     */
    public String getVirtualHost() {
        return virtualHost;
    }

    /**
     * Sets the <i>virtualHost</i> to be used when creating a connection to the RabbitMQ broker.
     * If the parameter is <code>null</code> the current <i>virtualHost</i> is not changed.
     *
     * @param virtualHost - virtual host to be used when creating a connection to the RabbitMQ broker
     */
    public void setVirtualHost(String virtualHost) {
        if (virtualHost != null) this.virtualHost = virtualHost;
        else this.logger.warn("Cannot set virtualHost to null (on {})", this);
    }

    /**
     * Returns the host name to be used when creating a connection to the RabbitMQ broker.
     *
     * @return the host name of the RabbitMQ broker
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the <i>host</i> of the RabbitMQ broker. The host name can be an IP address or a host name.
     * If the parameter is <code>null</code> the current <i>host name</i> is not changed.
     *
     * @param host - IP address or a host name of the RabbitMQ broker, in String form
     */
    public void setHost(String host) {
        if (host != null) this.host = host;
        else this.logger.warn("Cannot set host to null (on {})", this);
    }

    /**
     * Returns the port the RabbitMQ broker listens to; this port is used to connect to the broker.
     * If the port has not been set (defaults to -1) then the default port for this type of connection is returned.
     *
     * @return the port the RabbitMQ broker listens to
     */
    public int getPort() {
        return this.port!=-1 ? this.port
             : isSsl() ? DEFAULT_RABBITMQ_SSL_PORT
             : DEFAULT_RABBITMQ_PORT;
    }

    /**
     * Set the <i>port</i> to be used when making a connection to the RabbitMQ broker.  This is the port number the broker will listen on.
     * Setting this to -1 means take the RabbitMQ default (which depends on the type of connection).
     *
     * @param port - a TCP port number
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns the time to wait in milliseconds when {@link Connection#close()} has been called for listeners and threads to
     * complete.
     *
     * @return the duration in milliseconds for which the {@link Connection#close()} waits before continuing shutdown sequence
     */
    public long getTerminationTimeout() {
        return terminationTimeout;
    }

    /**
     * Sets <i>terminationTimeout</i>: the time in milliseconds a {@link Connection#close()} should wait for threads/tasks/listeners to complete
     *
     * @param terminationTimeout - duration in milliseconds
     */
    public void setTerminationTimeout(long terminationTimeout) {
        this.terminationTimeout = terminationTimeout;
    }

    /**
     * Returns the maximum number of messages to read on a queue browser, or zero if there is no limit.
     *
     * @return the maximum number of messages to read on a queue browser
     */
    public int getQueueBrowserReadMax() {
        return this.queueBrowserReadMax;
    }

    /**
     * Sets <i>queueBrowserReadMax</i>: the maximum number of messages to read on a queue browser.
     * Non-positive values are set to zero, which is interpreted as no limit.
     *
     * @param queueBrowserReadMax - read no more than this number of messages on a queue browser.
     */
    public void setQueueBrowserReadMax(int queueBrowserReadMax) {
        this.queueBrowserReadMax = Math.max(0, queueBrowserReadMax);
    }

    /**
     * Returns the time in milliseconds {@link MessageListener#onMessage(Message)} can take to process a message
     * @return the time in milliseconds {@link MessageListener#onMessage(Message)} can take to process a message
     */
    public int getOnMessageTimeoutMs() { return this.onMessageTimeoutMs; }

    /**
     * Sets <i>onMessageTimeoutMs</i>: the time in milliseconds {@link MessageListener#onMessage(Message)} can take to process a message.
     * Non-positive values are rejected.
     * @param onMessageTimeoutMs - duration in milliseconds
     */
    public void setOnMessageTimeoutMs(int onMessageTimeoutMs){
        if (onMessageTimeoutMs > 0) this.onMessageTimeoutMs = onMessageTimeoutMs;
        else this.logger.warn("Cannot set onMessageTimeoutMs to non-positive value {} (on {})", onMessageTimeoutMs, this);
    }

    /**
     * QoS setting for channels created by this connection factory.
     *
     * @see com.rabbitmq.client.Channel#basicQos(int)
     */
    public int getChannelsQos() {
        return channelsQos;
    }

    /**
     * QoS setting for channels created by this connection factory.
     *
     * @see com.rabbitmq.client.Channel#basicQos(int)
     * @param channelsQos maximum number of messages that the server
     * will deliver, 0 if unlimited
     */
    public void setChannelsQos(int channelsQos) {
        this.channelsQos = channelsQos;
    }

    /**
     * Whether {@link MessageProducer} properties (delivery mode,
     * priority, TTL) take precedence over respective {@link Message}
     * properties or not.
     * Default is true (which is compliant to the JMS specification).
     */
    public void setPreferProducerMessageProperty(boolean preferProducerMessageProperty) {
        this.preferProducerMessageProperty = preferProducerMessageProperty;
    }

    public boolean isPreferProducerMessageProperty() {
        return preferProducerMessageProperty;
    }

    /**
     * Whether requeue message on {@link RuntimeException} in the
     * {@link javax.jms.MessageListener} or not.
     * Default is false.
     */
    public void setRequeueOnMessageListenerException(boolean requeueOnMessageListenerException) {
        this.requeueOnMessageListenerException = requeueOnMessageListenerException;
    }

    public boolean isRequeueOnMessageListenerException() {
        return requeueOnMessageListenerException;
    }

    /**
     * Whether to commit nack on rollback or not.
     * Default is false.
     */
    public void setCommitNackOnRollback(boolean commitNackOnRollback) {
        this.commitNackOnRollback = commitNackOnRollback;
    }

    public boolean isCommitNackOnRollback() {
        return commitNackOnRollback;
    }

    public void setCleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose(boolean cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose) {
        this.cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose = cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose;
    }

    public boolean isCleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose() {
        return this.cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose;
    }

    public void setAmqpPropertiesCustomiser(BiFunction<AMQP.BasicProperties.Builder, Message, AMQP.BasicProperties.Builder> amqpPropertiesCustomiser) {
        this.amqpPropertiesCustomiser = amqpPropertiesCustomiser;
    }

    /**
     * Enable or disable hostname verification when TLS is used.
     *
     * @param hostnameVerification
     * @see com.rabbitmq.client.ConnectionFactory#enableHostnameVerification()
     * @since 1.10.0
     */
    public void setHostnameVerification(boolean hostnameVerification) {
        this.hostnameVerification = hostnameVerification;
    }

    /**
     * Set the collector for AMQP-client metrics.
     *
     * @param metricsCollector
     * @since 1.10.0
     * @see com.rabbitmq.client.ConnectionFactory#setMetricsCollector(MetricsCollector)
     */
    public void setMetricsCollector(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }
    
    public List<String> getUris() {
        return this.uris.stream().map(uri -> uri.toString()).collect(Collectors.toList());
    }

    /**
     * Set a post-processor for the AMQP {@link com.rabbitmq.client.ConnectionFactory}.
     * <p>
     * The post-processor is called before the AMQP creation. This callback can be
     * useful to customize the {@link com.rabbitmq.client.ConnectionFactory}:
     * TLS-related configuration, metrics collection, etc.
     *
     * @param amqpConnectionFactoryPostProcessor
     * @since 1.10.0
     */
    public void setAmqpConnectionFactoryPostProcessor(Consumer<com.rabbitmq.client.ConnectionFactory> amqpConnectionFactoryPostProcessor) {
        this.amqpConnectionFactoryPostProcessor = amqpConnectionFactoryPostProcessor;
    }

    /**
     * Set callback called before sending a message.
     * Can be used to customize the message or the destination
     * before the message is actually sent.
     *
     * @see SendingContextConsumer
     * @see com.rabbitmq.jms.client.SendingContext
     * @since 1.11.0
     */
    public void setSendingContextConsumer(SendingContextConsumer sendingContextConsumer) {
        this.sendingContextConsumer = sendingContextConsumer;
    }

    /**
     * Set callback called before dispatching a received message to application code.
     * Can be used to customize messages before they handed
     * over application.
     *
     * @param receivingContextConsumer
     * @see ReceivingContextConsumer
     * @since 1.11.0
     */
    public void setReceivingContextConsumer(ReceivingContextConsumer receivingContextConsumer) {
        this.receivingContextConsumer = receivingContextConsumer;
    }

    /**
     * Whether <code>replyTo</code> destination for consumed messages should be declared.
     * <p>
     * Default is <code>true</code>. Set this value to <code>false</code> for the
     * <b>server-side of RPC</b>, this avoids creating a temporary reply-to destination on
     * both client and server, leading to an error.
     * <p>
     * This is implemented as {@link ReceivingContextConsumer}.
     *
     * @see RMQConnectionFactory#setReceivingContextConsumer(ReceivingContextConsumer)
     * @since 1.11.0
     */
    public void setDeclareReplyToDestination(boolean declareReplyToDestination) {
        this.declareReplyToDestination = declareReplyToDestination;
    }

    /**
     * Set the callback to be notified of publisher confirms.
     * <p>
     * When this property is set, publisher confirms are enabled for all
     * the underlying AMQP {@link com.rabbitmq.client.Channel}s created by
     * this {@link ConnectionFactory}.
     *
     * @param confirmListener the callback
     * @see <a href="https://www.rabbitmq.com/confirms.html#publisher-confirms">Publisher Confirms</a>
     * @see <a href="https://www.rabbitmq.com/publishers.html#data-safety">Publisher Guide</a>
     * @see ConfirmListener
     * @since 1.13.0
     */
    public void setConfirmListener(ConfirmListener confirmListener) {
        this.confirmListener = confirmListener;
    }

    @FunctionalInterface
    private interface ConnectionCreator {
        com.rabbitmq.client.Connection create(com.rabbitmq.client.ConnectionFactory cf) throws Exception;
    }

    private static final class NoOpMetricsCollector implements MetricsCollector, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public void newConnection(com.rabbitmq.client.Connection connection) {

        }

        @Override
        public void closeConnection(com.rabbitmq.client.Connection connection) {

        }

        @Override
        public void newChannel(Channel channel) {

        }

        @Override
        public void closeChannel(Channel channel) {

        }

        @Override
        public void basicPublish(Channel channel) {

        }

        @Override
        public void consumedMessage(Channel channel, long deliveryTag, boolean autoAck) {

        }

        @Override
        public void consumedMessage(Channel channel, long deliveryTag, String consumerTag) {

        }

        @Override
        public void basicAck(Channel channel, long deliveryTag, boolean multiple) {

        }

        @Override
        public void basicNack(Channel channel, long deliveryTag) {

        }

        @Override
        public void basicReject(Channel channel, long deliveryTag) {

        }

        @Override
        public void basicConsume(Channel channel, String consumerTag, boolean autoAck) {

        }

        @Override
        public void basicCancel(Channel channel, String consumerTag) {

        }
    }

    private static final class NoOpSerializableConsumer<T> implements Consumer<T>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public void accept(T t) { }
    }

    private static final class NoOpSerializableSendingContextConsumer implements SendingContextConsumer, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public void accept(SendingContext sendingContext) { }
    }

    private static final class NoOpSerializableReceivingContextConsumer implements ReceivingContextConsumer, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public void accept(ReceivingContext receivingContext) { }
    }
}

