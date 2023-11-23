/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.util;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;


/**
 * Helper class to encode/decode (pieces of) URI strings
 * <p>
 * Static methods are provided to encode and decode parts of a URI. Works only with UTF-8 and US-ASCII character encodings.
 * The results are undefined for all other encodings.
 * </p>
 * <p>
 * Encoding a string involves encoding each character not allowed in that part of the URI string. Non-allowed characters are
 * converted to a sequence of ‘percent-encoded’ bytes (octets). A percent-encoded octet is ‘<code>%</code><i>xx</i>’
 * where <i>xx</i> are two hexadecimal digits. The octets percent-encoded for a character depend upon the <i>character</i>
 * encoding used: in the case of UTF-8 this might be up to four octets, but for US-ASCII encoding this would be only one octet.
 * Decoding reconstructs the characters from the percent-encoded octets.
 * </p>
 * <p>
 * The character encoding used to decode a string must be that used encode it, or the results are undefined.
 * </p>
 * <p>
 * Coding and decoding a URI depends which part of the URI you are accessing. Notice, for example, that in the
 * <code>userinfo</code> part, colons are allowed asis, but that in the first relative path segment they are not.
 * </p>
 * <p>
 * This class only provides string-to-string encoding and decoding methods for (some) parts of a URI string.
 * It does <i>not</i> syntactically validate the encoded or decoded strings for each part. So, for example,
 * it will encode a scheme part which contains colons (replacing them with percent-encoded octets), even though
 * percent-encoded octets are not allowed in schemes.
 * </p>
 * <p>
 * [<b>NB</b>: The algorithmic assumption is made throughout that all <i>valid</i> characters are represented by a single byte
 * in the US-ASCII codepage and that these coincide with their representation in UTF-8.]
 * </p>
 * <p>
 * <b>URI Specification</b>
 * </p>
 * <p>
 * <a href="https://tools.ietf.org/pdf/rfc2396.pdf">RFC 2396 - Uniform Resource Identifiers (URI): Generic Syntax</a>
 * is obsoleted by <a href="https://tools.ietf.org/pdf/rfc3986.pdf">RFC 3986 -
 * Uniform Resource Identifier (URI): Generic Syntax</a> which describes in (A)BNF the
 * valid form of an <i>encoded</i> URI.
 * </p>
 * <p>
 * Here is the appendix from <a href="https://tools.ietf.org/pdf/rfc3986.pdf">RFC 3986</a> which gives the ABNF.
 * ABNF itself is defined in <a href="https://tools.ietf.org/pdf/rfc2234.pdf">RFC
 * 2234 - Augmented BNF for Syntax Specifications: ABNF</a> where furthermore the following
 * non-terminals are defined: <code>ALPHA</code> (letters), <code>DIGIT</code> (decimal digits), and
 * <code>HEXDIG</code> (hexadecimal digits).
 * </p>
 * <p>[<b>NB</b>: ‘<code><b>;</b></code>’ indicates a line comment, as in the original, but I have replaced the
 * solidus ‘<code><b>/</b></code>’ (alternative) operator by the graphemically more pleasing vertical
 * bar ‘<code><b>|</b></code>’.]
 * </p>
 */
public abstract class UriCodec {  // Prevent me declaring instances.
    private UriCodec() {}         // Prevent anyone else declaring instances; also prohibits extensions.

    /**
     * Encode a string for the userinfo part of a URI string.
     * @param ui - uncoded input
     * @param encodingScheme - must be one of "UTF-8" or "US-ASCII"
     * @return ui encoded (with % encodings if necessary)
     */
    public static String encUserinfo(String ui, String encodingScheme) {
        return genericEncode(USERINFO_C_BA, ui, encodingScheme);
    }

    /**
     * @param rawui - encoded userinfo part
     * @param encodingScheme - must be one of "UTF-8" of "US-ASCII"
     * @return rawui decoded (with % encodings replaced by characters they represent)
     */
    public static String decUserinfo(String rawui, String encodingScheme) {
        return genericDecode(rawui, encodingScheme);
    }

