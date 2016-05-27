# Java JMS Client for RabbitMQ

**WIP warning**: open sourcing of RabbitMQ JMS repositories is a
work-in-progress. There are currently no publicly released Maven
artifacts and compliance test suite is still not open source.

## Overview

This is a JMS 1.1 client library for RabbitMQ, working in concert with [rabbitmq-jms-topic-exchange](https://github.com/rabbitmq/rabbitmq-jms-topic-exchange),
a RabbitMQ server plugin.

## Installation

### From Maven

This repository is in the process of being prepared for open sourcing. It is not yet available
from any public Maven repository.


### Building from Source

This project is managed by Maven, so use

    mvn clean install

to build it from source and install into the local repository.


## Running Tests

See [CONTRIBUTING.md](./CONTRIBUTING.md) for an overview of the development process.

### Unit Tests

    mvn clean test

### Integration Tests

TBD: open sourcing the compliance test suite is a work-in-progress.


## License and Copyright

(c) Pivotal Software, Inc., 2007-2016.

This package, the RabbitMQ JMS client library, is triple-licensed
under the Apache License version 2 ("ASL"), the Mozilla Public License
1.1 ("MPL"), and the GNU General Public License version 2 ("GPL").

See [LICENSE](./LICENSE).
