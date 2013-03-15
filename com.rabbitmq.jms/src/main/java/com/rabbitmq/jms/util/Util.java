/* Copyright © 2013 VMware, Inc. All rights reserved. */
package com.rabbitmq.jms.util;

import java.util.UUID;

/**
 * Utility class which generates unique string identifiers.
 */
public final class Util {
    /**
     * Generates a UUID string identifier.
     * @param prefix - prefix of uniquely generated string; may be null, in which case "null" is the prefix used.
     * @return a UUID string with given prefix
     */
    public final static String generateUUID(String prefix) {
        return new StringBuilder().append(prefix).append(UUID.randomUUID()).toString();
    }
}
