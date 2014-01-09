## Java JMS Client based on RabbitMQ

### Overview

This Java code (forming a single `jar`) is the heart of the RabbitMQ JMS Client.  It is a Maven project, with parent `rabbitmq-jms-parent` whose source is in the repository named `rabbit-jms-parent`.

The complete list of RabbitMQ JMS project repositories is:

* [`rabbit-jms-client`](#rabbit-jms-client) this repository, a `jar` project
* [`rabbit-jms-parent`](#rabbit-jms-parent) the common parent `pom` project with dependency information
* [`rabbit-jms-cts`](#rabbit-jms-cts) a JMS integration test suite, a `jar` project
* [`rabbit-jms-package`](#rabbit-jms-package) a `pom` project that builds snapshots and releases
* [`rabbit-jms-topic-exchange`](#rabbit-jms-topic-exchange) a specialised `pom` project that builds a RabbitMQ plugin
* [`rabbit-jms-trader`](#rabbit-jms-trader) a specialised `pom` project that builds a sample application using the RabbitMQ JMS client.
* [`rabbit-jms-boot-demo`](#rabbit-jms-boot-demo) a very small sample app using Spring, with Groovy and Java embodiments.

### <a name="rabbit-jms-client"></a> `rabbit-jms-client`

This repository. The jar is built, and installed into the local Maven repository, using

    mvn clean install

as usual, but requires access to pre-built `rabbit-jms-parent` and `rabbit-jms-topic-exchange` projects at the same version level. It requires special access permissions to `deploy`.

### <a name="rabbit-jms-parent"></a> `rabbit-jms-parent`

The holder of the master version, the (Maven) plugin and package dependencies and their versions, and certain globally used property settings. It can be built and deployed locally for private use (using `install`).

### <a name="rabbit-jms-cts"></a> `rabbit-jms-cts`

A fork of a compliance test suite, adjusted to test this client to its functional limits.

### <a name="rabbit-jms-package"></a> `rabbit-jms-package`

The master distribution build project — this knows what pieces and versions of samples, the Plugin and the client jar to build and package for distribution.

### <a name="rabbit-jms-topic-exchange"></a> `rabbit-jms-topic-exchange`

This is an Erlang coded RabbitMQ plugin (_the Plugin_) which defines a new exchange type for RabbitMQ Server. Exchanges of this type can piggy-back onto topic exchanges (by exchange to exchange binding) to filter out messages based upon their properties and a selector expression (written in a JMS SQL syntax).

### <a name="rabbit-jms-trader"></a> `rabbit-jms-trader`

A sample RabbitMQ JMS Client application, completely self-contained. Built with play and comes with a tomcat container for demonstration purposes.

### <a name="rabbit-jms-boot-demo"></a> `rabbit-jms-boot-demo`

A sample very small RabbitMQ JMS Client application, using Spring and with both `groovy` and Java stand-alone invocations. Suitable for development trial.
This repository is not distributed.
