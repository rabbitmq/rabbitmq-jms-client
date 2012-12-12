package com.rabbitmq.jms.util;

import java.util.UUID;

/**
 * Utility class which generates unique string identifiers.
 */
public final class Util {
    /**
     * Generates a UUID string identifier.
     * @param prefix - prefix of uniquely generated string; must not be null
     * @return a UUID string with given prefix
     */
    public final static String generateUUID(String prefix) {
        return prefix + UUID.randomUUID().toString();
    }
}
