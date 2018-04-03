#!/bin/sh

rm -rf /tmp/tls-gen
git clone https://github.com/michaelklishin/tls-gen.git /tmp/tls-gen
make -C /tmp/tls-gen/basic
./mvnw clean resources:testResources -Dtest-tls-certs.dir=/tmp/tls-gen/basic

wget https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.7.4/rabbitmq-server-generic-unix-3.7.4.tar.xz
tar xf rabbitmq-server-generic-unix-3.7.4.tar.xz

cp target/test-classes/rabbit@localhost.config rabbitmq_server-3.7.4/etc/rabbitmq/rabbitmq.config
rabbitmq_server-3.7.4/sbin/rabbitmq-plugins enable rabbitmq_jms_topic_exchange
rabbitmq_server-3.7.4/sbin/rabbitmq-server -detached
export RABBITMQ_CTL=rabbitmq_server-3.7.4/sbin/rabbitmqctl

sleep 3

true
