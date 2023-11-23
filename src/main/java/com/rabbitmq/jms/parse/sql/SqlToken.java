/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse.sql;

import java.util.List;

/**
 * Tokens are lexically extracted from SQL selector expressions.
 * A token has a type {@link SqlTokenType} and a value. The value can be either a {@link String} or a list of {@link String}s.
 */
class SqlToken {
    private final SqlTokenType tokType;
    private final String tokValue;
    private final List<String> tokValueList;

    SqlToken(SqlTokenType tokType, String tokValue) {
        this(tokType, tokValue, null);
    }

    SqlToken(SqlTokenType tokType, List<String> tokValueList) {
        this(tokType, null, tokValueList);
        if (tokType.valueType() != SqlTokenValueType.LIST)
            throw new IllegalArgumentException("type not a LIST");
    }

    private SqlToken(SqlTokenType tokType, String tokValue, List<String> tokValueList) {
        this.tokType = tokType;
        this.tokValue = tokValue;
        this.tokValueList = tokValueList;
    }

    SqlTokenType type() {
        return this.tokType;
    }

    List<String> getList() {
        return this.tokValueList;
    }

    double getFloat() {
        if (this.tokType.valueType() == SqlTokenValueType.FLOAT)
            return(stringToFloat(this.tokValue));
        return 0.0F;
    }
    long getLong() {
        if (this.tokType.valueType() == SqlTokenValueType.LONG)
            return(Long.valueOf(this.tokValue));
        return 0L;
    }
    long getHex() {
        if (this.tokType.valueType() == SqlTokenValueType.HEX)
            return(hexToLong(this.tokValue));
        return 0L;
    }
    String getString() {
        if (this.tokType.valueType() == SqlTokenValueType.STRING)
            return(unEscape(this.tokValue));
        return "";
    }
    String getIdent() {
        if (this.tokType.valueType() == SqlTokenValueType.IDENT)
            return(String.valueOf(this.tokValue));
        return "";
    }
    public String toString() {
        StringBuilder sb = new StringBuilder(this.tokType.opCode());
        try {
            switch (this.tokType.valueType()) {
            case FLOAT:
                sb.append(": ").append(getFloat());         break;
            case HEX:
                sb.append(": ").append(getHex());           break;
            case IDENT:
                sb.append(": ").append(getIdent());         break;
            case LONG:
                sb.append(": ").append(getLong());          break;
            case STRING:
                sb.append(": ").append(this.tokValue);      break;
            case LIST:
                sb.append(": ").append(this.tokValueList);  break;
            default:
                break;
            }
        } catch (Exception e) {
            sb.append("invalid(").append(this.tokValue).append(')');
        }
        return sb.toString();
    }

    private static final String unEscape(String s) {
        final int len = s.length()-1;
        StringBuilder sb = new StringBuilder();
        for (int i=1; i<len; ++i) {
            char c=s.charAt(i);
            if (c == '\'' && s.charAt(i+1) == '\'') ++i;  // compress two single-quotes to one
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * This is basic hexadecimal conversion. Precondition is that <code>s</code> matches the pattern:
     * <code>"0x[0-9a-fA-F]+"</code>.
     *
     * @param s - hex token string to convert to a long integer
     * @return the long integer equal in value to the hex token
     */
    private static final long hexToLong(String s) {
        final int len = s.length();
        long result = 0L;
        for (int i=2; i<len; ++i) { // i starts at 2 to skip the "0x" prefix of the hex token
            result=16*result + hexVal(s.charAt(i));
        }
        return result;
    }
    private static final long hexVal(char c) {
        switch (c) {
            case '0': return 0L;
            case '1': return 1L;
            case '2': return 2L;
            case '3': return 3L;
            case '4': return 4L;
            case '5': return 5L;
            case '6': return 6L;
            case '7': return 7L;
            case '8': return 8L;
            case '9': return 9L;
            case 'a':
            case 'A': return 10L;
            case 'b':
            case 'B': return 11L;
            case 'c':
            case 'C': return 12L;
            case 'd':
            case 'D': return 13L;
            case 'e':
            case 'E': return 14L;
            case 'f':
            case 'F': return 15L;
            default : return 0L;
        }
    }
    private static final double stringToFloat(String s) {
        return Double.valueOf(s);
    }
}
