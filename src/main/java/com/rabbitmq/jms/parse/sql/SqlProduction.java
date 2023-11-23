/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse.sql;

import static com.rabbitmq.jms.parse.sql.SqlTreeType.BINARYOP;
import static com.rabbitmq.jms.parse.sql.SqlTreeType.COLLAPSE1;
import static com.rabbitmq.jms.parse.sql.SqlTreeType.COLLAPSE2;
import static com.rabbitmq.jms.parse.sql.SqlTreeType.CONJUNCTION;
import static com.rabbitmq.jms.parse.sql.SqlTreeType.DISJUNCTION;
import static com.rabbitmq.jms.parse.sql.SqlTreeType.JOINLIST;
import static com.rabbitmq.jms.parse.sql.SqlTreeType.LEAF;
import static com.rabbitmq.jms.parse.sql.SqlTreeType.LIST;
import static com.rabbitmq.jms.parse.sql.SqlTreeType.PATTERN1;
import static com.rabbitmq.jms.parse.sql.SqlTreeType.PATTERN2;
import static com.rabbitmq.jms.parse.sql.SqlTreeType.POSTFIXUNARYOP;
import static com.rabbitmq.jms.parse.sql.SqlTreeType.PREFIXUNARYOP;
import static com.rabbitmq.jms.parse.sql.SqlTreeType.TERNARYOP;

import com.rabbitmq.jms.parse.Multiples.Pair;
import com.rabbitmq.jms.parse.TokenStream;

/**
 * This describes the SQL grammar in terms of the tokens {@link SqlTokenType}.
 * <p>
 * For each grammar production, we match the alternatives in turn. For each alternative, we match the terms in sequence
 * until we reach the end of the sequence. If we reach the end with all matched we return the resulting tree
 * constructed from the sequence of terms matched (using a SqlTreeNodeType for that purpose), otherwise we go to the next alternative.
 * If we run out of alternatives, and none of them matched, we return <code>null</code>.
 * </p>
 * <p>
 * The top-level expression should check, after matching correctly, that we have consumed all the tokens from the input
 * stream. If so, then we are good to go. If not, the top-level expression has failed to match everything, and we can report the
 * next few tokens to indicate where the failure occurred.
 * </p>
 * <p>
 * The <i>order</i> of a production’s alternatives is important, we match those with more tokens first, where the prefixes are the same.
 * </p>
 * <p>
 * The grammar, where the uppercase terms are terminals ({@link SqlTokenType}s) and the lowercase terms are non-terminals,
 * is completely defined by the alternatives in the productions declared below.  For example:
 * <pre>
 * or_expr      -> and_expr OR or_expr
 *               | and_expr
 * </pre>
 * corresponds to the production declaration:
 * <pre>
 * or_expr   ( cAlt( "and_expr OR or_expr"                              , DISJUNCTION    )
 *           , cAlt( "and_expr"                                         , COLLAPSE1      ) ),
 * </pre>
 * where each alternative is coded. (Because Java will not allow pre-reference of enum constants in enum definitions we cannot
 * use the productions directly but must encode them (in this case as an array of strings); they are decoded during parsing.)
 * </p>
 * <p>
 * Each alternative gets a sequence of productions (terminal or non-terminal) and a production action ({@link SqlTreeType}); the sequence
 * is matched in order to match the alternative.  The parser algorithm (for each production) treats the alternatives
 * in declared order and accepts the first one that matches fully.
 * </p>
 * <p>
 * Each production {@link SqlProduction} knows how to parse itself. This is the algorithm encoded in {@link #parse()} which recursively
 * calls each potential sub-expression to parse a sequence.  The terminal nodes know how to parse themselves (they are just single tokens),
 * and progress has to be made (in the token sequence) so the recursion will terminate eventually, provided the grammar is well-defined.
 * </p>
 * <p>
 * The parse tree is constructed using the {@link SqlTreeType} for a matched alternative (or LEAF in the case of a terminal).  The
 * parsed terms of the alternative are passed to the {@link SqlTreeType} to construct the tree of this type.
 * Each terminal {@link SqlTokenType} is a LEAF in the tree and the token value is the value of the node.
 * </p>
 * <p>
 * Non-terminals are associated with an array of coded alternatives; each coded alternative is an {@link SqlTreeType} and a {@link String} array.
 * Each string in the array is the name of a {@link SqlProduction}.
 * </p>
 * <note>
 * I would have liked to use {@link SqlProduction} explicitly, but Java doesn't let me use an enumerated type term in
 * the definition of a term, unless it has been defined lexically beforehand (!) so I use strings instead!!
 * </note>
 * <p>
 * Terminals are simply proxies for a {@link SqlTokenType} and have no alternatives.
 * </p>
 * <p>
 * The root of the grammar is <i><code>expression</code></i>, exposed in <code>ROOT</code>.
 * </p>
 */
