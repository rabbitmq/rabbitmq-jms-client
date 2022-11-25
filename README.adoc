:2-stable: 2.7.0
:2-milestone: 2.8.0.RC1
:2-snapshot: 2.8.0-SNAPSHOT
:3-stable: 3.0.0
:3-milestone: 3.1.0.RC1
:3-snapshot: 3.1.0-SNAPSHOT

= Java JMS Client for RabbitMQ

image:https://maven-badges.herokuapp.com/maven-central/com.rabbitmq.jms/rabbitmq-jms/badge.svg["Maven Central", link="https://maven-badges.herokuapp.com/maven-central/com.rabbitmq.jms/rabbitmq-jms"]
image:https://github.com/rabbitmq/rabbitmq-jms-client/workflows/Build%20(Linux)/badge.svg?branch=2.x.x-stable["Build Status", link="https://github.com/rabbitmq/rabbitmq-jms-client/actions?query=workflow%3A%22Build+%28Linux%29%22+branch%3A2.x.x-stable"] (2.x)
image:https://github.com/rabbitmq/rabbitmq-jms-client/workflows/Build%20(Linux)/badge.svg?branch=main["Build Status", link="https://github.com/rabbitmq/rabbitmq-jms-client/actions?query=workflow%3A%22Build+%28Linux%29%22+branch%3Amain"] (3.x)

== Overview

This is a JMS client library for RabbitMQ, working in concert with https://github.com/rabbitmq/rabbitmq-server/tree/main/deps/rabbitmq_jms_topic_exchange[rabbitmq-jms-topic-exchange],
a RabbitMQ server plugin.
It supports JMS 2.0 as of <<jms-2, 2.7.0>> and JMS 3.0 as of <<jms-3, 3.0.0>>.
Both 2.x and 3.x branches are maintained and supported.

https://rabbitmq.com/jms-client.html[Documentation]

== Installation

=== With Maven or Gradle

This package is published to several Maven package repositories:

* https://search.maven.org/search?q=g:com.rabbitmq.jms%20AND%20a:rabbitmq-jms[Maven Central]
* https://packagecloud.io/rabbitmq/maven-milestones[RabbitMQ Maven Milestones repository]
* https://oss.sonatype.org/content/repositories/snapshots/com/rabbitmq/jms/rabbitmq-jms/[Sonatype OSS snapshot repository]

[[jms-2]]
=== JMS 1.1 and 2.0

RabbitMQ JMS Client implements JMS 2.0 as of 2.7.0.

==== Stable

Add the following to `pom.xml` for Maven:

.pom.xml
[source,xml,subs="attributes,specialcharacters"]
----
<dependency>
  <groupId>com.rabbitmq.jms</groupId>
  <artifactId>rabbitmq-jms</artifactId>
  <version>{2-stable}</version>
</dependency>
----

Or the following to `build.gradle` for Gradle:

.build.gradle
[source,groovy,subs="attributes,specialcharacters"]
----
compile 'com.rabbitmq.jms:rabbitmq-jms:{2-stable}'
----

////
==== Milestone and Release Candidate

For milestones and release candidates, declare the <<milestone-rc-repository,milestone repository>> in your dependency manager.

Then add the following to `pom.xml` for Maven:

.pom.xml
[source,xml,subs="attributes,specialcharacters"]
----
<dependency>
  <groupId>com.rabbitmq.jms</groupId>
  <artifactId>rabbitmq-jms</artifactId>
  <version>{2-milestone}</version>
</dependency>
----

Or the following to `build.gradle` for Gradle:

.build.gradle
[source,groovy,subs="attributes,specialcharacters"]
----
compile 'com.rabbitmq.jms:rabbitmq-jms:{2-milestone}'
----
////

==== Snapshot

For snapshots, declare the <<snapshot-repository,snapshot repository>> in your dependency manager.

Then add the following to `pom.xml` for Maven:

.pom.xml
[source,xml,subs="attributes,specialcharacters"]
----
<dependency>
  <groupId>com.rabbitmq.jms</groupId>
  <artifactId>rabbitmq-jms</artifactId>
  <version>{2-snapshot}</version>
</dependency>
----

Or the following to `build.gradle` for Gradle:

.build.gradle
[source,groovy,subs="attributes,specialcharacters"]
----
compile 'com.rabbitmq.jms:rabbitmq-jms:{2-snapshot}'
----

[[jms-3]]
=== JMS 3.0

RabbitMQ JMS Client implements JMS 3.0 as of 3.0.0.

==== Stable

Add the following to `pom.xml` for Maven:

.pom.xml
[source,xml,subs="attributes,specialcharacters"]
----
<dependency>
  <groupId>com.rabbitmq.jms</groupId>
  <artifactId>rabbitmq-jms</artifactId>
  <version>{3-stable}</version>
</dependency>
----

Or the following to `build.gradle` for Gradle:

