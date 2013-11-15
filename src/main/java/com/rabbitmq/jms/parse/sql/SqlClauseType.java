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
    // the definition of a term, unless it has been defined lexically beforehand (!) so I use strings instead!!

    // Terminals are simply proxies for the SqlTokenType type.

    // non-terminals:
    expression( nt(COLLAPSE1,      /* -> */ "or_expr") ),
    or_expr   ( nt(DISJUNCTION,    /* -> */ "and_expr OR or_expr")
              , nt(COLLAPSE1,      /*  | */ "and_expr") ),
    and_expr  ( nt(CONJUNCTION,    /* -> */ "not_expr AND and_expr")
              , nt(COLLAPSE1,      /*  |  */ "not_expr") ),
    not_expr  ( nt(PREFIXUNARYOP,  /* ->  */ "NOT cmp_expr")
              , nt(COLLAPSE1,      /*  |  */ "cmp_expr") ),
    cmp_expr  ( nt(BINARYOP,       /* ->  */ "arith_expr op_cmp arith_expr")
              , nt(TERNARYOP,      /*  |  */ "arith_expr BETWEEN arith_expr AND arith_expr")
              , nt(TERNARYOP,      /*  |  */ "arith_expr NOT_BETWEEN arith_expr AND arith_expr")
              , nt(COLLAPSE1,      /*  |  */ "arith_expr") ),
    op_cmp    ( nt(LEAF,           /* ->  */ "CMP_EQ")
              , nt(LEAF,           /*  |  */ "CMP_NEQ")
              , nt(LEAF,           /*  |  */ "CMP_LTEQ")
              , nt(LEAF,           /*  |  */ "CMP_GTEQ")
              , nt(LEAF,           /*  |  */ "CMP_LT")
              , nt(LEAF,           /*  |  */ "CMP_GT") ),
    op_plus   ( nt(LEAF,           /* ->  */ "OP_PLUS")
              , nt(LEAF,           /*  |  */ "OP_MINUS") ),
    op_mult   ( nt(LEAF,           /* ->  */ "OP_MULT")
              , nt(LEAF,           /*  |  */ "OP_DIV") ),
    arith_expr( nt(COLLAPSE1,      /* ->  */ "plus_expr") ),
    plus_expr ( nt(BINARYOP,       /* ->  */ "mult_expr op_plus plus_expr")
              , nt(COLLAPSE1,      /*  |  */ "mult_expr") ),
    mult_expr ( nt(BINARYOP,       /* ->  */ "sign_expr op_mult mult_expr")
              , nt(COLLAPSE1,      /*  |  */ "sign_expr") ),
    sign_expr ( nt(PREFIXUNARYOP,  /* ->  */ "op_plus sign_expr")
              , nt(COLLAPSE1,      /*  |  */ "simple") ),
    simple    ( nt(COLLAPSE2,      /* ->  */ "LP expression RP")
              , nt(LEAF,           /*  |  */ "TRUE")
              , nt(LEAF,           /*  |  */ "FALSE")
              , nt(LEAF,           /*  |  */ "STRING")
              , nt(COLLAPSE1,      /*  |  */ "number")
              , nt(POSTFIXUNARYOP, /*  |  */ "IDENT NULL")
              , nt(POSTFIXUNARYOP, /*  |  */ "IDENT NOT_NULL")
              , nt(BINARYOP,       /*  |  */ "IDENT IN stringlist")
              , nt(BINARYOP,       /*  |  */ "IDENT NOT_IN stringlist")
              , nt(BINARYOP,       /*  |  */ "IDENT LIKE pattern")
              , nt(BINARYOP,       /*  |  */ "IDENT NOT_LIKE pattern")
              , nt(LEAF,           /*  |  */ "IDENT") ),
    stringlist( nt(COLLAPSE2,      /* ->  */ "LP strings RP") ),
    strings   ( nt(BINARYOP,       /* ->  */ "STRING COMMA strings")
              , nt(LIST,           /*  |  */ "STRING") ),
    pattern   ( nt(PATTERN2,       /* ->  */ "STRING ESCAPE STRING")
              , nt(PATTERN1,       /*  |  */ "STRING") ),
    number    ( nt(LEAF,           /* ->  */ "HEX")
              , nt(LEAF,           /*  |  */ "FLOAT")
              , nt(LEAF,           /*  |  */ "INT") ),

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
            SqlClauseType[] alt = this.clauses[i].clauses();
            SqlParseTree[] children = new SqlParseTree[alt.length];
            SqlParseTree child = null;
            for (int childIndex = 0; childIndex < alt.length; ++childIndex) {
                child = alt[childIndex].parse(ts);   // recursive call
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

    private static final CP nt(SqlTreeNodeType tnt, String ts) { return new CP(tnt, ts); }

    private static class CP extends Pair<SqlTreeNodeType, String> {
        public CP(SqlTreeNodeType l, String r) { super(l, r); }
        public SqlClauseType[] clauses() {
            return aValueOf(this.right());
        }
    };

    private static SqlClauseType[] aValueOf(String s) {
        String[] ss = s.split(" ");
        SqlClauseType[] scts = new SqlClauseType[ss.length];
        for (int i=0; i<ss.length; ++i) {
            scts[i] = SqlClauseType.valueOf(ss[i]);
        }
        return scts;
    }
}
