package com.rabbitmq.jms.parse.sql;

import static com.rabbitmq.jms.parse.sql.SqlTreeNodeType.BINARYOP;
import static com.rabbitmq.jms.parse.sql.SqlTreeNodeType.COLLAPSE1;
import static com.rabbitmq.jms.parse.sql.SqlTreeNodeType.COLLAPSE2;
import static com.rabbitmq.jms.parse.sql.SqlTreeNodeType.CONJUNCTION;
import static com.rabbitmq.jms.parse.sql.SqlTreeNodeType.DISJUNCTION;
import static com.rabbitmq.jms.parse.sql.SqlTreeNodeType.LEAF;
import static com.rabbitmq.jms.parse.sql.SqlTreeNodeType.LIST;
import static com.rabbitmq.jms.parse.sql.SqlTreeNodeType.PATTERN1;
import static com.rabbitmq.jms.parse.sql.SqlTreeNodeType.PATTERN2;
import static com.rabbitmq.jms.parse.sql.SqlTreeNodeType.POSTFIXUNARYOP;
import static com.rabbitmq.jms.parse.sql.SqlTreeNodeType.PREFIXUNARYOP;
import static com.rabbitmq.jms.parse.sql.SqlTreeNodeType.TERNARYOP;

import com.rabbitmq.jms.parse.Multiples.Pair;
import com.rabbitmq.jms.parse.TokenStream;

/**
 * This describes the SQL grammar in terms of the tokens {@link SqlTokenType}.
 * <p>
 * For each grammar clause, we match the alternatives in turn. For each alternative, we match the nodes in sequence
 * until we reach the end of the sequence. If we reach the end with a full match we return the resulting node
 * constructed from the sequence of nodes matched, otherwise we go to the next alternative. If we run out of
 * alternatives, and none of them matched, we return NO_MATCH.
 * </p>
 * <p>
 * The top-level expression should check, after matching correctly, that we have consumed all the tokens from the input
 * stream. If so, then we are good to go. If not, the top-level expression has failed to match everything, and we can report the
 * next few tokens to indicate where the failure occurred.
 * </p>
 * <p>
 * The <i>order</i> of the grammar terms is important, we match the terms with more tokens first, where the prefixes are the same.
 * </p>
 * <p>
 * The grammar, where the uppercase terms are terminals ({@link SqlToken}s) and the lowercase terms are non-terminals, is defined
 * in the line comments to the right of the enum value declarations.  Each clause of the grammar corresponds to an alternative in
 * the declaration.
 * Each non-terminal clause gets a production action (SqlTreeNodeType) and a list of other clauses (terminal or non-terminal), which are
 * matched in sequence to match the alternative.  The parser algorithm treats the alternatives for a single clause type
 * in sequence and accepts the first one that matches.
 * </p>
 * <pre>
 * expression   -> or_expr
 * or_expr      -> and_expr OR or_expr
 *               | and_expr
 * and_expr     -> not_expr AND and_expr
 *               | not_expr
 * not_expr     -> cmp_expr
 *               | NOT cmp_expr
 * cmp_expr     -> arith_expr op_cmp arith_expr
 *               | arith_expr BETWEEN arith_expr AND arith_expr
 *               | arith_expr NOT_BETWEEN arith_expr AND arith_expr
 *               | arith_expr
 * op_cmp       -> CMP_EQ
 *               | CMP_NEQ
 *               | CMP_LTEQ
 *               | CMP_GTEQ
 *               | CMP_LT
 *               | CMP_GT
 * op_plus      -> OP_PLUS
 *               | OP_MINUS
 * op_mult      -> OP_MULT
 *               | OP_DIV
 * arith_expr   -> plus_expr
 * plus_expr    -> mult_expr op_plus plus_expr
 *               | mult_expr
 * mult_expr    -> sign_expr op_mult mult_expr
 *               | sign_expr
 * sign_expr    -> op_plus sign_expr
 *               | simple
 * simple       -> LP expression RP
 *               | TRUE
 *               | FALSE
 *               | STRING
 *               | number
 *               | IDENT IS_NULL
 *               | IDENT IS_NOT_NULL
 *               | IDENT IN stringlist
 *               | IDENT NOT_IN stringlist
 *               | IDENT LIKE pattern
 *               | IDENT NOT_LIKE pattern
 *               | IDENT
 * stringlist   -> LP strings RP
 * strings      -> STRING COMMA strings
 *               | STRING
 * pattern      -> STRING ESCAPE STRING
 *               | STRING
 * number       -> HEX
 *               | FLOAT
 *               | INT
 * </pre>
 * <p>
 * This grammar is captured in this enumerated type, by defining a production for every terminal and non-terminal in this grammar.
 * The parse tree is constructed using each {@link SqlTreeNodeType} as a node in the tree, and the children being the parsed children.
 * Each terminal {@link SqlTokenType} is a LEAF node in the tree and the token value is the value of the node.
 * </p>
 */
