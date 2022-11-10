// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2019-2022 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.jms.client;

import javax.jms.CompletionListener;
import javax.jms.Message;

/**
 * Internal interface to notify about a published message when publisher confirms are enabled.
 *
 * @since 1.13.0
 */
interface PublishingListener {

    void publish(Message message, CompletionListener completionListener, long sequenceNumber);

}
