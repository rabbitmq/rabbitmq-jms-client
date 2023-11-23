// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.parse.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rabbitmq.jms.parse.Multiples.Pair;
import com.rabbitmq.jms.parse.Multiples.Triple;
import com.rabbitmq.jms.parse.Visitor;

/**
 * This test class generates all combinations of {@link SqlTreeNode}s possible at a point of an {@link SqlParseTree} and
 * runs {@link SqlTypeSetterVisitor#visitAfter(SqlTreeNode, SqlTreeNode[])} ()} against them.  There are tables of expected results, and
 * if the combination isn't in the table the result is expected to be {@link SqlExpressionType SqlExpressionType.INVALID}.
 */
public class SqlTypeSetterVisitorTest {

    //Table of expected results:

    //SqlTreeType   arity  SqlTreeNode    SqlTreeType.expType()...  (Result)
    //˜˜˜˜˜˜˜˜˜˜˜˜˜ ˜˜˜˜˜  ˜˜˜˜˜˜˜˜˜˜˜    ˜˜˜˜˜˜˜ ˜˜˜˜˜˜˜ ˜˜˜˜˜˜˜   ˜˜˜˜˜˜˜˜˜
    //LEAF           0     IDENT                                    (id-type)
    //                     STRING                                   (STRING)
    //                     FLOAT                                    (ARITH)
    //                     INT                                      (ARITH)
    //                     HEX                                      (ARITH)
    //                     LIST                                     (LIST)
    //                     TRUE                                     (BOOL)
    //                     FALSE                                    (BOOL)
    //LIST           0     LIST                                     (LIST)

    //POSTFIXUNARYOP 1     NULL           *                         (BOOL)
    //                     NOT_NULL       *                         (BOOL)
    //PREFIXUNARYOP  1     NOT            BOOL+                     (BOOL)
    //                     OP_MINUS       ARITH+                    (ARITH)
    //                     OP_PLUS        ARITH+                    (ARITH)
    //PATTERN1       1     *              *                         (NOT_SET)

    //CONJUNCTION
    //DISJUNCTION    2     *              BOOL+   BOOL+             (BOOL)
    //BINARYOP       2     CMP_EQ
    //                     CMP_NEQ        BOOL+   BOOL+             (BOOL)
    //                                    STRING+ STRING+           (BOOL)
    //                                    ARITH+  ARITH+            (BOOL)
    //                     CMP_LT
    //                     CMP_LTEQ
    //                     CMP_GT
    //                     CMP_GTEQ       ARITH+  ARITH+            (BOOL)
    //                     LIKE
    //                     NOT_LIKE
    //                     IN
    //                     NOT_IN         STRING+ *                 (BOOL)
    //                     OP_DIV
    //                     OP_MULT
    //                     OP_PLUS
    //                     OP_MINUS       ARITH+  ARITH+            (ARITH)
    //PATTERN2       2     *              *       *                 (NOT_SET)

    //TERNARYOP      3     BETWEEN
    //                     NOT_BETWEEN    ARITH+  ARITH+  ARITH+    (BOOL)

    // id-type means look the identifier up in the map for its type; ANY if not there.
    // type+ means type|ANY;
    // *     means anything is allowed;

    // things that do not occur in the table should give the result INVALID.

    // Some useful pre-typed identifiers
    private final Map<String, SqlExpressionType> idTypes = new HashMap<String, SqlExpressionType>();

    private static final SqlExpressionType[] NO_EXPRESSIONS = new SqlExpressionType[0];

    private Visitor<SqlTreeNode> tsVisitorBasic;
    private Visitor<SqlTreeNode> tsVisitorIdents;

    @BeforeEach
    public void setUp() throws Exception {
        // Some useful pre-typed identifiers
        idTypes.put("aBool"     , SqlExpressionType.BOOL    );
        idTypes.put("aArith"    , SqlExpressionType.ARITH   );
        idTypes.put("aAny"      , SqlExpressionType.ANY     );
        idTypes.put("aString"   , SqlExpressionType.STRING  );
        idTypes.put("aInvalid"  , SqlExpressionType.INVALID );
        idTypes.put("aList"     , SqlExpressionType.LIST    );
        idTypes.put("aNotSet"   , SqlExpressionType.NOT_SET );

        tsVisitorBasic = new SqlTypeSetterVisitor();
        tsVisitorIdents = new SqlTypeSetterVisitor(idTypes);
    }

    @Test
    public void testVisit0() {
        visit0(false);
        visit0(true);
    }