public enum SqlClauseType {
    // The root of the grammar is <code>expression</code>, defined in <code>ROOT</code>.

    // Non-terminals have an array of alternatives; each alternative is an SqlTreeNodeType and an SqlClauseType array.
    // I would have liked to use SqlClauseType explicitly, but Java doesn't let me use an enumerated type term in
    // the definition of a term, unless it has been defined lexically beforehand (!) so I use indexes instead (yuch).

    // Terminals are simply proxies for the SqlTokenType type.

    // non-terminals:
    expression                                  // index 00
              ( nt(COLLAPSE1,1) ),              //             -> or_expr
    or_expr                                     // index 01
              ( nt(DISJUNCTION,2,26,1)          //             -> and_expr OR or_expr
              , nt(COLLAPSE1,2) ),              //              | and_expr
    and_expr                                    // index 02
              ( nt(CONJUNCTION,3,25,2)          //             -> not_expr AND and_expr
              , nt(COLLAPSE1,3) ),              //              | not_expr
    not_expr                                    // index 03
              ( nt(PREFIXUNARYOP,27,4)          //             -> NOT cmp_expr
              , nt(COLLAPSE1,4) ),              //              | cmp_expr
    cmp_expr                                    // index 04
              ( nt(BINARYOP,8,5,8)              //             -> arith_expr op_cmp arith_expr
              , nt(TERNARYOP,8,23,8,25,8)       //              | arith_expr BETWEEN arith_expr AND arith_expr
              , nt(TERNARYOP,8,24,8,25,8)       //              | arith_expr NOT_BETWEEN arith_expr AND arith_expr
              , nt(COLLAPSE1,8) ),              //              | arith_expr
    op_cmp                                      // index 05
              ( nt(LEAF,31)                     //             -> CMP_EQ
              , nt(LEAF,32)                     //              | CMP_NEQ
              , nt(LEAF,33)                     //              | CMP_LTEQ
              , nt(LEAF,34)                     //              | CMP_GTEQ
              , nt(LEAF,35)                     //              | CMP_LT
              , nt(LEAF,36) ),                  //              | CMP_GT
    op_plus                                     // index 06
              ( nt(LEAF,37)                     //             -> OP_PLUS
              , nt(LEAF,38) ),                  //              | OP_MINUS
    op_mult                                     // index 07
              ( nt(LEAF,39)                     //             -> OP_MULT
              , nt(LEAF,40) ),                  //              | OP_DIV
    arith_expr                                  // index 08
              ( nt(COLLAPSE1,9) ),              //             -> plus_expr
    plus_expr                                   // index 09
              ( nt(BINARYOP,10,6,9)             //             -> mult_expr op_plus plus_expr
              , nt(COLLAPSE1,10) ),             //              | mult_expr
    mult_expr                                   // index 10
              ( nt(BINARYOP,11,7,10)            //             -> sign_expr op_mult mult_expr
              , nt(COLLAPSE1,11) ),             //              | sign_expr
    sign_expr                                   // index 11
              ( nt(PREFIXUNARYOP,6,11)          //             -> op_plus sign_expr
              , nt(COLLAPSE1,12) ),             //              | simple
    simple                                      // index 12
              ( nt(COLLAPSE2,42,0,43)           //             -> LP expression RP
              , nt(LEAF,29)                     //              | TRUE
              , nt(LEAF,30)                     //              | FALSE
              , nt(LEAF,45)                     //              | STRING
              , nt(COLLAPSE1,16)                //              | number
              , nt(POSTFIXUNARYOP,44,21)        //              | IDENT IS_NULL
              , nt(POSTFIXUNARYOP,44,22)        //              | IDENT IS_NOT_NULL
              , nt(BINARYOP,44,19,13)           //              | IDENT IN stringlist
              , nt(BINARYOP,44,20,13)           //              | IDENT NOT_IN stringlist
              , nt(BINARYOP,44,17,15)           //              | IDENT LIKE pattern
              , nt(BINARYOP,44,18,15)           //              | IDENT NOT_LIKE pattern
              , nt(LEAF,44) ),                  //              | IDENT
    stringlist                                  // index 13
              ( nt(COLLAPSE2,42,14,43)),        //             -> LP strings RP
    strings                                     // index 14
              ( nt(BINARYOP,45,41,14)           //             -> STRING COMMA strings
              , nt(LIST,45) ),                  //              | STRING
    pattern                                     // index 15
              ( nt(PATTERN2,45,28,45)           //             -> STRING ESCAPE STRING
              , nt(PATTERN1,45) ),              //              | STRING
    number                                      // index 16
              ( nt(LEAF,46)                     //             -> HEX
              , nt(LEAF,47)                     //              | FLOAT
              , nt(LEAF,48) ),                  //              | INT