enum SqlProduction {
    // non-terminals: grammar_alternative_______________________________   SqlTreeType
    expression( cAlt( "or_expr"                                          , COLLAPSE1      ) ),
    or_expr   ( cAlt( "and_expr OR or_expr"                              , DISJUNCTION    )
              , cAlt( "and_expr"                                         , COLLAPSE1      ) ),
    and_expr  ( cAlt( "not_expr AND and_expr"                            , CONJUNCTION    )
              , cAlt( "not_expr"                                         , COLLAPSE1      ) ),
    not_expr  ( cAlt( "NOT cmp_expr"                                     , PREFIXUNARYOP  )
              , cAlt( "cmp_expr"                                         , COLLAPSE1      ) ),
    cmp_expr  ( cAlt( "arith_expr op_cmp arith_expr"                     , BINARYOP       )
              , cAlt( "arith_expr BETWEEN arith_expr AND arith_expr"     , TERNARYOP      )
              , cAlt( "arith_expr NOT_BETWEEN arith_expr AND arith_expr" , TERNARYOP      )
              , cAlt( "arith_expr"                                       , COLLAPSE1      ) ),
    op_cmp    ( cAlt( "CMP_EQ"                                           , LEAF           )
              , cAlt( "CMP_NEQ"                                          , LEAF           )
              , cAlt( "CMP_LTEQ"                                         , LEAF           )
              , cAlt( "CMP_GTEQ"                                         , LEAF           )
              , cAlt( "CMP_LT"                                           , LEAF           )
              , cAlt( "CMP_GT"                                           , LEAF           ) ),
    op_plus   ( cAlt( "OP_PLUS"                                          , LEAF           )
              , cAlt( "OP_MINUS"                                         , LEAF           ) ),
    op_mult   ( cAlt( "OP_MULT"                                          , LEAF           )
              , cAlt( "OP_DIV"                                           , LEAF           ) ),
    arith_expr( cAlt( "plus_expr"                                        , COLLAPSE1      ) ),
    plus_expr ( cAlt( "mult_expr op_plus plus_expr"                      , BINARYOP       )
              , cAlt( "mult_expr"                                        , COLLAPSE1      ) ),
    mult_expr ( cAlt( "sign_expr op_mult mult_expr"                      , BINARYOP       )
              , cAlt( "sign_expr"                                        , COLLAPSE1      ) ),
    sign_expr ( cAlt( "op_plus sign_expr"                                , PREFIXUNARYOP  )
              , cAlt( "simple"                                           , COLLAPSE1      ) ),
    simple    ( cAlt( "LP expression RP"                                 , COLLAPSE2      )
              , cAlt( "TRUE"                                             , LEAF           )
              , cAlt( "FALSE"                                            , LEAF           )
              , cAlt( "STRING"                                           , LEAF           )
              , cAlt( "number"                                           , COLLAPSE1      )
              , cAlt( "IDENT NULL"                                       , POSTFIXUNARYOP )
              , cAlt( "IDENT NOT_NULL"                                   , POSTFIXUNARYOP )
              , cAlt( "IDENT IN stringlist"                              , BINARYOP       )
              , cAlt( "IDENT NOT_IN stringlist"                          , BINARYOP       )
              , cAlt( "IDENT LIKE pattern"                               , BINARYOP       )
              , cAlt( "IDENT NOT_LIKE pattern"                           , BINARYOP       )
              , cAlt( "IDENT"                                            , LEAF           ) ),
    stringlist( cAlt( "LP strings RP"                                    , COLLAPSE2      ) ),
    strings   ( cAlt( "STRING COMMA strings"                             , JOINLIST       )
              , cAlt( "STRING"                                           , LIST           ) ),
    pattern   ( cAlt( "STRING ESCAPE STRING"                             , PATTERN2       )
              , cAlt( "STRING"                                           , PATTERN1       ) ),
    number    ( cAlt( "HEX"                                              , LEAF           )
              , cAlt( "FLOAT"                                            , LEAF           )
              , cAlt( "INT"                                              , LEAF           ) ),