    private void visit0(boolean withIdents) {
        //LEAF           0     IDENT                                    (id-type)
        //                     STRING                                   (STRING)
        //                     FLOAT                                    (ARITH)
        //                     INT                                      (ARITH)
        //                     HEX                                      (ARITH)
        //                     LIST                                     (LIST)
        //                     TRUE                                     (BOOL)
        //                     FALSE                                    (BOOL)
        //LIST           0     LIST                                     (LIST)
        checkSetExpTypeForLeaves( withIdents
        , SqlTreeType.LEAF
        , os(SqlTokenType.IDENT, SqlTokenType.STRING     , SqlTokenType.FLOAT     , SqlTokenType.INT       , SqlTokenType.HEX       , SqlTokenType.LIST     , SqlTokenType.TRUE     , SqlTokenType.FALSE    )
        , os(null,               SqlExpressionType.STRING, SqlExpressionType.ARITH, SqlExpressionType.ARITH, SqlExpressionType.ARITH, SqlExpressionType.LIST, SqlExpressionType.BOOL, SqlExpressionType.BOOL)
          // null expressionType in result means the type is determined by the identifier token
        );
        checkSetExpTypeForLeaves( withIdents
        , SqlTreeType.LIST
        , os(SqlTokenType.LIST     )
        , os(SqlExpressionType.LIST)
        );
    }

    @Test
    public void testVisit1() {
        //POSTFIXUNARYOP 1     NULL           *                         (BOOL)
        //                     NOT_NULL       *                         (BOOL)
        //PREFIXUNARYOP  1     NOT            BOOL+                     (BOOL)
        //                     OP_MINUS       ARITH+                    (ARITH)
        //                     OP_PLUS        ARITH+                    (ARITH)
        //PATTERN1       1     *              *                         (NOT_SET)
        checkSetExpType
        ( SqlTreeType.POSTFIXUNARYOP
        , os(SqlTokenType.NULL      , SqlTokenType.NOT_NULL  )
        , os(genCombs(os((SqlExpressionType)null)), genCombs(os((SqlExpressionType)null))) // any types will do
        , os(SqlExpressionType.BOOL , SqlExpressionType.BOOL )
        );
        checkSetExpType
        ( SqlTreeType.PREFIXUNARYOP
        , os(SqlTokenType.NOT                    , SqlTokenType.OP_MINUS                , SqlTokenType.OP_PLUS                 )
        , os(genCombs(os(SqlExpressionType.BOOL)), genCombs(os(SqlExpressionType.ARITH)), genCombs(os(SqlExpressionType.ARITH)))
        , os(SqlExpressionType.BOOL, SqlExpressionType.ARITH, SqlExpressionType.ARITH)
        );
        checkSetExpType
        ( SqlTreeType.PATTERN1
        , os((SqlTokenType)null       )              // null means any type will do
        , o1(genCombs(os((SqlExpressionType)null)))  // any type will do
        , os(SqlExpressionType.NOT_SET)
        );
    }

