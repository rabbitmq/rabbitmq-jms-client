# Changes Between 1.4.7 and 1.5.0

`1.5.0` is a maintainence release that includes a fix
for `CVE-2016-6194`.

## Limited ObjectMessage Deserialization

Classes that can be deserialized from `javax.jms.ObjectMessage`
now can be limited via a package prefix white list.

GH issue: [rabbitmq-jms-client#3](https://github.com/rabbitmq/rabbitmq-jms-client/issues/3).

This fixes `CVE-2016-6194`.


## RabbitMQ Java Client Dependency Update

This client now depends on RabbitMQ Java client `3.6.3`.
