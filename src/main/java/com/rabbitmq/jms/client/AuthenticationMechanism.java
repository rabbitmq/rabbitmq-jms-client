// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.jms.client;

/**
 * Authentication mechanisms that the client can use to authenticate to the server.
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