    // terminals:
    LIKE        (SqlTokenType.LIKE       ),
    NOT_LIKE    (SqlTokenType.NOT_LIKE   ),
    IN          (SqlTokenType.IN         ),
    NOT_IN      (SqlTokenType.NOT_IN     ),
    NULL        (SqlTokenType.NULL       ),
    NOT_NULL    (SqlTokenType.NOT_NULL   ),
    BETWEEN     (SqlTokenType.BETWEEN    ),
    NOT_BETWEEN (SqlTokenType.NOT_BETWEEN),
    AND         (SqlTokenType.AND        ),
    OR          (SqlTokenType.OR         ),
    NOT         (SqlTokenType.NOT        ),
    ESCAPE      (SqlTokenType.ESCAPE     ),
    TRUE        (SqlTokenType.TRUE       ),
    FALSE       (SqlTokenType.FALSE      ),
    CMP_EQ      (SqlTokenType.CMP_EQ     ),
    CMP_NEQ     (SqlTokenType.CMP_NEQ    ),
    CMP_LTEQ    (SqlTokenType.CMP_LTEQ   ),
    CMP_GTEQ    (SqlTokenType.CMP_GTEQ   ),
    CMP_LT      (SqlTokenType.CMP_LT     ),
    CMP_GT      (SqlTokenType.CMP_GT     ),
    OP_PLUS     (SqlTokenType.OP_PLUS    ),
    OP_MINUS    (SqlTokenType.OP_MINUS   ),
    OP_MULT     (SqlTokenType.OP_MULT    ),
    OP_DIV      (SqlTokenType.OP_DIV     ),
    COMMA       (SqlTokenType.COMMA      ),
    LP          (SqlTokenType.LP         ),
    RP          (SqlTokenType.RP         ),
    IDENT       (SqlTokenType.IDENT      ),
    STRING      (SqlTokenType.STRING     ),
    FLOAT       (SqlTokenType.FLOAT      ),
    INT         (SqlTokenType.INT        ),
    HEX         (SqlTokenType.HEX        ),
    ;
    public static final SqlProduction ROOT = expression;

    private final SqlTokenType tokenType;
    private final CodedAlternative[] codedAlts;

    /**
     * Constructor for non-terminals
     * @param alts - (array of) alternatives
     */
    SqlProduction(CodedAlternative ... alts) {
        this.tokenType = null;
        this.codedAlts = alts;
    }

    /**
     * Constructor for terminals
     * @param tokenType - terminal token type
     */
    SqlProduction(SqlTokenType tokenType) {
        this.tokenType = tokenType;
        this.codedAlts = null;
    }

    /**
     * Recursive descent parser for a particular production.
     * @param ts - token stream to parse
     * @return <code>null</code> if this production cannot be (fully) matched immediately next in the stream,
     *   otherwise returns the constructed tree for this production with the stream set past the tokens used for it.
     */
    SqlParseTree parse(TokenStream<SqlToken, Integer> ts) {
        if (this.tokenType != null) {
            // terminal:
            if (isTokenRightType(ts.readToken(), this.tokenType))
                return new SqlParseTree(new SqlTreeNode(LEAF, ts.getNext()));  // return single node tree
        } else {
            // non-terminal:
            int startPosition = ts.position();
            AlternativeParser altParser = new AlternativeParser(ts);

            // Match each subsequent alternative until one fully matches
            for (CodedAlternative codedAlt : this.codedAlts) {
                if (altParser.match(codedAlt.alternative())) {
                    return codedAlt.treeType().tree(altParser.getMatchedChildren());
                }
            }
            // We drop through if none of the alternatives matched fully.
            ts.reset(startPosition);
        }
        return null;
    }

