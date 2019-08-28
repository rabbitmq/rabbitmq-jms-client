/* Copyright (c) 2019 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import javax.jms.Message;

/**
 * Internal interface to notify about a published message when publisher confirms are enabled.
 *
 * @since 1.13.0
 */
interface PublishingListener {

    void publish(Message message, long sequenceNumber);

}
