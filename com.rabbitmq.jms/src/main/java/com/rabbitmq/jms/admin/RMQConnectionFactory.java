//
// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License
// at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
// the License for the specific language governing rights and
// limitations under the License.
//
// The Original Code is RabbitMQ.
//
// The Initial Developer of the Original Code is VMware, Inc.
// Copyright (c) 2012 VMware, Inc. All rights reserved.
//
package com.rabbitmq.jms.admin;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

/**
 * RabbitMQ Implementation of JMS {@link ConnectionFactory}
 */
public class RMQConnectionFactory implements ConnectionFactory, Referenceable, Serializable {

    /** Default serializable uid. */
    private static final long serialVersionUID = 1L;

    private RMQConnectionProperties properties = null;

    public static RMQConnectionFactory newConnectionFactory(RMQConnectionProperties properties) {
        RMQConnectionFactory factory = new RMQConnectionFactory();
        factory.properties = properties;
        return factory;
    }

    @Override
    public Connection createConnection() throws JMSException {
        return createConnection(properties.getUsername(), properties.getPassword());
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Reference getReference() throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

}