    private static final class AlternativeParser {

        private static final SqlProduction[] EMPTY_ALTERNATIVE = new SqlProduction[0];
        private static final SqlParseTree[]  EMPTY_SUBTREES    = new SqlParseTree[0];

        private final TokenStream<SqlToken, Integer> tokenStream;
        private final int startPosition;

        private SqlProduction[] alternative;
        private SqlParseTree[] subtrees;
        private int numberMatched;
        // invariant: alternative.length == subtrees.length
        //            subtrees[i] is the parsed tree of alternative[i], for i in 0..numberMatched-1
        //            tokenStream from startPosition to tokenStream.position() contains the tokens which parse to subtrees[0..numberMatched-1]

        AlternativeParser(TokenStream<SqlToken, Integer> tokenStream) {
            this.tokenStream = tokenStream;
            this.startPosition = tokenStream.position();

            this.alternative = EMPTY_ALTERNATIVE;
            this.subtrees = EMPTY_SUBTREES;
            this.numberMatched = 0;
        }

        boolean match(SqlProduction[] alt) {
            setNewAlternative(alt);

            for (int term=this.numberMatched; term < this.subtrees.length; ++term, ++this.numberMatched) {
                this.subtrees[term] = this.alternative[term].parse(this.tokenStream);
                if (null == this.subtrees[term]) break;
            }
            return isFullyMatched();
        }

        private void setNewAlternative(SqlProduction[] alt) {
            SqlParseTree[] newSubtrees = new SqlParseTree[alt.length];

            if (canUsePreviousMatches(alt)) {
                System.arraycopy(this.subtrees, 0, newSubtrees, 0, this.numberMatched);
            } else {
                this.numberMatched = 0;
                this.tokenStream.reset(this.startPosition);
            }
            this.alternative = alt;
            this.subtrees = newSubtrees;
        }

        private boolean canUsePreviousMatches(SqlProduction[] alt) {
            if (this.numberMatched > alt.length) return false;

            for (int i=0; i < this.numberMatched; ++i) {
                if (alt[i] != this.alternative[i]) return false;
            }
            return true;
        }

        private boolean isFullyMatched() {
            return this.numberMatched == this.subtrees.length;
        }

        SqlParseTree[] getMatchedChildren() {
            return isFullyMatched() ? this.subtrees : null;
        }
    }

    /** @return <b><code>true</code></b> if <code>tok</code> is a {@link SqlToken} of the correct type. */
    private static final boolean isTokenRightType(SqlToken tok, SqlTokenType tokType) {
        return (tok != null && tok.type() == tokType);
    }

    /**
     * Non-terminal coded alternative (array of these used to declare non-terminals)
     * @param terms - string of production names
     * @param treeNodeType - type of tree to construct if matched
     * @return a coded grammar alternative for a production
     */
    private static final CodedAlternative cAlt(String terms, SqlTreeType treeNodeType) {
        return new CodedAlternative(treeNodeType, terms.split(" "));
    }

    /**
     * An alternative is coded as a specialisation of a simple {@link Pair} which initially just stores the coded form.
     * The accessor methods {@link #alternative()} and {@link #treeType()}
     * do the necessary decoding (to an array of {@link SqlProduction} and a {@link SqlTreeType} respectively).
     */
    private static final class CodedAlternative extends Pair<SqlTreeType, String[]> {
        public CodedAlternative(SqlTreeType l, String[] r) { super(l, r); }
        /** Get array of productions: the term sequence of the alternative */
        public SqlProduction[] alternative() { return aValueOf(this.right()); }
        /** Get the type of tree to construct for this alternative */
        public SqlTreeType treeType()    { return this.left(); }

        /**
         * Convert array of {@link String}s to array of {@link SqlProduction}s
         */
        private static final SqlProduction[] aValueOf(String[] ss) {
            SqlProduction[] scts = new SqlProduction[ss.length];
            for (int i=0; i<ss.length; ++i) {
                scts[i] = SqlProduction.valueOf(ss[i]);
            }
            return scts;
        }
    };

}