    @Test
    public void testVisit2() {
        //CONJUNCTION
        //DISJUNCTION    2     *              BOOL+   BOOL+             (BOOL)
        //BINARYOP       2     CMP_EQ
        //                     CMP_NEQ        BOOL+   BOOL+             (BOOL)
        //                                    STRING+ STRING+           (BOOL)
        //                                    ARITH+  ARITH+            (BOOL)
        //                     CMP_LT
        //                     CMP_LTEQ
        //                     CMP_GT
        //                     CMP_GTEQ       ARITH+  ARITH+            (BOOL)
        //                     LIKE
        //                     NOT_LIKE
        //                     IN
        //                     NOT_IN         STRING+ *                 (BOOL)
        //                     OP_DIV
        //                     OP_MULT
        //                     OP_PLUS
        //                     OP_MINUS       ARITH+  ARITH+            (ARITH)
        //PATTERN2       2     *              *       *                 (NOT_SET)
        checkSetExpType
        ( SqlTreeType.CONJUNCTION
        , os(SqlTokenType.NULL     )
        , o1(genCombs(os(SqlExpressionType.BOOL, SqlExpressionType.BOOL)))
        , os(SqlExpressionType.BOOL)
        );
        checkSetExpType
        ( SqlTreeType.DISJUNCTION
        , os(SqlTokenType.NULL     )
        , o1(genCombs(os(SqlExpressionType.BOOL, SqlExpressionType.BOOL)))
        , os(SqlExpressionType.BOOL)
        );
        checkSetExpType
        ( SqlTreeType.BINARYOP
        , os(SqlTokenType.CMP_EQ)
        , o1(genCombs( os(SqlExpressionType.BOOL, SqlExpressionType.BOOL)
                     , os(SqlExpressionType.STRING, SqlExpressionType.STRING)
                     , os(SqlExpressionType.ARITH, SqlExpressionType.ARITH)))
        , os(SqlExpressionType.BOOL)
        );
        checkSetExpType
        ( SqlTreeType.BINARYOP
        , os(SqlTokenType.CMP_NEQ)
        , o1(genCombs( os(SqlExpressionType.BOOL, SqlExpressionType.BOOL)
                     , os(SqlExpressionType.STRING, SqlExpressionType.STRING)
                     , os(SqlExpressionType.ARITH, SqlExpressionType.ARITH)))
        , os(SqlExpressionType.BOOL)
        );
        checkSetExpType
        ( SqlTreeType.BINARYOP
        , os(SqlTokenType.CMP_LT    , SqlTokenType.CMP_LTEQ  , SqlTokenType.CMP_GT    , SqlTokenType.CMP_GTEQ  )
        , os( genCombs(os(SqlExpressionType.ARITH, SqlExpressionType.ARITH))
            , genCombs(os(SqlExpressionType.ARITH, SqlExpressionType.ARITH))
            , genCombs(os(SqlExpressionType.ARITH, SqlExpressionType.ARITH))
            , genCombs(os(SqlExpressionType.ARITH, SqlExpressionType.ARITH)))
        , os(SqlExpressionType.BOOL , SqlExpressionType.BOOL , SqlExpressionType.BOOL , SqlExpressionType.BOOL )
        );
        checkSetExpType
        ( SqlTreeType.BINARYOP
        , os(SqlTokenType.LIKE       , SqlTokenType.NOT_LIKE   , SqlTokenType.IN         , SqlTokenType.NOT_IN     )
        , os( genCombs(os(SqlExpressionType.STRING, null))
            , genCombs(os(SqlExpressionType.STRING, null))
            , genCombs(os(SqlExpressionType.STRING, null))
            , genCombs(os(SqlExpressionType.STRING, null)))
        , os(SqlExpressionType.BOOL  , SqlExpressionType.BOOL  , SqlExpressionType.BOOL  , SqlExpressionType.BOOL  )
        );
        checkSetExpType
        ( SqlTreeType.BINARYOP
        , os(SqlTokenType.OP_DIV    , SqlTokenType.OP_MULT   , SqlTokenType.OP_PLUS   , SqlTokenType.OP_MINUS  )
        , os( genCombs(os(SqlExpressionType.ARITH, SqlExpressionType.ARITH))
            , genCombs(os(SqlExpressionType.ARITH, SqlExpressionType.ARITH))
            , genCombs(os(SqlExpressionType.ARITH, SqlExpressionType.ARITH))
            , genCombs(os(SqlExpressionType.ARITH, SqlExpressionType.ARITH)))
        , os(SqlExpressionType.ARITH, SqlExpressionType.ARITH, SqlExpressionType.ARITH, SqlExpressionType.ARITH)
        );
        checkSetExpType
        ( SqlTreeType.PATTERN2
        , os((SqlTokenType)null       )  // null means any type will do
        , o1(genCombs(os((SqlExpressionType)null, null)))  // any types will do
        , os(SqlExpressionType.NOT_SET)
        );
    }
    @Test
    public void testVisit3() {
        //TERNARYOP      3     BETWEEN
        //                     NOT_BETWEEN    ARITH+  ARITH+  ARITH+    (BOOL)
        checkSetExpType
        ( SqlTreeType.TERNARYOP
        , os(SqlTokenType.BETWEEN, SqlTokenType.NOT_BETWEEN)
        , os( genCombs(os(SqlExpressionType.ARITH, SqlExpressionType.ARITH, SqlExpressionType.ARITH))
            , genCombs(os(SqlExpressionType.ARITH, SqlExpressionType.ARITH, SqlExpressionType.ARITH)))
        , os(SqlExpressionType.BOOL, SqlExpressionType.BOOL)
        );
    }

