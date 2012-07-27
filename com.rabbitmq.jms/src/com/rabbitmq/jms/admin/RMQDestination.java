/**
 *
 */
package com.rabbitmq.jms.admin;

import java.io.Serializable;

import javax.jms.Destination;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

/**
 * RabbitMQ implementation of JMS {@link Destination}
 */
public class RMQDestination implements Destination, Referenceable, Serializable {

    /** Default serializable uid */
    private static final long serialVersionUID = 1L;

    @Override
    public Reference getReference() throws NamingException {
        return new Reference(this.getClass().getCanonicalName());
    }

}
