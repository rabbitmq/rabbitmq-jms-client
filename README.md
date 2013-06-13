## Java JMS Client based on RabbitMQ

(#1.0.1-PATCH-2013-06-13)

### Overview

This Java code (forming a single `jar`) is the heart of the RabbitMQ JMS Client.  It is a Maven project, with parent `rabbitmq-jms-parent` whose source is in the repository named `rabbit-jms-parent`.

The complete list of RabbitMQ JMS project repositories is:

* [rabbit-jms-client](#rabbit-jms-client) this repository, a `jar` project
* [rabbit-jms-parent](#rabbit-jms-parent) the common parent `pom` project with dependency information
* [rabbit-jms-cts](#rabbit-jms-cts) a JMS integration test suite, a `jar` project
* [rabbit-jms-package](#rabbit-jms-package) a `pom` project that builds snapshots and releases
* [rabbit-jms-topic-exchange](#rabbit-jms-topic-exchange) a specialised `pom` project that builds a RabbitMQ plugin
* [rabbit-jms-trader](#rabbit-jms-trader) a specialised `pom` project that builds a sample application using the RabbitMQ JMS client.

### rabbit-jms-client <a id="rabbit-jms-client"></a>

This repository. The jar is built, and installed into the local Maven repository, using

    mvn clean install

as usual, but requires access to pre-built `rabbit-jms-parent` and `rabbit-jms-topic-exchange` projects at the same version level. It requires special access permissions to `deploy`.

### rabbit-jms-parent <a id="rabbit-jms-parent"></a>

The holder of the master version, the (Maven) plugin and package dependencies and their versions, and certain globally used property settings. It can be built and deployed locally for private use (using `install`).

### rabbit-jms-cts <a id="rabbit-jms-cts"></a>

A fork of a compliance test suite, adjusted to test this client to its functional limits.

### rabbit-jms-package <a id="rabbit-jms-package"></a>

The master distribution build project — this knows what pieces and versions of trader, the Plugin and the client jar to build and package for distribution.

### rabbit-jms-topic-exchange <a id="rabbit-jms-topic-exchange"></a>

This is an Erlang coded RabbitMQ plugin which defines a new exchange type for RabbitMQ Server. Exchanges of this type can piggy-back onto topic exchanges (by exchange to exchange binding) to filter out messages based upon their properties and a selector expression (written in a JMS SQL syntax).

### rabbit-jms-trader <a id="rabbit-jms-trader"></a>

A RabbitMQ JMS Client application, completely self-contained.