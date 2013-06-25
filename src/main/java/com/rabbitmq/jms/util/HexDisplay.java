package com.rabbitmq.jms.util;

public class HexDisplay {
    /** ---------------------------------------- XXXX XXXX XXXX XXXX XXXX XXXX XXXX XXXX | cccccccccccccccc */
    private static final String LineTemplate = "                                         |                 ";
    private static final int IndexFirstChar = 43; //                                       |

    public static final void decodeByteArrayIntoStringBuilder(byte[] buf, StringBuilder bufferOutput) {
        int bufPos = 0;
        StringBuilder lineBuf = new StringBuilder();
        int noLines = (buf.length + 15)/16;
        for (int line=0; line<noLines; ++line) {
            int startOfLine = 16*line;
            int lineExtent = min(buf.length, 16*line + 16);
            lineBuf.append(LineTemplate);
            while (bufPos < lineExtent) {
                int posHH = posOfHiHex(bufPos-startOfLine);
                lineBuf.setCharAt(posHH,   hiHex(buf[bufPos]));
                lineBuf.setCharAt(1+posHH, loHex(buf[bufPos]));
                lineBuf.setCharAt(IndexFirstChar + (bufPos-startOfLine), safeChar(buf[bufPos]));
                ++bufPos;
            }
            lineBuf.append('\n');
            bufferOutput.append(lineBuf);
            lineBuf.setLength(0);
        }
    }

    private static final char[] HEXCHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final char[] SAFECHARS = {'.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.',
                                             '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.',
                                             ' ', '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
                                             '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
                                             '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
                                             'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_',
                                             '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
                                             'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', '.'};

    private static final char hiHex(byte b) {
        return HEXCHARS[(b>>4) & 0x0F];
    }
    private static final char loHex(byte b) {
        return HEXCHARS[b & 0x0F];
    }
    private static final char safeChar(byte b) {
        return SAFECHARS[b<0 ? 0 : b];
    }

    private static final int posOfHiHex(int i) {
        return (i/2)*5 + (i%2 == 0 ? 1 : 3);
    }

    private static final int min(int a, int b) {
        return a<b ? a : b;
    }

}
