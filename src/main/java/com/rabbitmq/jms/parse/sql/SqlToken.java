/**
 *
 */
package com.rabbitmq.jms.parse.sql;

import java.util.List;

/**
 * @author spowell
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
    }

    SqlToken(SqlTokenType tokType) {
        this(tokType, null, null);
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
        if (this.tokType.valueType() == SqlTokenValueType.LIST)
            return this.tokValueList;
        return null;
    }

    float getFloat() {
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
        StringBuilder sb = new StringBuilder(this.tokType.description());
        //sb.append(this.tokType.valueType().toString(value))
        switch (this.tokType.valueType()) {
        case FLOAT:
            sb.append(": ").append(getFloat());   break;
        case HEX:
            sb.append(": ").append(getHex());     break;
        case IDENT:
            sb.append(": ").append(getIdent());   break;
        case LONG:
            sb.append(": ").append(getLong());    break;
        case STRING:
            sb.append(": ").append(getString());  break;
        default:
            break;
        }
        return sb.toString();
    }

    private final static String unEscape(String s) {
        final int len = s.length()-1;
        StringBuilder sb = new StringBuilder();
        for (int i=1; i<len; ++i) {
            char c=s.charAt(i);
            switch (c) {
                case '\'' : if ('\''==s.charAt(i+1))
                                ++i;
                            break;
                default   : break;
            }
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
    private final static long hexToLong(String s) {
        final int len = s.length();
        long result = 0L;
        for (int i=2; i<len; ++i) { // i starts at 2 to skip the "0x" prefix of the hex token
            result=16*result + hexVal(s.charAt(i));
        }
        return result;
    }
    private final static long hexVal(char c) {
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
    private final static float stringToFloat(String s) {
        return Float.valueOf(s);
    }
}