    private void checkSetExpTypeForLeaves(boolean withIdents, SqlTreeType treeType, SqlTokenType[] tts, SqlExpressionType[] rets) {
        int resultIndex=0;
        for (SqlTokenType pre_tt: tts) {
            for (SqlTokenType tt : (pre_tt == null) ? SqlTokenType.values() : os(pre_tt)) { // null means any one will do, try them all
                for( PairSTSET tokenExpType : generateTokenExpTypePairs(withIdents, tt, rets[resultIndex])) {
                    Visitor<SqlTreeNode> visitor = (withIdents ? tsVisitorIdents : tsVisitorBasic);
                    assertVisitGood(treeType, tokenExpType.left(), NO_EXPRESSIONS, tokenExpType.right(), visitor);
                }
            }
            ++resultIndex;
        }
    }

    private static void assertVisitGood( SqlTreeType treeType, SqlToken token, SqlExpressionType[] argTypes, SqlExpressionType resultType
                                       , Visitor<SqlTreeNode> visitor
                                       ) {
        ArrayList<SqlTreeNode> children = new ArrayList<SqlTreeNode>();
        for (SqlExpressionType arg : argTypes) {
            children.add(sqlDummyTypedNode(arg));
        }
        SqlTreeNode stn = new SqlTreeNode(treeType, token);
        assertTrue(visitor.visitAfter(stn, children.toArray(new SqlTreeNode[children.size()])));
        assertEquals(resultType, stn.getExpValue().getType(), "Wrong expType for " + stn + " with children " + children);
    }

    private static SqlTreeNode sqlDummyTypedNode(SqlExpressionType arg) {
        SqlTreeNode tn = new SqlTreeNode(null, new SqlToken(SqlTokenType.WS, ""));
        tn.getExpValue().setType(arg);
        return tn;
    }

    private PairSTSET[] generateTokenExpTypePairs(boolean withIdents, SqlTokenType tt, SqlExpressionType sqlExpressionType) {
        if (tt == SqlTokenType.IDENT && sqlExpressionType == null) {
            if (withIdents) {
                ArrayList<PairSTSET> plist = new ArrayList<PairSTSET>();
                for(Entry<String, SqlExpressionType> e: idTypes.entrySet()) {
                    plist.add(PairSTSET.pair(new SqlToken(tt, e.getKey()), e.getValue()));
                }
                plist.add(PairSTSET.pair(new SqlToken(tt,"dummyIdent"), SqlExpressionType.ANY));
                return plist.toArray(new PairSTSET[plist.size()]);
            } else
                return os(PairSTSET.pair(new SqlToken(tt,"dummyIdent"), SqlExpressionType.ANY));
        } else
            return os(PairSTSET.pair(new SqlToken(tt,"no-value"), sqlExpressionType));
    }

    private void checkSetExpType(SqlTreeType treeType, SqlTokenType[] tts, SqlExpressionType[][][] aaaets, SqlExpressionType[] rets) {
        int resultIndex=0;
        for (SqlTokenType pre_tt: tts) {
            for (SqlTokenType tt : (pre_tt == null) ? SqlTokenType.values() : os(pre_tt)) { // null means any one will do, try them all
                for(TripleSTaSETSET tokenAExpTypeExpType : generateTokenExpTypeTriples(tt, aaaets[resultIndex], rets[resultIndex])) {
                    assertVisitGood(treeType, tokenAExpTypeExpType.first(), tokenAExpTypeExpType.second(), tokenAExpTypeExpType.third(), tsVisitorBasic);
                    assertVisitGood(treeType, tokenAExpTypeExpType.first(), tokenAExpTypeExpType.second(), tokenAExpTypeExpType.third(), tsVisitorIdents);
                }
            }
            ++resultIndex;
        }
    }

    private TripleSTaSETSET[] generateTokenExpTypeTriples(SqlTokenType tt, SqlExpressionType[][] validCombinations, SqlExpressionType sqlExpressionType) {
        List<TripleSTaSETSET> plist = new ArrayList<TripleSTaSETSET>();
        SqlToken rootToken = new SqlToken(tt, "no-value");
        for(SqlExpressionType[] args: validCombinations) {
            plist.add(new TripleSTaSETSET(rootToken, args, sqlExpressionType));
        }
        for(SqlExpressionType[] args: complement(validCombinations)) {
            plist.add(new TripleSTaSETSET(rootToken, args, SqlExpressionType.INVALID));
        }
        return plist.toArray(new TripleSTaSETSET[plist.size()]);
    }

