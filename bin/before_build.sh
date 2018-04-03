#!/bin/sh

${HOP_RABBITMQ_PLUGINS:="sudo rabbitmq-plugins"}

$HOP_RABBITMQ_PLUGINS enable rabbitmq_jms_topic_exchange

rm -rf /tmp/tls-gen
git clone https://github.com/michaelklishin/tls-gen.git /tmp/tls-gen
make -C /tmp/tls-gen/basic
./mvnw clean resources:testResources -Dtest-tls-certs.dir=/tmp/tls-gen/basic
cp target/test-classes/rabbit@localhost.config /tmp/rabbitmq.config
sudo service rabbitmq-server start

sleep 3

true
