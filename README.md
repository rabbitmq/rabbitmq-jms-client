# Java JMS Client for RabbitMQ

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.rabbitmq.jms/rabbitmq-jms/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.rabbitmq.jms/rabbitmq-jms)
[![Travis CI](https://travis-ci.org/rabbitmq/rabbitmq-jms-client.svg?branch=master)](https://travis-ci.org/rabbitmq/rabbitmq-jms-client)

## Overview

This is a JMS 1.1 client library for RabbitMQ, working in concert with [rabbitmq-jms-topic-exchange](https://github.com/rabbitmq/rabbitmq-jms-topic-exchange),
a RabbitMQ server plugin.

## Installation

### With Maven or Gradle

This package is published to several Maven package repositories:

 * [Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.rabbitmq.jms%22%20AND%20a%3A%22rabbitmq-jms%22)
 * [RabbitMQ Maven Release repository](https://bintray.com/rabbitmq/maven) on Bintray
 * [RabbitMQ Maven Milestones repository](https://bintray.com/rabbitmq/maven-milestones) on Bintray
 * [Sonatype OSS snapshot repository](https://oss.sonatype.org/content/repositories/snapshots/com/rabbitmq/jms/rabbitmq-jms/) for snapshots
 

Add the following to `pom.xml` for Maven:

``` xml
<dependency>
  <groupId>com.rabbitmq.jms</groupId>
  <artifactId>rabbitmq-jms</artifactId>
  <version>1.14.0</version>
</dependency>
```

Or the following to `build.gradle` for Gradle:

```
compile 'com.rabbitmq.jms:rabbitmq-jms:1.14.0'
```

### Building from Source

This project is managed by Maven, so use

    ./mvnw clean install

to build it from source and install into the local repository.


## Running Tests

See [CONTRIBUTING.md](./CONTRIBUTING.md) for an overview of the development process.

### Unit Tests

    ./mvnw clean test

### Integration Tests

The integration tests assume a RabbitMQ node 
with [rabbitmq-jms-topic-exchange](https://github.com/rabbitmq/rabbitmq-jms-topic-exchange/)
listening on localhost:5672 (the default settings).

Connection recovery tests need `rabbitmqctl` to control the running node.

Maven will start this node with the appropriate configuration by default when
launching the `verify` command:

    ./mvnw clean verify

You can also provide your own broker node. To disable the
automatic test node setup, disable the `setup-test-node` Maven
profile:

    ./mvn clean verify -P '!setup-test-node'

The easiest way to run a test node is to clone
[rabbitmq-jms-topic-exchange](https://github.com/rabbitmq/rabbitmq-jms-topic-exchange/) and use `make run-broker`.

### JMS 1.1 Compliance Test Suite

[JMS 1.1 compliance test suite](https://github.com/rabbitmq/rabbitmq-jms-cts) for this client is available
in a separate repository.

## Versioning

This library uses [semantic versioning](https://semver.org/).

## Support

See the [RabbitMQ Java libraries support page](https://www.rabbitmq.com/java-versions.html)
for the support timeline of this library.

## License and Copyright

(c) Pivotal Software, Inc., 2007-2020.

This package, the RabbitMQ JMS client library, is double-licensed
under the Apache License version 2 ("ASL") and the Mozilla Public License
1.1 ("MPL").

See [LICENSE](./LICENSE).