    /**
     * Encode a string for the scheme part of a URI string.
     * @param ui - uncoded input
     * @param encodingScheme - must be one of "UTF-8" or "US-ASCII"
     * @return ui encoded (with % encodings if necessary)
     */
    public static String encScheme(String ui, String encodingScheme) {
        return genericEncode(SCHEME_C_BA, ui, encodingScheme);
    }

    /**
     * @param rawui - encoded scheme part
     * @param encodingScheme - must be one of "UTF-8" of "US-ASCII"
     * @return rawui decoded (with % encodings replaced by characters they represent)
     */
    public static String decScheme(String rawui, String encodingScheme) {
        return genericDecode(rawui, encodingScheme);
    }

    /**
     * Encode a string for a segment part of a URI string.
     * @param ui - uncoded input
     * @param encodingScheme - must be one of "UTF-8" or "US-ASCII"
     * @return ui encoded (with % encodings if necessary)
     */
    public static String encSegment(String ui, String encodingScheme) {
        return genericEncode(SEGMENT_C_BA, ui, encodingScheme);
    }

    /**
     * @param rawui - encoded segment part
     * @param encodingScheme - must be one of "UTF-8" of "US-ASCII"
     * @return rawui decoded (with % encodings replaced by characters they represent)
     */
    public static String decSegment(String rawui, String encodingScheme) {
        return genericDecode(rawui, encodingScheme);
    }

    /**
     * Encode a string for the host part of a URI string.
     * @param ui - uncoded input
     * @param encodingScheme - must be one of "UTF-8" or "US-ASCII"
     * @return ui encoded (with % encodings if necessary)
     */
    public static String encHost(String ui, String encodingScheme) {
        return genericEncode(HOST_C_BA, ui, encodingScheme);
    }

    /**
     * @param rawui - encoded host part
     * @param encodingScheme - must be one of "UTF-8" of "US-ASCII"
     * @return rawui decoded (with % encodings replaced by characters they represent)
     */
    public static String decHost(String rawui, String encodingScheme) {
        return genericDecode(rawui, encodingScheme);
    }

    /**
     * Encode a string for the path part of a URI string.
     * @param ui - uncoded input
     * @param encodingScheme - must be one of "UTF-8" or "US-ASCII"
     * @return ui encoded (with % encodings if necessary)
     */
    public static String encPath(String ui, String encodingScheme) {
        return genericEncode(PATH_C_BA, ui, encodingScheme);
    }

    /**
     * @param rawui - encoded path part
     * @param encodingScheme - must be one of "UTF-8" of "US-ASCII"
     * @return rawui decoded (with % encodings replaced by characters they represent)
     */
    public static String decPath(String rawui, String encodingScheme) {
        return genericDecode(rawui, encodingScheme);
    }

    private static String genericDecode(String rawstr, String encodingScheme) {
        try {
            int starState = 0;
            int n=0; int hiHex = 0;
            byte[] bytes = new byte[rawstr.length()]; // invariant: n bytes are set
            for (byte b : rawstr.getBytes(encodingScheme)) {
                switch (starState) {
                case 0 : if (b!='%') bytes[n++] = b;
                         else { starState = 1; }        break;
                case 1 : starState = 2; hiHex = hex(b); break;
                case 2 : starState = 0; bytes[n++] = (byte) (hiHex*16 + hex(b)); break;
                default : break;
                }
            } // n is length of 'set' byte array
            return new String(bytes, 0, n, encodingScheme);
        } catch (UnsupportedEncodingException e) {
            return rawstr;
        }
    }

    private static int hex(int b) {
        return  (b < 48)  ? 0
               :(b < 58)  ? (b-48)  // 48..57  ->  0..9
               :(b < 65)  ? 0
               :(b < 71)  ? (b-55)  // 65..70  -> 10..15
               :(b < 97)  ? 0
               :(b < 103) ? (b-87)  // 97..102 -> 10..15
               :            0;
    }

