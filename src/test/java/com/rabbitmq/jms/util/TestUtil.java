/* Copyright (c) 2013 GoPivotal, Inc. All rights reserved. */
package com.rabbitmq.jms.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class TestUtil {

    private static final String PREFIX = "abcd-";

    /**
     * Test generateUUID puts the prefix on.
     */
    @Test
    public void generateUUIDPrefix() throws Exception {
        assertTrue("Prefix "+PREFIX+" not added correctly", Util.generateUUID(PREFIX).startsWith(PREFIX));
        assertTrue("Null case doesn't start with null", Util.generateUUID(null).startsWith("null")); // special case
    }

    private static final int REPEAT_COUNT = 1000;

    /**
     * Test generateUUID doesn't repeat quickly.
     */
    @Test
    public void generateUUIDMultiple() throws Exception {
        List<String> uuidList = new LinkedList<String>();

        for(int i=0; i<REPEAT_COUNT; ++i) {
            uuidList.add(Util.generateUUID(""));
        }

        Set<String> uuidSet = new HashSet<String>(uuidList);
        assertEquals("Generated a duplicate uuid!", REPEAT_COUNT, uuidSet.size());
    }

}