    // terminals:
    LIKE        (SqlTokenType.LIKE       ),     // index 17
    NOT_LIKE    (SqlTokenType.NOT_LIKE   ),     // index 18
    IN          (SqlTokenType.IN         ),     // index 19
    NOT_IN      (SqlTokenType.NOT_IN     ),     // index 20
    NULL        (SqlTokenType.NULL       ),     // index 21
    NOT_NULL    (SqlTokenType.NOT_NULL   ),     // index 22
    BETWEEN     (SqlTokenType.BETWEEN    ),     // index 23
    NOT_BETWEEN (SqlTokenType.NOT_BETWEEN),     // index 24
    AND         (SqlTokenType.AND        ),     // index 25
    OR          (SqlTokenType.OR         ),     // index 26
    NOT         (SqlTokenType.NOT        ),     // index 27
    ESCAPE      (SqlTokenType.ESCAPE     ),     // index 28
    TRUE        (SqlTokenType.TRUE       ),     // index 29
    FALSE       (SqlTokenType.FALSE      ),     // index 30
    CMP_EQ      (SqlTokenType.CMP_EQ     ),     // index 31
    CMP_NEQ     (SqlTokenType.CMP_NEQ    ),     // index 32
    CMP_LTEQ    (SqlTokenType.CMP_LTEQ   ),     // index 33
    CMP_GTEQ    (SqlTokenType.CMP_GTEQ   ),     // index 34
    CMP_LT      (SqlTokenType.CMP_LT     ),     // index 35
    CMP_GT      (SqlTokenType.CMP_GT     ),     // index 36
    OP_PLUS     (SqlTokenType.OP_PLUS    ),     // index 37
    OP_MINUS    (SqlTokenType.OP_MINUS   ),     // index 38
    OP_MULT     (SqlTokenType.OP_MULT    ),     // index 39
    OP_DIV      (SqlTokenType.OP_DIV     ),     // index 40
    COMMA       (SqlTokenType.COMMA      ),     // index 41
    LP          (SqlTokenType.LP         ),     // index 42
    RP          (SqlTokenType.RP         ),     // index 43
    IDENT       (SqlTokenType.IDENT      ),     // index 44
    STRING      (SqlTokenType.STRING     ),     // index 45
    FLOAT       (SqlTokenType.FLOAT      ),     // index 46
    INT         (SqlTokenType.INT        ),     // index 47
    HEX         (SqlTokenType.HEX        ),     // index 48
    ;
    public static final SqlClauseType ROOT = expression;

    private final SqlTokenType tokenType;

    private final CP[] clauses;

    /**
     * Non-terminal clause constructor
     * @param clauses - (array of) alternatives
     */
    SqlClauseType(CP ... clauses) {
        this.tokenType = null;
        this.clauses = clauses;
    }

    /**
     * Terminal clause constructor
     * @param tokenType - terminal token type
     */
    SqlClauseType(SqlTokenType tokenType) {
        this.tokenType = tokenType;
        this.clauses = null;
    }

    /**
     * Recursive descent parser for this clause type
     * @param ts - token stream to parse starting at this clause type
     * @return <code>null</code> if there is no clause of this type next in the stream,
     *   otherwise returns the constructed tree node and the stream moved on.
     */
    SqlParseTree parse(TokenStream<SqlToken, Integer> ts) {
        int pos = ts.position(); // Entry position in token stream
        if (this.isTerminal()) {
            SqlToken tok = ts.getNext();
            if (tok == null || tok.type() != this.tokenType) {
                ts.reset(pos); // put token stream back to token point
                return null;   // report no token of the right type
            }
            return new SqlParseTree(new SqlTreeNode(LEAF, tok));  // return single node tree
        }

        // non-terminal (recurse):
        for (int i=0; i<this.clauses.length; ++i) { // for each alternative clause
            ts.reset(pos);
            SqlTreeNodeType treeNodeType = this.clauses[i].left();
            int[] intalt = this.clauses[i].right();
            SqlParseTree[] children = new SqlParseTree[intalt.length];
            SqlParseTree child = null;
            for (int childIndex = 0; childIndex < intalt.length; ++childIndex) {
                child = SqlClauseType.values()[intalt[childIndex]].parse(ts);   // recursive call
                if (child == null) break;
                children[childIndex] = child;
            }
            if (child != null) {
                return treeNodeType.tree(children);
            }
        }
        return null;
    }

    SqlTokenType tokenType() { return this.tokenType; }

    boolean isTerminal() { return this.tokenType != null; }

    private static final CP nt(SqlTreeNodeType tnt, int ... ts) { return new CP(tnt, ts); }

    private static class CP extends Pair<SqlTreeNodeType, int[]> { public CP(SqlTreeNodeType l, int[] r) { super(l, r); } };

}
