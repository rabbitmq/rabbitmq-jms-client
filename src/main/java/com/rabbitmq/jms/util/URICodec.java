/* Copyright (c) 2013 GoPivotal, Inc. All rights reserved. */
package com.rabbitmq.jms.util;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;


/**
 * Helper class to build/deconstruct URIs
 * <p>
 * Static methods are provided to code and decode parts of a URI. Works only with UTF-8 and US_ASCII.
 * </p>
 * <p>
 * Coding and decoding a URI depends which part of the URI you are accessing. Notice, for example, that in the
 * <code>userinfo</code> part, colons are allowed asis, but that in relative segments they are not. Useful as this is,
 * we probably don’t want to have more than one colon in the <code>userinfo</code> part as it is conventionally used to
 * separate a <code>username</code> from a <code>password</code>. Modern passwords are likely to have a lot of special
 * characters in them, including colons, so we provide special functions which handle this, without violating the rules.
 * </p>
 * <p>
 * [<b>NB</b>: The algorithmic assumption is made throughout that all <i>valid</i> characters are represented by a single byte
 * in the US-ASCII codepage and that these coincide with their representation in UTF-8.]
 * </p>
 * <p>
 * <b>URI Specification</b>
 * </p>
 * <p>
 * <a href="http://tools.ietf.org/pdf/rfc2396.pdf">RFC 2396 - Uniform Resource Identifiers (URI): Generic Syntax</a>
 * is obsoleted by <a href="http://tools.ietf.org/pdf/rfc3986.pdf">RFC 3986 -
 * Uniform Resource Identifier (URI): Generic Syntax</a> which describes in (A)BNF the
 * valid form of an <i>encoded</i> URI.
 * </p>
 * <p>
 * Here is the appendix from <a href="http://tools.ietf.org/pdf/rfc3986.pdf">RFC 3986</a> which gives the ABNF.
 * ABNF itself is defined in <a href="http://tools.ietf.org/pdf/rfc2234.pdf">RFC
 * 2234 - Augmented BNF for Syntax Specifications: ABNF</a> where furthermore the following
 * non-terminals are defined: <code>ALPHA</code> (letters), <code>DIGIT</code> (decimal digits), and
 * <code>HEXDIG</code> (hexadecimal digits).
 * </p>
 * <p>[<b>NB</b>: I have replaced the ‘<code>/</code>’ (alternative) operator by the graphically more pleasing ‘<code>|</code>’.]
 * </p>
 * <pre>
 *    URI           = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
 *
 *    hier-part     = "//" authority path-abempty
 *                  | path-absolute
 *                  | path-rootless
 *                  | path-empty
 *
 *    URI-reference = URI | relative-ref
 *
 *    absolute-URI  = scheme ":" hier-part [ "?" query ]
 *
 *    relative-ref  = relative-part [ "?" query ] [ "#" fragment ]
 *
 *    relative-part = "//" authority path-abempty
 *                  | path-absolute
 *                  | path-noscheme
 *                  | path-empty
 *
 *    scheme        = ALPHA *( ALPHA | DIGIT | "+" | "-" | "." )
 *
 *    authority     = [ userinfo "@" ] host [ ":" port ]
 *    userinfo      = *( unreserved | pct-encoded | sub-delims | ":" )
 *    host          = IP-literal | IPv4address | reg-name
 *    port          = *DIGIT
 *
 *    IP-literal    = "[" ( IPv6address | IPvFuture  ) "]"
 *
 *    IPvFuture     = "v" 1*HEXDIG "." 1*( unreserved | sub-delims | ":" )
 *
 *    IPv6address   =                            6( h16 ":" ) ls32
 *                  |                       "::" 5( h16 ":" ) ls32
 *                  | [               h16 ] "::" 4( h16 ":" ) ls32
 *                  | [ *1( h16 ":" ) h16 ] "::" 3( h16 ":" ) ls32
 *                  | [ *2( h16 ":" ) h16 ] "::" 2( h16 ":" ) ls32
 *                  | [ *3( h16 ":" ) h16 ] "::"    h16 ":"   ls32
 *                  | [ *4( h16 ":" ) h16 ] "::"              ls32
 *                  | [ *5( h16 ":" ) h16 ] "::"              h16
 *                  | [ *6( h16 ":" ) h16 ] "::"
 *
 *    h16           = 1*4HEXDIG
 *    ls32          = ( h16 ":" h16 ) | IPv4address
 *    IPv4address   = dec-octet "." dec-octet "." dec-octet "." dec-octet
 *
 *    dec-octet     = DIGIT                 ; 0-9
 *                  | %x31-39 DIGIT         ; 10-99
 *                  | "1" 2DIGIT            ; 100-199
 *                  | "2" %x30-34 DIGIT     ; 200-249
 *                  | "25" %x30-35          ; 250-255
 *
 *    reg-name      = *( unreserved | pct-encoded | sub-delims )
 *
 *    path          = path-abempty    ; begins with "/" or is empty
 *                  | path-absolute   ; begins with "/" but not "//"
 *                  | path-noscheme   ; begins with a non-colon segment
 *                  | path-rootless   ; begins with a segment
 *                  | path-empty      ; zero characters
 *
 *    path-abempty  = *( "/" segment )
 *    path-absolute = "/" [ segment-nz *( "/" segment ) ]
 *    path-noscheme = segment-nz-nc *( "/" segment )
 *    path-rootless = segment-nz *( "/" segment )
 *    path-empty    = 0<pchar>
 *
 *    segment       = *pchar
 *    segment-nz    = 1*pchar
 *    segment-nz-nc = 1*( unreserved | pct-encoded | sub-delims | "@" )
 *                  ; non-zero-length segment without any colon ":"
 *
 *    pchar         = unreserved | pct-encoded | sub-delims | ":" | "@"
 *
 *    query         = *( pchar | "/" | "?" )
 *
 *    fragment      = *( pchar | "/" | "?" )
 *
 *    pct-encoded   = "%" HEXDIG HEXDIG
 *
 *    unreserved    = ALPHA | DIGIT | "-" | "." | "_" | "~"
 *    reserved      = gen-delims | sub-delims
 *    gen-delims    = ":" | "/" | "?" | "#" | "[" | "]" | "@"
 *    sub-delims    = "!" | "$" | "&" | "'" | "(" | ")"
 *                  | "*" | "+" | "," | ";" | "="
 *
 * </pre>
 */