.build.gradle
[source,groovy,subs="attributes,specialcharacters"]
----
compile 'com.rabbitmq.jms:rabbitmq-jms:{3-stable}'
----


////
==== Milestone and Release Candidate

For milestones and release candidates, declare the <<milestone-rc-repository,milestone repository>> in your dependency manager.

Then add the following to `pom.xml` for Maven:

.pom.xml
[source,xml,subs="attributes,specialcharacters"]
----
<dependency>
  <groupId>com.rabbitmq.jms</groupId>
  <artifactId>rabbitmq-jms</artifactId>
  <version>{3-milestone}</version>
</dependency>
----

Or the following to `build.gradle` for Gradle:

.build.gradle
[source,groovy,subs="attributes,specialcharacters"]
----
compile 'com.rabbitmq.jms:rabbitmq-jms:{3-milestone}'
----
////

==== Snapshot

For snapshots, declare the <<snapshot-repository,snapshot repository>> in your dependency manager.

Then add the following to `pom.xml` for Maven:

.pom.xml
[source,xml,subs="attributes,specialcharacters"]
----
<dependency>
  <groupId>com.rabbitmq.jms</groupId>
  <artifactId>rabbitmq-jms</artifactId>
  <version>{3-snapshot}</version>
</dependency>
----

Or the following to `build.gradle` for Gradle:

.build.gradle
[source,groovy,subs="attributes,specialcharacters"]
----
compile 'com.rabbitmq.jms:rabbitmq-jms:{3-snapshot}'
----

[[milestone-rc-repository]]
=== Milestones and Release Candidates Repository

Milestones and release candidates are available on the RabbitMQ Milestone Repository.

Maven:

.pom.xml
[source,xml,subs="attributes,specialcharacters"]
----
<repositories>
  <repository>
    <id>packagecloud-rabbitmq-maven-milestones</id>
    <url>https://packagecloud.io/rabbitmq/maven-milestones/maven2</url>
    <releases>
      <enabled>true</enabled>
    </releases>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
  </repository>
</repositories>
----

Gradle:

.build.gradle
[source,groovy,subs="attributes,specialcharacters"]
----
repositories {
  maven {
    url "https://packagecloud.io/rabbitmq/maven-milestones/maven2"
  }
}
----

[[snapshot-repository]]
=== Snapshot Repository

Snapshots are available on https://oss.sonatype.org/content/repositories/snapshots/com/rabbitmq/jms/rabbitmq-jms/[Sonatype OSS snapshot repository].

Add the https://oss.sonatype.org/content/repositories/snapshots/com/rabbitmq/jms/rabbitmq-jms/[Sonatype OSS snapshot repository] to your dependency manager:

Maven:

.pom.xml
[source,xml,subs="attributes,specialcharacters"]
----
<repositories>
  <repository>
    <id>ossrh</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
    <releases>
      <enabled>false</enabled>
    </releases>
  </repository>
</repositories>
----

Gradle:

.build.gradle
[source,groovy,subs="attributes,specialcharacters"]
----
repositories {
  maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
  mavenCentral()
}
----

=== Building from Source

This project is managed by Maven, so use

```sh
./mvnw clean install -Dmaven.test.skip=true
```

to build it from source and install into the local repository.

== Running Tests

See https://github.com/rabbitmq/rabbitmq-jms-client/blob/main/CONTRIBUTING.md[CONTRIBUTING.md] for an overview of the development process.

=== Unit Tests

```sh
./mvnw clean test
```

=== Integration Tests

==== Running Integration Tests with Docker

Launch the broker:

```sh
docker run -it --rm --name rabbitmq -p 5672:5672 rabbitmq
```

Enable the JMS Topic Exchange plugin:

```sh
docker exec rabbitmq rabbitmq-plugins enable rabbitmq_jms_topic_exchange
```

Launch the tests:

```sh
./mvnw verify -Drabbitmqctl.bin=DOCKER:rabbitmq
```

==== Running Integration Tests with a Local Broker

To launch the test suite (requires a local RabbitMQ node with JMS Topic Exchange plugin enabled):

```sh
./mvnw verify -Drabbitmqctl.bin=/path/to/rabbitmqctl
```

=== JMS 1.1 Compliance Test Suite

https://github.com/rabbitmq/rabbitmq-jms-cts[JMS 1.1 compliance test suite] for this client is available
in a separate repository.

== Versioning

This library uses https://semver.org/[semantic versioning].

== Support

See the https://www.rabbitmq.com/java-versions.html[RabbitMQ Java libraries support page]
for the support timeline of this library.

== License and Copyright

(c) 2007-2022 VMware, Inc. or its affiliates.

This package, the RabbitMQ JMS client library, is double-licensed under the Apache License version 2 ("ASL") and the Mozilla Public License 2.0 ("MPL").

See https://github.com/rabbitmq/rabbitmq-jms-client/blob/main/LICENSE[LICENSE].

