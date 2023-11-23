// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.parse.sql;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class SqlParserTest {

    @Test
    public void simpleTree() throws Exception {
        assertParse( "nothing IS NULL"
                   , "POSTFIXUNARYOP: is_null"
                   , "    LEAF: ident: nothing"
                   );
    }

    @Test
    public void moreTrees() throws Exception {
        assertParse( "nothing IS NULL And JMSPriority > 12-4"
                   , "CONJUNCTION:"
                   , "    POSTFIXUNARYOP: is_null"
                   , "        LEAF: ident: nothing"
                   , "    BINARYOP: >"
                   , "        LEAF: ident: JMSPriority"
                   , "        BINARYOP: -"
                   , "            LEAF: integer: 12"
                   , "            LEAF: integer: 4"
                   );
        assertParse( "nothing IS NulL oR JMSPriority > 12- 4"
                   , "DISJUNCTION:"
                   , "    POSTFIXUNARYOP: is_null"
                   , "        LEAF: ident: nothing"
                   , "    BINARYOP: >"
                   , "        LEAF: ident: JMSPriority"
                   , "        BINARYOP: -"
                   , "            LEAF: integer: 12"
                   , "            LEAF: integer: 4"
                   );
        assertParse( "nothing IS NulL oR (JMSPriority >= 12/-4.3)"
                   , "DISJUNCTION:"
                   , "    POSTFIXUNARYOP: is_null"
                   , "        LEAF: ident: nothing"
                   , "    BINARYOP: >="
                   , "        LEAF: ident: JMSPriority"
                   , "        BINARYOP: /"
                   , "            LEAF: integer: 12"
                   , "            PREFIXUNARYOP: -"
                   , "                LEAF: float: 4.3"
                   );
    }

    @Test
    public void incompleteParse() throws Exception {
        SqlTokenStream tokenStream = new SqlTokenStream("not 2 < 3 (-1)");
        SqlParser sp = new SqlParser(tokenStream);
        sp.parse();

        if ("".equals(tokenStream.getResidue()) && sp.parseOk()) {
            fail("Parse did not fail!");
        } else {
            assertEquals("Terminated before end of stream; next token is: '(' at index: 4."
                        , sp.getErrorMessage()
                        , "Parse did not fail with the right message."
                        );
        }
    }

    @Test
    public void badParse() throws Exception {
        SqlTokenStream tokenStream = new SqlTokenStream("true and");
        SqlParser sp = new SqlParser(tokenStream);
        sp.parse();

        if ("".equals(tokenStream.getResidue()) && sp.parseOk()) {
            fail("Parse did not fail!");
        } else {
            assertEquals("Terminated before end of stream; next token is: 'and' at index: 1."
                        , sp.getErrorMessage()
                        , "Parse did not fail with the right message."
                        );
        }
    }

    @Test
    public void unopenedQuoteParse() throws Exception {
        SqlTokenStream tokenStream = new SqlTokenStream("'abc' = abc'");
        SqlParser sp = new SqlParser(tokenStream);
        sp.parse();

        if ("".equals(tokenStream.getResidue()) && sp.parseOk()) {
            fail("Parse did not fail!");
        } else {
            assertEquals("'"
                        , tokenStream.getResidue()
                        , "Parse did not fail correctly. Residue not valid."
                        );
        }
    }

    private void assertParse(String inStr, String... outStr) {
        SqlTokenStream stream = new SqlTokenStream(inStr);
        assertEquals("", stream.getResidue(), "Residue not empty");

        SqlParser sp = new SqlParser(stream);
        SqlParseTree pt = sp.parse();

        if (!sp.parseOk()) {
            fail(sp.getErrorMessage());
        }

        String[] formatted = pt.formattedTree();
        assertArrayEquals(outStr, formatted, "Parsed tree doesn't match");
    }

}
