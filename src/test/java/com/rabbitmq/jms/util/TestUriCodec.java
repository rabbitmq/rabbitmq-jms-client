// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.util;

import static com.rabbitmq.jms.util.UriCodec.decHost;
import static com.rabbitmq.jms.util.UriCodec.decPath;
import static com.rabbitmq.jms.util.UriCodec.decScheme;
import static com.rabbitmq.jms.util.UriCodec.decSegment;
import static com.rabbitmq.jms.util.UriCodec.decUserinfo;
import static com.rabbitmq.jms.util.UriCodec.encHost;
import static com.rabbitmq.jms.util.UriCodec.encPath;
import static com.rabbitmq.jms.util.UriCodec.encScheme;
import static com.rabbitmq.jms.util.UriCodec.encSegment;
import static com.rabbitmq.jms.util.UriCodec.encUserinfo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


;

public class TestUriCodec {

    private static final String[][] UI_TESTS =
        {{"simple valid string", "simplename" , "simplename"     }
        ,{"colon name"         , "simple:name", "simple:name"    }
        ,{"multi-colon-name"   , "s:i:m::p:"  , "s:i:m::p:"      }
        ,{"slash-at-string"    , "sim/at@name", "sim%2Fat%40name"}
        ,{"spaced name"        , "spaced name", "spaced%20name"  }
        ,{"english pound name" , "pound£name" , "pound%C2%A3name"}
        };
    @Test
    public void testEncUserinfo() {
        for (String[] arr : UI_TESTS) {
            assertEquals(arr[2], encUserinfo(arr[1], "UTF-8"), "Failed to encode "+arr[0]);
        }
    }

    @Test
    public void testDecUserinfo() {
        for (String[] arr : UI_TESTS) {
            assertEquals(arr[1], decUserinfo(arr[2], "UTF-8"), "Failed to decode "+arr[0]);
        }
    }

    private static final String[][] SC_TESTS =
        {{"simple valid string", "simplename" , "simplename"     }
        ,{"colon name"         , "simple:name", "simple%3Aname"  }
        ,{"slash-at-string"    , "sim/at@name", "sim%2Fat%40name"}
        ,{"spaced name"        , "spaced name", "spaced%20name"  }
        ,{"english pound name" , "pound£name" , "pound%C2%A3name"}
        };
    @Test
    public void testEncScheme() {
        for (String[] arr : SC_TESTS) {
            assertEquals(arr[2], encScheme(arr[1], "UTF-8"), "Failed to encode "+arr[0]);
        }
    }

    @Test
    public void testDecScheme() {
        for (String[] arr : SC_TESTS) {
            assertEquals(arr[1], decScheme(arr[2], "UTF-8"), "Failed to decode "+arr[0]);
        }
    }

    private static final String[][] SEG_TESTS =
        {{"simple valid string", "simplename" , "simplename"     }
        ,{"colon name"         , "simple:name", "simple:name"    }
        ,{"slash-at-string"    , "sim/at@name", "sim%2Fat@name"  }
        ,{"spaced name"        , "spaced name", "spaced%20name"  }
        ,{"english pound name" , "pound£name" , "pound%C2%A3name"}
        };
    @Test
    public void testEncSegment() {
        for (String[] arr : SEG_TESTS) {
            assertEquals(arr[2], encSegment(arr[1], "UTF-8"), "Failed to encode "+arr[0]);
        }
    }

    @Test
    public void testDecSegment() {
        for (String[] arr : SEG_TESTS) {
            assertEquals(arr[1], decSegment(arr[2], "UTF-8"), "Failed to decode "+arr[0]);
        }
    }

    private static final String[][] HOST_TESTS =
        {{"simple valid string", "simplename" , "simplename"     }
        ,{"colon name"         , "simple:name", "simple:name"    }
        ,{"slash-at-string"    , "sim/at@name", "sim%2Fat%40name"}
        ,{"spaced name"        , "spaced name", "spaced%20name"  }
        ,{"english pound name" , "pound£name" , "pound%C2%A3name"}
        };
    @Test
    public void testEncHost() {
        for (String[] arr : HOST_TESTS) {
            assertEquals(arr[2], encHost(arr[1], "UTF-8"), "Failed to encode "+arr[0]);
        }
    }

    @Test
    public void testDecHost() {
        for (String[] arr : HOST_TESTS) {
            assertEquals(arr[1], decHost(arr[2], "UTF-8"), "Failed to decode "+arr[0]);
        }
    }

    private static final String[][] PATH_TESTS =
        {{"simple valid string", "simplename" , "simplename"     }
        ,{"colon name"         , "simple:name", "simple:name"    }
        ,{"slash-at-string"    , "sim/at@name", "sim/at@name"    }
        ,{"spaced name"        , "spaced name", "spaced%20name"  }
        ,{"english pound name" , "pound£name" , "pound%C2%A3name"}
        };
    @Test
    public void testEncPath() {
        for (String[] arr : PATH_TESTS) {
            assertEquals(arr[2], encPath(arr[1], "UTF-8"), "Failed to encode "+arr[0]);
        }
    }

    @Test
    public void testDecPath() {
        for (String[] arr : PATH_TESTS) {
            assertEquals(arr[1], decPath(arr[2], "UTF-8"), "Failed to decode "+arr[0]);
        }
    }

}