    private static String genericEncode(byte[] charClass, String str, String encodingScheme) {
        try {
            StringBuilder sb = new StringBuilder();
            for (byte b : str.getBytes(encodingScheme)) {
                if (inClass(b,charClass)) sb.append(Character.toChars(b));
                else addEscaped(sb, b);
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    private static void addEscaped(StringBuilder sb, int b) {
        int ind = (b+256)%256; // to range 0<=ind<=255
        sb.append('%').append(HIHEX[ind]).append(LOHEX[ind]);
    }

    private static boolean inClass(byte b, byte[] sortedBytes) {
        return Arrays.binarySearch(sortedBytes, b) >= 0;
    }

    private static final String DIGIT  = "0123456789";
    private static final String ALPHA  = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String SUB_DELIMS = "!$&'()*+,;=";                     // sub-delims
    private static final String UNRESERVED = ALPHA + DIGIT + "-._~";            // unreserved
    private static final String SCHEME_C = ALPHA + DIGIT + "+-.";               // scheme
    private static final String PCHAR_C = UNRESERVED + SUB_DELIMS + ":@";       // pchar
    private static final String SEGMENT_C  = PCHAR_C;                           // segment
    private static final String PATH_C     = SEGMENT_C + "/";                   // path

    private static final String USERINFO_C = UNRESERVED + SUB_DELIMS + ":";     // userinfo
    private static final String HOST_C     = UNRESERVED + SUB_DELIMS + ":[].";  // host

    // sorted array optimisations
    private static final byte[] SCHEME_C_BA   = bytesOf(SCHEME_C);
    private static final byte[] USERINFO_C_BA = bytesOf(USERINFO_C);
    private static final byte[] HOST_C_BA     = bytesOf(HOST_C);
    private static final byte[] PCHAR_C_BA    = bytesOf(PCHAR_C);
    private static final byte[] SEGMENT_C_BA  = PCHAR_C_BA;
    private static final byte[] PATH_C_BA     = bytesOf(PATH_C);

    private static final char[] LOHEX = new char[] { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   , '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   , '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   , '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   , '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   , '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   , '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   , '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   , '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   , '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   , '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   , '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   , '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   , '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   , '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   , '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
                                                   };
    private static final char[] HIHEX = new char[] { '0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0'
                                                   , '1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1'
                                                   , '2','2','2','2','2','2','2','2','2','2','2','2','2','2','2','2'
                                                   , '3','3','3','3','3','3','3','3','3','3','3','3','3','3','3','3'
                                                   , '4','4','4','4','4','4','4','4','4','4','4','4','4','4','4','4'
                                                   , '5','5','5','5','5','5','5','5','5','5','5','5','5','5','5','5'
                                                   , '6','6','6','6','6','6','6','6','6','6','6','6','6','6','6','6'
                                                   , '7','7','7','7','7','7','7','7','7','7','7','7','7','7','7','7'
                                                   , '8','8','8','8','8','8','8','8','8','8','8','8','8','8','8','8'
                                                   , '9','9','9','9','9','9','9','9','9','9','9','9','9','9','9','9'
                                                   , 'A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A'
                                                   , 'B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B'
                                                   , 'C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C'
                                                   , 'D','D','D','D','D','D','D','D','D','D','D','D','D','D','D','D'
                                                   , 'E','E','E','E','E','E','E','E','E','E','E','E','E','E','E','E'
                                                   , 'F','F','F','F','F','F','F','F','F','F','F','F','F','F','F','F'
                                                   };

    private static final byte[] bytesOf(String str) {
        try {
            byte[] result = str.getBytes("UTF-8");
            Arrays.sort(result, 0, result.length);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate byte array for URI conversion.", e);
        }
    }
}
