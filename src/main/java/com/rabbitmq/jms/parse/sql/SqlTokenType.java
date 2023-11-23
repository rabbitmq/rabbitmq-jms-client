/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse.sql;

import java.util.regex.Pattern;


/**
 * SQL Token type
 * <p>
 * Each type of token has an <b>opCode</b> (which is printable), a recognition pattern (regular expression), a data type (for associated data),
 * and a boolean flag indicating if the token is streamed. Tokens which are not streamed are intended to be discarded
 * when recognised. Token which are streamed are intended for consumption by a parser.
 * </p>
 * <p>
 * The optional data type is a value of the <code>enum</code> {@link SqlTokenValueType}.</p>
 * <b>Notes</b>
 * <p>The <b><i>order</i></b> of the <code>enum</code> elements is significant. Matches in the lexical
 * parser that uses this type check each pattern <i>in turn</i> and the first match wins. This is
 * not the same as the generic lexical analysers generally available (for example Le(e)x) which
 * usually match the longest prefix of the character string.</p>
 * <p>The keywords are case insensitive so appear like <code>[Ll][Ii][Kk][Ee]</code> in the patterns.</p>
 * <p>Each keyword (sequence) is terminated by <code>(?![a-zA-Z0-9_$.])</code> to prevent identifiers that
 * <i>begin</i> with a keyword being (mis)recognised as a keyword <i>followed by</i> an identifier.
 * This pattern checks that the next character is not part of an identifier body, but does not match it.
 * </p>
 */
enum SqlTokenType {
//  Token_name_ Include OpCode_________   Recognition_pattern_____________________________________________    Type_of_variable_datum__
    WS          (false, "whitespace"    , "\\s+"                                                           ),
    LIKE        (true , "like"          , "[Ll][Ii][Kk][Ee](?![a-zA-Z0-9_$.])"                             ),
    NOT_LIKE    (true , "not_like"      , "[Nn][Oo][Tt]\\s+[Ll][Ii][Kk][Ee](?![a-zA-Z0-9_$.])"             ),
    IN          (true , "in"            , "[Ii][Nn](?![a-zA-Z0-9_$.])"                                     ),
    NOT_IN      (true , "not_in"        , "[Nn][Oo][Tt]\\s+[Ii][Nn](?![a-zA-Z0-9_$.])"                     ),
    NULL        (true , "is_null"       , "[Ii][Ss]\\s+[Nn][Uu][Ll][Ll](?![a-zA-Z0-9_$.])"                 ),
    NOT_NULL    (true , "not_null"      , "[Ii][Ss]\\s+[Nn][Oo][Tt]\\s+[Nn][Uu][Ll][Ll](?![a-zA-Z0-9_$.])" ),
    BETWEEN     (true , "between"       , "[Bb][Ee][Tt][Ww][Ee][Ee][Nn](?![a-zA-Z0-9_$.])"                 ),
    NOT_BETWEEN (true , "not_between"   , "[Nn][Oo][Tt]\\s+[Bb][Ee][Tt][Ww][Ee][Ee][Nn](?![a-zA-Z0-9_$.])" ),
    AND         (true , "and"           , "[Aa][Nn][Dd](?![a-zA-Z0-9_$.])"                                 ),
    OR          (true , "or"            , "[Oo][Rr](?![a-zA-Z0-9_$.])"                                     ),
    NOT         (true , "not"           , "[Nn][Oo][Tt](?![a-zA-Z0-9_$.])"                                 ),
    ESCAPE      (true , "ESCAPE"        , "[Ee][Ss][Cc][Aa][Pp][Ee](?![a-zA-Z0-9_$.])"                     ),
    TRUE        (true , "true"          , "[Tt][Rr][Uu][Ee](?![a-zA-Z0-9_$.])"                             ),
    FALSE       (true , "false"         , "[Ff][Aa][Ll][Ss][Ee](?![a-zA-Z0-9_$.])"                         ),
    CMP_EQ      (true , "="             , "="                                                              ),
    CMP_NEQ     (true , "<>"            , "<>"                                                             ),
    CMP_LTEQ    (true , "<="            , "<="                                                             ),
    CMP_GTEQ    (true , ">="            , ">="                                                             ),
    CMP_LT      (true , "<"             , "<"                                                              ),
    CMP_GT      (true , ">"             , ">"                                                              ),
    OP_PLUS     (true , "+"             , "\\+"                                                            ),
    OP_MINUS    (true , "-"             , "-"                                                              ),
    OP_MULT     (true , "*"             , "\\*"                                                            ),
    OP_DIV      (true , "/"             , "/"                                                              ),
    COMMA       (true , ","             , ","                                                              ),
    LP          (true , "("             , "\\("                                                            ),
    RP          (true , ")"             , "\\)"                                                            ),
    IDENT       (true , "ident"         , "[a-zA-Z_$][a-zA-Z0-9_$.]*"                                       , SqlTokenValueType.IDENT ),
    STRING      (true , "string"        , "'([^']|'')*'"                                                    , SqlTokenValueType.STRING),
    FLOAT       (true , "float"         , "[0-9]+(\\.[0-9]+[Ee][-+]?[0-9]+"
                                              + "|(\\.([Ee][-+]?[0-9]+)|[Ee][-+]?[0-9]+)"
                                              + "|(\\.[0-9]*)?[fFdD]"
                                              + "|\\.[0-9]*)"                                               , SqlTokenValueType.FLOAT ),
    INT         (true , "integer"       , "[0-9]+"                                                          , SqlTokenValueType.LONG  ),
    HEX         (true , "hex"           , "0x[0-9a-fA-F]+"                                                  , SqlTokenValueType.HEX   ),
    LIST        (false, "list"          , null                                                              , SqlTokenValueType.LIST  ),
    TEST        (false, "‹›"            , null                                                              , SqlTokenValueType.NO_VALUE);  // only used in unit testing

    private boolean include;            // include in token stream
    private String opCode;              // used by compiler
    private Pattern pattern;            // recogniser for tokenizer
    private SqlTokenValueType vtype;    // type of variable data in token

    SqlTokenType(boolean isToken, String opCode, String regex) {
        this(isToken, opCode, regex, SqlTokenValueType.NO_VALUE);
    }

    SqlTokenType(boolean include, String opCode, String regex, SqlTokenValueType vtype) {
        this.include = include;
        this.opCode = opCode;
        this.vtype = vtype;
        this.pattern = (regex==null ? null : Pattern.compile(regex));
    }

    String opCode() {
        return this.opCode;
    }

    SqlTokenValueType valueType() {
        return this.vtype;
    }

    boolean include() {
        return this.include;
    }

    Pattern pattern() {
        return this.pattern;
    }
}
