// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class TestUtil {

    private static final String PREFIX = "abcd-";

    /**
     * Test generateUUID puts the prefix on.
     */
    @Test
    public void generateUUIDPrefix() throws Exception {
        assertTrue(Util.generateUUID(PREFIX).startsWith(PREFIX), "Prefix '"+PREFIX+"' not added correctly");
        assertTrue(Util.generateUUID(null).startsWith("null"), "Null case doesn't start with null"); // special case
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

        assertEquals(REPEAT_COUNT, setOf(uuidList).size(), "Generated a duplicate uuid!");
    }

    private static <Obj> Set<Obj> setOf(List<Obj> list) { return new HashSet<Obj>(list); }
}
