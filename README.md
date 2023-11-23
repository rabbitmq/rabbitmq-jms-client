# Java JMS Client for RabbitMQ

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.rabbitmq.jms/rabbitmq-jms/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.rabbitmq.jms/rabbitmq-jms)
![Build Status](https://github.com/rabbitmq/rabbitmq-jms-client/workflows/Build%20(Linux)/badge.svg?branch=main)

## Overview

This is a JMS 1.1 client library for RabbitMQ, working in concert with [rabbitmq-jms-topic-exchange](https://github.com/rabbitmq/rabbitmq-server/tree/master/deps/rabbitmq_jms_topic_exchange),
a RabbitMQ server plugin.

[Documentation](https://rabbitmq.com/jms-client.html)

## Installation

### With Maven or Gradle

This package is published to several Maven package repositories:

 * [Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.rabbitmq.jms%22%20AND%20a%3A%22rabbitmq-jms%22)
 * [RabbitMQ Maven Milestones repository](https://packagecloud.io/rabbitmq/maven-milestones)
 * [Sonatype OSS snapshot repository](https://oss.sonatype.org/content/repositories/snapshots/com/rabbitmq/jms/rabbitmq-jms/)

#### Latest Stable

Add the following to `pom.xml` for Maven:

```xml
<dependency>
  <groupId>com.rabbitmq.jms</groupId>
  <artifactId>rabbitmq-jms</artifactId>
  <version>2.6.0</version>
</dependency>
```

Or the following to `build.gradle` for Gradle:

```groovy
compile 'com.rabbitmq.jms:rabbitmq-jms:2.6.0'
```

#### Snapshot

Add the following to `pom.xml` for Maven:

```xml
<dependency>
  <groupId>com.rabbitmq.jms</groupId>
  <artifactId>rabbitmq-jms</artifactId>
  <version>2.7.0-SNAPSHOT</version>
</dependency>
```

You need to declare the snapshot repository as well:

```xml
<repositories>

  <repository>
    <id>ossrh</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <snapshots><enabled>true</enabled></snapshots>
    <releases><enabled>false</enabled></releases>
  </repository>

</repositories>
```

Or the following to `build.gradle` for Gradle:

```groovy
compile 'com.rabbitmq.jms:rabbitmq-jms:2.7.0-SNAPSHOT'
```

You need to declare the snapshot repository as well:

```groovy
repositories {
  maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
  mavenCentral()
}
```

### Building from Source

This project is managed by Maven, so use

    ./mvnw clean install -Dmaven.test.skip=true

to build it from source and install into the local repository.


## Running Tests

See [CONTRIBUTING.md](./CONTRIBUTING.md) for an overview of the development process.

### Unit Tests

    ./mvnw clean test

### Integration Tests

#### Running Integration Tests with Docker

Launch the broker:

    docker run -it --rm --name rabbitmq -p 5672:5672 rabbitmq

Enable the JMS Topic Exchange plugin:

    docker exec rabbitmq rabbitmq-plugins enable rabbitmq_jms_topic_exchange

Launch the tests:

    ./mvnw verify -Drabbitmqctl.bin=DOCKER:rabbitmq

#### Running Integration Tests with a Local Broker

To launch the test suite (requires a local RabbitMQ node with JMS Topic Exchange plugin enabled):

    ./mvnw verify -Drabbitmqctl.bin=/path/to/rabbitmqctl

### JMS 1.1 Compliance Test Suite

[JMS 1.1 compliance test suite](https://github.com/rabbitmq/rabbitmq-jms-cts) for this client is available
in a separate repository.

## Versioning

This library uses [semantic versioning](https://semver.org/).

## Support

See the [RabbitMQ Java libraries support page](https://www.rabbitmq.com/java-versions.html)
for the support timeline of this library.

## License and Copyright

(c) 2007-2023 Broadcom. All Rights Reserved.
The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

This package, the RabbitMQ JMS client library, is double-licensed
under the Apache License version 2 ("ASL") and the Mozilla Public License
2.0 ("MPL").

See [LICENSE](./LICENSE).
