#!/usr/bin/env bash

RABBITMQ_IMAGE=${RABBITMQ_IMAGE:-rabbitmq:3.12}
DELAYED_MESSAGE_EXCHANGE_PLUGIN_VERSION=${DELAYED_MESSAGE_EXCHANGE_PLUGIN_VERSION:-3.12.0}

wait_for_message() {
  while ! docker logs "$1" | grep -q "$2";
  do
      sleep 5
      echo "Waiting 5 seconds for $1 to start..."
  done
}

make -C "${PWD}"/tls-gen/basic

mkdir -p rabbitmq-configuration/tls
cp -R "${PWD}"/tls-gen/basic/result/* rabbitmq-configuration/tls
chmod o+r rabbitmq-configuration/tls/*
chmod g+r rabbitmq-configuration/tls/*

echo "[rabbitmq_jms_topic_exchange,rabbitmq_delayed_message_exchange]." > rabbitmq-configuration/enabled_plugins

echo "loopback_users = none

listeners.ssl.default = 5671

ssl_options.cacertfile = /etc/rabbitmq/tls/ca_certificate.pem
ssl_options.certfile   = /etc/rabbitmq/tls/server_$(hostname)_certificate.pem
ssl_options.keyfile    = /etc/rabbitmq/tls/server_$(hostname)_key.pem
ssl_options.verify     = verify_peer
ssl_options.fail_if_no_peer_cert = false" > rabbitmq-configuration/rabbitmq.conf

echo "Running RabbitMQ ${RABBITMQ_IMAGE}"

docker rm -f rabbitmq 2>/dev/null || echo "rabbitmq was not running"
docker run -d --name rabbitmq \
    --network host \
    -v "${PWD}"/rabbitmq-configuration:/etc/rabbitmq \
    -v "${PWD}"/ci/rabbitmq_delayed_message_exchange-"${DELAYED_MESSAGE_EXCHANGE_PLUGIN_VERSION}".ez:/opt/rabbitmq/plugins/rabbitmq_delayed_message_exchange-"${DELAYED_MESSAGE_EXCHANGE_PLUGIN_VERSION}".ez \
    "${RABBITMQ_IMAGE}"

wait_for_message rabbitmq "completed with"

docker exec rabbitmq rabbitmq-diagnostics erlang_version
docker exec rabbitmq rabbitmqctl version
