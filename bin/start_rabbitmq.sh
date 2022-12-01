#!/usr/bin/env bash

LOCAL_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

RABBITMQ_IMAGE_TAG=${RABBITMQ_IMAGE_TAG:-3.11.0-rc.2.29.geee5757.dirty}
RABBITMQ_IMAGE=${RABBITMQ_IMAGE:-pivotalrabbitmq/rabbitmq}

wait_for_message() {
  while ! docker logs $1 | grep -q "$2";
  do
      sleep 5
      echo "Waiting 5sec for $1 to start ..."
  done
}

echo "Download required plugins"
wget https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/3.11.1/rabbitmq_delayed_message_exchange-3.11.1.ez

cat > ${PWD}/enabled_plugins << ENDOFFILE
[rabbitmq_management,rabbitmq_jms_topic_exchange,rabbitmq_delayed_message_exchange].
ENDOFFILE

echo "Running RabbitMQ ${RABBITMQ_IMAGE}:${RABBITMQ_IMAGE_TAG}"

docker rm -f rabbitmq 2>/dev/null || echo "rabbitmq was not running"
docker run -d --name rabbitmq \
    -p 15672:15672 -p 5672:5672 \
    -v ${PWD}/enabled_plugins:/etc/rabbitmq/enabled_plugins \
    -v ${PWD}/rabbitmq_delayed_message_exchange-3.11.1.ez:/opt/rabbitmq/plugins/rabbitmq_delayed_message_exchange-3.11.1.ez \
    ${RABBITMQ_IMAGE}:${RABBITMQ_IMAGE_TAG}


wait_for_message rabbitmq "Server startup complete"
