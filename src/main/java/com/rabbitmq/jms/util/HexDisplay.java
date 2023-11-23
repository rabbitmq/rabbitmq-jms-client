/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.util;

public class HexDisplay {
    /** ---------------------------------------- XXXX XXXX XXXX XXXX XXXX XXXX XXXX XXXX | cccccccccccccccc */
    private static final String LineTemplate = "                                         |                 ";
    private static final int IndexFirstChar = 43; //                                       |

    private static final char[] HEXCHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final char[] SAFECHARS = {'.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.',
                                             '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.',
                                             ' ', '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
                                             '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
                                             '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
                                             'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_',
                                             '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
                                             'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', '.'};

    public static final void decodeByteArrayIntoStringBuilder(byte[] buf, StringBuilder bufferOutput) {
        StringBuilder lineBuf = new StringBuilder();
        for (int startOfLine=0; startOfLine<buf.length; startOfLine+=16) {
            int lineLength = min(buf.length - startOfLine, 16);
            lineBuf.setLength(0);
            lineBuf.append(LineTemplate);
            for (int linePos=0; linePos<lineLength; ++linePos) {
                byte b = buf[startOfLine+linePos];
                setHexInStringBuilder(lineBuf, displayPosOfHiHex(linePos), b);
                lineBuf.setCharAt(IndexFirstChar + linePos, safeChar(b));
            }
            bufferOutput.append(lineBuf).append('\n');
        }
    }

    private static final void setHexInStringBuilder(StringBuilder lineBuf, int pos, byte b) {
        lineBuf.setCharAt(pos++, HEXCHARS[(b>>4) & 0x0F]);
        lineBuf.setCharAt(pos,   HEXCHARS[     b & 0x0F]);
    }

    private static final char safeChar(byte b) {
        return SAFECHARS[b<0 ? 0 : b];
    }

    private static final int displayPosOfHiHex(int i) {
        return (i/2)*5 + (i%2 == 0 ? 1 : 3);
    }

    private static final int min(int a, int b) {
        return a<b ? a : b;
    }

}
