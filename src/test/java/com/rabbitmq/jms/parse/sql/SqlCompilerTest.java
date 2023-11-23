// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.parse.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

public class SqlCompilerTest {

    @Test
    public void simpleCompiles() {
        assertCompile( "nothing is null"
                     , "{'is_null',{'ident',<<\"nothing\">>}}." );

        assertCompile( "nothing IS NULL And JMSPrefix > 12-4"
                     , "{'and'"+
                       ",{'is_null',{'ident',<<\"nothing\">>}}"+
                       ",{'>'"+
                         ",{'ident',<<\"JMSPrefix\">>}"+
                         ",{'-',12,4}}}." );
    }

    private void assertCompile(String inStr, String outStr) {
        SqlTokenStream stream = new SqlTokenStream(inStr);
        assertEquals("", stream.getResidue(), "Residue not empty");

        SqlParser sp = new SqlParser(stream);
        SqlParseTree pt = sp.parse();

        if (!sp.parseOk()) {
            fail(sp.getErrorMessage());
        }

        SqlCompiler compiled = new SqlCompiler(new SqlEvaluator(sp, Collections.<String, SqlExpressionType> emptyMap()));
        if (!compiled.compileOk()) {
            fail("Did not compile, tree=" + Arrays.toString(pt.formattedTree()));
        }
        assertNull(compiled.getErrorMessage(), "Error message generated!");
        assertEquals(outStr, compiled.compile(), "Compiled code doesn't match");
    }

}
