## Changes Between 1.5.x and 1.6.0

### Configurable On Message Timeout

Contributed by Guillaume Mornet.

GitHub issue: [rabbitmq-jms-client#5](https://github.com/rabbitmq/rabbitmq-jms-client/issues/5)


## Changes Between 1.4.7 and 1.5.0

`1.5.0` is a maintainence release that includes a fix
for `CVE-2016-6194`.

### Limited ObjectMessage Deserialization

Classes that can be deserialized from `javax.jms.ObjectMessage`
now can be limited via a package prefix white list.
There are two ways to do it:

 * Via the `com.rabbitmq.jms.TrustedPackagesPrefixes` JVM property which accepts
   a comma separated list of prefixes, for example, `java,com.rabbitmq,com.mycompany`
 * Using `RMQConnectionFactory#setTrustedPackages`, `RMQConnection#setTrustedPackages`, or `RMQSession#setTrustedPackages`
   which accept lists of package prefixes

All options take a list of package name prefixes, e.g. `java` will make all classes
in `java.lang`, `java.util`, and other packages starting with `java` trusted.
Deserialization attempt for untrusted classes will throw an exception.

GH issue: [rabbitmq-jms-client#3](https://github.com/rabbitmq/rabbitmq-jms-client/issues/3).

This fixes `CVE-2016-6194` (note: attacker must be authenticated
with RabbitMQ in order to carry out the attack).


## RabbitMQ Java Client Dependency Update

This client now depends on RabbitMQ Java client `3.6.3`.
