#!/bin/sh

rm -rf /tmp/tls-gen
git clone https://github.com/michaelklishin/tls-gen.git /tmp/tls-gen
make -C /tmp/tls-gen/basic
./mvnw clean resources:testResources -Dtest-tls-certs.dir=/tmp/tls-gen/basic

wget https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.7.7/rabbitmq-server-generic-unix-3.7.7.tar.xz
tar xf rabbitmq-server-generic-unix-3.7.7.tar.xz
mv rabbitmq_server-3.7.7 rabbitmq

cp target/test-classes/rabbit@localhost.config rabbitmq/etc/rabbitmq/rabbitmq.config
rabbitmq/sbin/rabbitmq-plugins enable rabbitmq_jms_topic_exchange
rabbitmq/sbin/rabbitmq-server -detached

sleep 3

true
