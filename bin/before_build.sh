#!/bin/sh

${HOP_RABBITMQ_PLUGINS:="sudo rabbitmq-plugins"}

$HOP_RABBITMQ_PLUGINS enable rabbitmq_jms_topic_exchange

sleep 3

true