    private SqlExpressionType[][] complement(SqlExpressionType[][] validCombinations) {
        // #validCombinations > 0
        // x,y : validCombinations | #x = #y
        Set<List<SqlExpressionType>> setOfCombs = new HashSet<List<SqlExpressionType>>();
        for (SqlExpressionType[] comb : validCombinations) {
            setOfCombs.add(listOf(comb));
        }

        Set<List<SqlExpressionType>> product = setProduct(validCombinations[0].length);
        product.removeAll(setOfCombs);

        SqlExpressionType[][] arrArr = new SqlExpressionType[product.size()][];
        int index = 0;
        for(List<SqlExpressionType> list : product) {
            arrArr[index++] = list.toArray(new SqlExpressionType[list.size()]);
        }
        return arrArr;
    }

    private List<SqlExpressionType> listOf(SqlExpressionType[] comb) {
        List<SqlExpressionType> list = new ArrayList<SqlExpressionType>(comb.length);
        for(SqlExpressionType set : comb) {
            list.add(set);
        }
        return list;
    }

    private Set<List<SqlExpressionType>> setProduct(int n) {
        // 0 ≤ n ≤ 3
        Set<List<SqlExpressionType>> product = new HashSet<List<SqlExpressionType>>();
        SqlExpressionType[] allVals = SqlExpressionType.values();
        switch (n) {
        case 0:
            product.add(listOf(new SqlExpressionType[0]));
            break;
        case 1:
            for (SqlExpressionType set : allVals) {
                product.add(listOf(os(set)));
            }
            break;
        case 2:
            for (SqlExpressionType set1 : allVals) {
                for (SqlExpressionType set2 : allVals) {
                    product.add(listOf(os(set1, set2)));
                }
            }
            break;
        case 3:
            for (SqlExpressionType set1 : allVals) {
                for (SqlExpressionType set2 : allVals) {
                    for (SqlExpressionType set3 : allVals) {
                        product.add(listOf(os(set1, set2, set3)));
                    }
                }
            }
            break;
        default:
            throw new IllegalArgumentException("cannot generate product set for n="+n);
        }
        return product;
    }

    private static SqlExpressionType[][] genCombs(SqlExpressionType[]... aaets) {
        List<SqlExpressionType> emptyArgList = new ArrayList<SqlExpressionType>();
        List<List<SqlExpressionType>> argLists = new ArrayList<List<SqlExpressionType>>();
        argLists.add(emptyArgList);
        List<List<SqlExpressionType>> list = new ArrayList<List<SqlExpressionType>>();
        for(SqlExpressionType[] aets : aaets) {
            list.addAll(generateArgumentCombinationLists(0, aets, argLists));
        }
        SqlExpressionType[][] result = new SqlExpressionType[list.size()][];
        {   int index = 0;
            for(List<SqlExpressionType> argList : list) {
                result[index++] = argList.toArray(new SqlExpressionType[argList.size()]);
            }
        }
        return result;
    }

    private static List<List<SqlExpressionType>> generateArgumentCombinationLists(int start, SqlExpressionType[] aets, List<List<SqlExpressionType>> lists) {
        if (start < aets.length) {
            List<List<SqlExpressionType>> newLists = new ArrayList<List<SqlExpressionType>>();
            for(List<SqlExpressionType> alist : lists) {
                SqlExpressionType[] ets = (aets[start] == null ? SqlExpressionType.values()
                                                               : os(aets[start], SqlExpressionType.ANY));
                for(SqlExpressionType et: ets) {
                    List<SqlExpressionType> copyList = new ArrayList<SqlExpressionType>(alist);
                    copyList.add(et);
                    newLists.add(copyList);
                }
            }
            return generateArgumentCombinationLists(start+1, aets, newLists);
        }
        return lists;
    }

    private static class PairSTSET extends Pair<SqlToken, SqlExpressionType> {
        private PairSTSET(SqlToken l, SqlExpressionType r) { super(l, r); }
        public static PairSTSET pair(SqlToken l, SqlExpressionType r) { return new PairSTSET(l,r); }
    };
    private static class TripleSTaSETSET extends Triple<SqlToken, SqlExpressionType[], SqlExpressionType> {
        private TripleSTaSETSET(SqlToken f, SqlExpressionType[] s, SqlExpressionType t) { super(f, s, t); }
    };

    @SuppressWarnings("unchecked")
    private static <Obj> Obj[] os(Obj... objs) { return objs; }
    // os doesn't work as expected when Obj is already an Array and there is a single argument. Hence following special case:
    private static SqlExpressionType[][][] o1(SqlExpressionType[][] aaset) { SqlExpressionType[][][] aaaset = new SqlExpressionType[1][][]; aaaset[0] = aaset; return aaaset; }
}