public abstract class URICodec {

    public static final String encUserinfo(String ui, String encodingScheme) {
        return genericEncode(USERINFO_C_BA, ui, encodingScheme);
    }

    public static final String decUserinfo(String rawui, String encodingScheme) {
        return genericDecode(rawui, encodingScheme);
    }

    public static final String encScheme(String ui, String encodingScheme) {
        return genericEncode(SCHEME_C_BA, ui, encodingScheme);
    }

    public static final String decScheme(String rawui, String encodingScheme) {
        return genericDecode(rawui, encodingScheme);
    }

    public static final String encSegment(String ui, String encodingScheme) {
        return genericEncode(SEGMENT_C_BA, ui, encodingScheme);
    }

    public static final String decSegment(String rawui, String encodingScheme) {
        return genericDecode(rawui, encodingScheme);
    }

    public static final String encHost(String ui, String encodingScheme) {
        return genericEncode(HOST_C_BA, ui, encodingScheme);
    }

    public static final String decHost(String rawui, String encodingScheme) {
        return genericDecode(rawui, encodingScheme);
    }

    private static final String genericDecode(String rawstr, String encodingScheme) {
        try {
            int starState = 0;
            int n=0; byte hiHex = 0;
            byte[] bytes = new byte[rawstr.length()]; // n bytes are set
            for (byte b : rawstr.getBytes(encodingScheme)) {
                switch (starState) {
                case 0 : if (b!='%') bytes[n++] = b;
                             else   { starState = 1; } break;
                case 1 : starState = 2; hiHex = b;     break;
                case 2 : starState = 0; bytes[n++] = (byte) (hiHex*16 + b); break;
                default : break;
                }
            }
            return new String(bytes, encodingScheme);
        } catch (UnsupportedEncodingException e) {
            return rawstr;
        }
    }
    private static final String genericEncode(byte[] charClass, String str, String encodingScheme) {
        try {
            StringBuilder sb = new StringBuilder();
            byte[] strBytes = str.getBytes(encodingScheme);
            for (byte b : strBytes) {
                if (inClass(b,charClass)) sb.append(Character.toChars(b));
                else addEscaped(sb, b);
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }
    private static final void addEscaped(StringBuilder sb, int b) {
        int ind = (b>=0 ? b : b+256); // to range 0<=b<=255
        sb.append('%').append(HIHEX[ind]).append(LOHEX[ind]);
    }

    private static final boolean inClass(byte b, byte[] sortedBytes) {
        return Arrays.binarySearch(sortedBytes, b) >= 0;
    }

    private URICodec() {}  // no instantiation

    private static final String DIGIT = "0123456789";
    private static final String HEXDIG = DIGIT + "abcdefABCDEF";
    private static final String ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String GEN_DELIMS = ":/?#[]@";
    private static final String SUB_DELIMS = "!$&'()*+,;=";

    private static final String UNRESERVED = ALPHA + DIGIT + "-._~";
    private static final String RESERVED = GEN_DELIMS + SUB_DELIMS;

    private static final String SCHEME_C = ALPHA + DIGIT + "+-.";

    private static final String PATHCHAR_C = UNRESERVED + SUB_DELIMS + ":@";
    private static final String QUERY_C = PATHCHAR_C + "/?";
    private static final String FRAGMENT_C = PATHCHAR_C + "/?";
    private static final String SEGMENT_C = PATHCHAR_C;

    private static final String USERINFO_C = UNRESERVED + SUB_DELIMS + ":";
    private static final String HOST_C = UNRESERVED + SUB_DELIMS + ":[].";

    private static final byte[] DIGIT_BA = bytesOf(DIGIT);
    private static final byte[] HEXDIG_BA = bytesOf(HEXDIG);
    private static final byte[] ALPHA_BA = bytesOf(ALPHA);
    private static final byte[] UNRESERVED_BA = bytesOf(UNRESERVED);
    private static final byte[] SCHEME_C_BA = bytesOf(SCHEME_C);
    private static final byte[] USERINFO_C_BA = bytesOf(USERINFO_C);
    private static final byte[] HOST_C_BA = bytesOf(HOST_C);
    private static final byte[] PATHCHAR_C_BA = bytesOf(PATHCHAR_C);
    private static final byte[] SEGMENT_C_BA = PATHCHAR_C_BA;

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
