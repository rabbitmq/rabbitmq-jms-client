// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.parse.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * This test class generates all {@link SqlTreeNode}s possible at a parent of an {@link SqlParseTree} and
 * runs {@link SqlCompilerVisitor#visit()} against it.  The children are supplied (but should not matter).
 */
public class SqlCompilerVisitorTest {

    //Table of expected results:

    //SqlTreeType   arity  SqlTreeNode  (beforeResult)         (afterResult)
    //˜˜˜˜˜˜˜˜˜˜˜˜˜ ˜˜˜˜˜  ˜˜˜˜˜˜˜˜˜˜˜  ˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜  ˜˜˜˜˜˜˜˜˜˜˜˜˜
    //LEAF           0     IDENT        {'ident',<<"dummy">>}
    //                     STRING       <<"dummy">>
    //                     FLOAT        1.57e12
    //                     INT          42
    //                     HEX          42
    //                     LIST         [<<"a">>,<<"b">>]
    //                     TRUE         'true'
    //                     FALSE        'false'
    //LIST           0     LIST         [<<"a">>,<<"b">>]

    //POSTFIXUNARYOP 1     NULL         {'is_null'             }
    //                     NOT_NULL     {'not_null'            }
    //PREFIXUNARYOP  1     NOT          {'not'                 }
    //                     OP_MINUS     {'-'                   }
    //                     OP_PLUS      {'+'                   }
    //PATTERN1       1     *                                   'no_escape'

    //CONJUNCTION    2     *            {'and'                 }
    //DISJUNCTION    2     *            {'or'                  }
    //BINARYOP       2     CMP_EQ       {'='                   }
    //                     CMP_NEQ      {'<>'                  }
    //                     CMP_LT       {'<'                   }
    //                     CMP_LTEQ     {'<='                  }
    //                     CMP_GT       {'>='                  }
    //                     CMP_GTEQ     {'>='                  }
    //                     LIKE         {'like'                }
    //                     NOT_LIKE     {'not_like'            }
    //                     IN           {'in'                  }
    //                     NOT_IN       {'not_in'              }
    //                     OP_DIV       {'/'                   }
    //                     OP_MULT      {'*'                   }
    //                     OP_PLUS      {'+'                   }
    //                     OP_MINUS     {'-'                   }
    //PATTERN2       2     *

    //TERNARYOP      3     BETWEEN      {'between'             }
    //                     NOT_BETWEEN  {'not_between'         }

    // ‹arity› children are supplied, each of TEST type
    // *     means anything is allowed;

    private static final String DUMMY_FLOAT = "1.57E12";
    private static final String DUMMY_STRING = "dummySTRING";
    private static final String DUMMY_IDENT = "dummyIDENT";
    private static final List<String> TEST_LIST = new ArrayList<String>();
    static {
        TEST_LIST.add("a");
        TEST_LIST.add("b");
    }
    private static final String TEST_LIST_COMPILED = "[<<\"a\">>,<<\"b\">>]";

    @Test
    public void testVisit0() {
        //LEAF           0     IDENT        {'ident',<<"dummyIDENT">>}
        //                     STRING       <<"dummySTRING">>
        //                     FLOAT        1.57e12
        //                     INT          42
        //                     HEX          42
        //                     LIST         [<<"a">>,<<"b">>]
        //                     TRUE         'true'
        //                     FALSE        'false'
        assertCompileStep(SqlTreeType.LEAF, SqlTokenType.IDENT , 0, "{'ident',<<\""+DUMMY_IDENT+"\">>}", "");
        assertCompileStep(SqlTreeType.LEAF, SqlTokenType.STRING, 0, "<<\""+DUMMY_STRING+"\">>"         , "");
        assertCompileStep(SqlTreeType.LEAF, SqlTokenType.FLOAT , 0, DUMMY_FLOAT                        , "");
        assertCompileStep(SqlTreeType.LEAF, SqlTokenType.INT   , 0, "43"                               , "");
        assertCompileStep(SqlTreeType.LEAF, SqlTokenType.HEX   , 0, "42"                               , "");
        assertCompileStep(SqlTreeType.LEAF, SqlTokenType.LIST  , 0, TEST_LIST_COMPILED                 , "");
        assertCompileStep(SqlTreeType.LEAF, SqlTokenType.TRUE  , 0, "'true'"                           , "");
        assertCompileStep(SqlTreeType.LEAF, SqlTokenType.FALSE , 0, "'false'"                          , "");

        //LIST           0     LIST         [<<"a">>,<<"b">>]
        assertCompileStep(SqlTreeType.LIST, SqlTokenType.LIST  , 0, TEST_LIST_COMPILED                 , "");
    }

    @Test
    public void testVisit1() {
        //POSTFIXUNARYOP 1     NULL         {'is_null'             }
        //                     NOT_NULL     {'not_null'            }
        assertCompileStep(SqlTreeType.POSTFIXUNARYOP, SqlTokenType.NULL    , 1, "{'is_null'" , "}");
        assertCompileStep(SqlTreeType.POSTFIXUNARYOP, SqlTokenType.NOT_NULL, 1, "{'not_null'", "}");

        //PREFIXUNARYOP  1     NOT          {'not'                 }
        //                     OP_MINUS     {'-'                   }
        //                     OP_PLUS      {'+'                   }
        assertCompileStep(SqlTreeType.PREFIXUNARYOP, SqlTokenType.NOT      , 1, "{'not'"     , "}");
        assertCompileStep(SqlTreeType.PREFIXUNARYOP, SqlTokenType.OP_MINUS , 1, "{'-'"       , "}");
        assertCompileStep(SqlTreeType.PREFIXUNARYOP, SqlTokenType.OP_PLUS  , 1, "{'+'"       , "}");

        //PATTERN1       1     *
        assertCompileStep(SqlTreeType.PATTERN1     , SqlTokenType.TEST , 1, "", "'no_escape'");
    }

    @Test
    public void testVisit2() {
        //CONJUNCTION    2     *            {'and'                 }
        assertCompileStep(SqlTreeType.CONJUNCTION, SqlTokenType.TEST    , 2, "{'and'", "}");

        //DISJUNCTION    2     *            {'or'                  }
        assertCompileStep(SqlTreeType.DISJUNCTION, SqlTokenType.TEST    , 2, "{'or'" , "}");

        //BINARYOP       2     CMP_EQ       {'='                   }
        //                     CMP_NEQ      {'<>'                  }
        //                     CMP_LT       {'<'                   }
        //                     CMP_LTEQ     {'<='                  }
        //                     CMP_GT       {'>='                  }
        //                     CMP_GTEQ     {'>='                  }
        //                     LIKE         {'like'                }
        //                     NOT_LIKE     {'not_like'            }
        //                     IN           {'in'                  }
        //                     NOT_IN       {'not_in'              }
        //                     OP_DIV       {'/'                   }
        //                     OP_MULT      {'*'                   }
        //                     OP_PLUS      {'+'                   }
        //                     OP_MINUS     {'-'                   }
        assertCompileStep(SqlTreeType.BINARYOP, SqlTokenType.CMP_EQ  , 2, "{'='"       , "}");
        assertCompileStep(SqlTreeType.BINARYOP, SqlTokenType.CMP_NEQ , 2, "{'<>'"      , "}");
        assertCompileStep(SqlTreeType.BINARYOP, SqlTokenType.CMP_LT  , 2, "{'<'"       , "}");
        assertCompileStep(SqlTreeType.BINARYOP, SqlTokenType.CMP_LTEQ, 2, "{'<='"      , "}");
        assertCompileStep(SqlTreeType.BINARYOP, SqlTokenType.CMP_GT  , 2, "{'>'"       , "}");
        assertCompileStep(SqlTreeType.BINARYOP, SqlTokenType.CMP_GTEQ, 2, "{'>='"      , "}");
        assertCompileStep(SqlTreeType.BINARYOP, SqlTokenType.LIKE    , 2, "{'like'"    , "}");
        assertCompileStep(SqlTreeType.BINARYOP, SqlTokenType.NOT_LIKE, 2, "{'not_like'", "}");
        assertCompileStep(SqlTreeType.BINARYOP, SqlTokenType.IN      , 2, "{'in'"      , "}");
        assertCompileStep(SqlTreeType.BINARYOP, SqlTokenType.NOT_IN  , 2, "{'not_in'"  , "}");
        assertCompileStep(SqlTreeType.BINARYOP, SqlTokenType.OP_DIV  , 2, "{'/'"       , "}");
        assertCompileStep(SqlTreeType.BINARYOP, SqlTokenType.OP_MULT , 2, "{'*'"       , "}");
        assertCompileStep(SqlTreeType.BINARYOP, SqlTokenType.OP_PLUS , 2, "{'+'"       , "}");
        assertCompileStep(SqlTreeType.BINARYOP, SqlTokenType.OP_MINUS, 2, "{'-'"       , "}");

        //PATTERN2       2     *
        assertCompileStep(SqlTreeType.PATTERN2, SqlTokenType.TEST, 2, "", "");
    }

    @Test
    public void testVisit3() {
        //TERNARYOP      3     BETWEEN      {'between'             }
        //                     NOT_BETWEEN  {'not_between'         }
        assertCompileStep(SqlTreeType.TERNARYOP, SqlTokenType.BETWEEN    , 3, "{'between'"    , "}");
        assertCompileStep(SqlTreeType.TERNARYOP, SqlTokenType.NOT_BETWEEN, 3, "{'not_between'", "}");
    }

    private void assertCompileStep(SqlTreeType treeType, SqlTokenType tokenType, int arity, String expectedBefore, String expectedAfter) {
        SqlCompilerVisitor scv = new SqlCompilerVisitor();
        SqlTreeNode parent = new SqlTreeNode(treeType, testSqlToken(tokenType));
        SqlTreeNode[] children = testChildren(arity);

        scv.visitBefore(parent, children);
        assertEquals(expectedBefore, scv.extractCode(), String.format("", treeType, tokenType));
        scv.clearCode();
        assertEquals("", scv.extractCode(), "extractCode() not cleared after visitBefore() and clearCode()");

        scv.visitAfter(parent, children);
        assertEquals(expectedAfter, scv.extractCode(), String.format("", treeType, tokenType));
        scv.clearCode();
        assertEquals("", scv.extractCode(), "extractCode() not cleared after visitAfter() and clearCode()");
    }

    private SqlTreeNode[] testChildren(int arity) {
        SqlTreeNode[] children = new SqlTreeNode[arity];
        for (int i=0; i<arity; ++i) {
            children[i] = new SqlTreeNode(SqlTreeType.LEAF, testSqlToken(SqlTokenType.TEST));
        }
        return children;
    }

    private SqlToken testSqlToken(SqlTokenType tokenType) {
        switch (tokenType) {
        case FLOAT:
            return new SqlToken(tokenType, DUMMY_FLOAT);
        case HEX:
            return new SqlToken(tokenType, "0x2A");              //  value 42
        case IDENT:
            return new SqlToken(tokenType, DUMMY_IDENT);
        case INT:
            return new SqlToken(tokenType, "43");
        case LIST:
            return new SqlToken(tokenType, TEST_LIST);
        case STRING:
            return new SqlToken(tokenType, "'"+DUMMY_STRING+"'"); // note quotes
        case LP:
        case RP:
        case WS:
            fail(String.format("Test token generated of type [%s]", tokenType));
        default:
            return new SqlToken(tokenType, tokenType.opCode());
        }
    }
}
