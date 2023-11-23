// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

/**
 * Authentication mechanisms that the client can use to authenticate to the server.
 *
 * @see com.rabbitmq.jms.admin.RMQConnectionFactory#setAuthenticationMechanism(AuthenticationMechanism)
 */
public enum AuthenticationMechanism {

    /**
     * Authentication mechanism corresponding to {@link com.rabbitmq.client.DefaultSaslConfig#PLAIN}
     */
    PLAIN,
    /**
     * Authentication mechanism corresponding to {@link com.rabbitmq.client.DefaultSaslConfig#EXTERNAL}
     */
    EXTERNAL;
}
