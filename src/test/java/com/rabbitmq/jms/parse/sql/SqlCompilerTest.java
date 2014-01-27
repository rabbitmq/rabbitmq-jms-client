package com.rabbitmq.jms.parse.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class SqlCompilerTest {

    @Test
    public void simpleCompiles() {
        assertCompile( "nothing is null"
                     , "{'is_null',{'ident',<<\"nothing\">>}}" );

        assertCompile( "nothing IS NULL And JMSPrefix > 12-4"
                     , "{'and'"+
                       ",{'is_null',{'ident',<<\"nothing\">>}}"+
                       ",{'>'"+
                         ",{'ident',<<\"JMSPrefix\">>}"+
                         ",{'-',12,4}}}" );
    }

    private void assertCompile(String inStr, String outStr) {
        SqlTokenStream stream = new SqlTokenStream(inStr);
        assertEquals("Residue not empty", "", stream.getResidue());

        SqlParser sp = new SqlParser(stream);
        SqlParseTree pt = sp.parse();

        if (!sp.parseOk()) {
            fail(sp.getErrorMessage());
        }

        SqlCompiler compiled = new SqlCompiler(sp, Collections.<String, SqlExpressionType> emptyMap());
        if (!compiled.compileOk()) {
            fail("Did not compile, tree=" + Arrays.toString(pt.formattedTree()));
        }
        assertNull("Error message generated!", compiled.getErrorMessage());
        assertEquals("Compiled code doesn't match", outStr, compiled.compile());
    }

}
