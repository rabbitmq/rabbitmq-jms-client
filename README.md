[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.rabbitmq.jms/rabbitmq-jms/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.rabbitmq.jms/rabbitmq-jms)

# Java JMS Client for RabbitMQ

## Overview

This is a JMS 1.1 client library for RabbitMQ, working in concert with [rabbitmq-jms-topic-exchange](https://github.com/rabbitmq/rabbitmq-jms-topic-exchange),
a RabbitMQ server plugin.

## Installation

### With Maven

This package is published to several Maven package repositories:

 * [oss.sonatype.org](https://oss.sonatype.org/#nexus-search;quick~rabbitmq-jms)
 * [repo.spring.io](https://repo.spring.io/libs-release-local/com/rabbitmq/jms/rabbitmq-jms/)
 * Maven Central (via eventual promotion from the first one)

Add the following dependency to `pom.xml`:

``` xml
<dependency>
  <groupId>com.rabbitmq.jms</groupId>
  <artifactId>rabbitmq-jms</artifactId>
  <version>1.5.0</version>
</dependency>
```

### Building from Source

This project is managed by Maven, so use

    mvn clean install

to build it from source and install into the local repository.


## Running Tests

See [CONTRIBUTING.md](./CONTRIBUTING.md) for an overview of the development process.

### Unit Tests

    mvn clean test

### Integration Tests

The integration tests assume a RabbitMQ node 
with [rabbitmq-jms-topic-exchange](https://github.com/rabbitmq/rabbitmq-jms-topic-exchange/)
listening on localhost:5672 (the default settings).

Connection recovery tests need `rabbitmqctl` to control the running node.

Maven will start this node with the appropriate configuration by default when
launching the `verify` command:

    mvn clean verify

You can also provide your own broker node. To disable the
automatic test node setup, disable the `setup-test-node` Maven
profile:

    mvn clean verify -P '!setup-test-node'

The easiest way to run a test node is to clone
[rabbitmq-jms-topic-exchange](https://github.com/rabbitmq/rabbitmq-jms-topic-exchange/) and use `make run-broker`.


## License and Copyright

(c) Pivotal Software, Inc., 2007-2016.

This package, the RabbitMQ JMS client library, is double-licensed
under the Apache License version 2 ("ASL") and the Mozilla Public License
1.1 ("MPL").

See [LICENSE](./LICENSE).
