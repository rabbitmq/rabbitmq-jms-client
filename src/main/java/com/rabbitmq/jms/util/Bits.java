package com.rabbitmq.jms.util;

/**
* Utility methods for packing/unpacking primitive values in/out of byte arrays
* using big-endian byte ordering.
* [Copied and extended from java.io.Bits, since it is package private :-(]
*/
public abstract class Bits { // prevent ourselves creating an instance

    private Bits() {};  // prevent anyone else creating an instance
    /*
     * Methods for unpacking primitive values from byte arrays starting at
     * given offsets.
     */

    public static final int NUM_BYTES_IN_BOOLEAN = 1;
    public static boolean getBoolean(byte[] b, int off) {
        return b[off] != 0;
    }

    public static final int NUM_BYTES_IN_CHAR = 2;
    public static char getChar(byte[] b, int off) {
        return (char) (((b[off + 1] & 0xFF) << 0) +
                       ((b[off + 0]) << 8));
    }

    public static final int NUM_BYTES_IN_SHORT = 2;
    public static short getShort(byte[] b, int off) {
        return (short) (((b[off + 1] & 0xFF) << 0) +
                        ((b[off + 0]) << 8));
    }

    public static final int NUM_BYTES_IN_INT = 4;
    public static int getInt(byte[] b, int off) {
        return ((b[off + 3] & 0xFF) << 0) +
               ((b[off + 2] & 0xFF) << 8) +
               ((b[off + 1] & 0xFF) << 16) +
               ((b[off + 0]) << 24);
    }

    public static final int NUM_BYTES_IN_FLOAT = 4;
    public static float getFloat(byte[] b, int off) {
        return Float.intBitsToFloat(getInt(b, off));
    }

    public static final int NUM_BYTES_IN_LONG = 8;
    public static long getLong(byte[] b, int off) {
        return ((b[off + 7] & 0xFFL) << 0) +
                ((b[off + 6] & 0xFFL) << 8) +
                ((b[off + 5] & 0xFFL) << 16) +
                ((b[off + 4] & 0xFFL) << 24) +
                ((b[off + 3] & 0xFFL) << 32) +
                ((b[off + 2] & 0xFFL) << 40) +
                ((b[off + 1] & 0xFFL) << 48) +
                (((long) b[off + 0]) << 56);
    }

    public static final int NUM_BYTES_IN_DOUBLE = 8;
    public static double getDouble(byte[] b, int off) {
        return Double.longBitsToDouble(getLong(b, off));
    }

    /*
     * Methods for packing primitive values into byte arrays starting at given
     * offsets.
     */

    public static void putBoolean(byte[] b, int off, boolean val) {
        b[off] = (byte) (val ? 1 : 0);
    }

    public static void putChar(byte[] b, int off, char val) {
        b[off + 1] = (byte) (val >>> 0);
        b[off + 0] = (byte) (val >>> 8);
    }

    public static void putShort(byte[] b, int off, short val) {
        b[off + 1] = (byte) (val >>> 0);
        b[off + 0] = (byte) (val >>> 8);
    }

    public static void putInt(byte[] b, int off, int val) {
        b[off + 3] = (byte) (val >>> 0);
        b[off + 2] = (byte) (val >>> 8);
        b[off + 1] = (byte) (val >>> 16);
        b[off + 0] = (byte) (val >>> 24);
    }

    public static void putFloat(byte[] b, int off, float val) {
        putInt(b, off, Float.floatToIntBits(val));
    }

    public static void putLong(byte[] b, int off, long val) {
        b[off + 7] = (byte) (val >>> 0);
        b[off + 6] = (byte) (val >>> 8);
        b[off + 5] = (byte) (val >>> 16);
        b[off + 4] = (byte) (val >>> 24);
        b[off + 3] = (byte) (val >>> 32);
        b[off + 2] = (byte) (val >>> 40);
        b[off + 1] = (byte) (val >>> 48);
        b[off + 0] = (byte) (val >>> 56);
    }

    public static void putDouble(byte[] b, int off, double val) {
        putLong(b, off, Double.doubleToLongBits(val));
    }
}