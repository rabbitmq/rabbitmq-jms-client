// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestHexDisplay {

    private final static byte[] TEST_BYTES_1 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
    private final static String TEST_STRING_1= " 0001 0203 0405 0607 0809 0A0B 0C0D 0E0F | ................\n"
                                              +" 1011 1213 14                            | .....           \n";
    private final static byte[] TEST_BYTES_2 = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
                                                 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f,
                                                 0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f,
                                                 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f,
                                                 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f,
                                                 0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f,
                                                 0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f,
                                                 0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0x7f,
                                                 (byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86, (byte) 0x87,
                                                 (byte) 0x88, (byte) 0x89, (byte) 0x8a, (byte) 0x8b, (byte) 0x8c, (byte) 0x8d, (byte) 0x8e, (byte) 0x8f,
                                                 (byte) 0x90, (byte) 0x91, (byte) 0x92, (byte) 0x93, (byte) 0x94, (byte) 0x95, (byte) 0x96, (byte) 0x97,
                                                 (byte) 0x98, (byte) 0x99, (byte) 0x9a, (byte) 0x9b, (byte) 0x9c, (byte) 0x9d, (byte) 0x9e, (byte) 0x9f,
                                                 (byte) 0xa0, (byte) 0xa1, (byte) 0xa2, (byte) 0xa3, (byte) 0xa4, (byte) 0xa5, (byte) 0xa6, (byte) 0xa7,
                                                 (byte) 0xa8, (byte) 0xa9, (byte) 0xaa, (byte) 0xab, (byte) 0xac, (byte) 0xad, (byte) 0xae, (byte) 0xaf,
                                                 (byte) 0xb0, (byte) 0xb1, (byte) 0xb2, (byte) 0xb3, (byte) 0xb4, (byte) 0xb5, (byte) 0xb6, (byte) 0xb7,
                                                 (byte) 0xb8, (byte) 0xb9, (byte) 0xba, (byte) 0xbb, (byte) 0xbc, (byte) 0xbd, (byte) 0xbe, (byte) 0xbf,
                                                 (byte) 0xc0, (byte) 0xc1, (byte) 0xc2, (byte) 0xc3, (byte) 0xc4, (byte) 0xc5, (byte) 0xc6, (byte) 0xc7,
                                                 (byte) 0xc8, (byte) 0xc9, (byte) 0xca, (byte) 0xcb, (byte) 0xcc, (byte) 0xcd, (byte) 0xce, (byte) 0xcf,
                                                 (byte) 0xd0, (byte) 0xd1, (byte) 0xd2, (byte) 0xd3, (byte) 0xd4, (byte) 0xd5, (byte) 0xd6, (byte) 0xd7,
                                                 (byte) 0xd8, (byte) 0xd9, (byte) 0xda, (byte) 0xdb, (byte) 0xdc, (byte) 0xdd, (byte) 0xde, (byte) 0xdf,
                                                 (byte) 0xe0, (byte) 0xe1, (byte) 0xe2, (byte) 0xe3, (byte) 0xe4, (byte) 0xe5, (byte) 0xe6, (byte) 0xe7,
                                                 (byte) 0xe8, (byte) 0xe9, (byte) 0xea, (byte) 0xeb, (byte) 0xec, (byte) 0xed, (byte) 0xee, (byte) 0xef,
                                                 (byte) 0xf0, (byte) 0xf1, (byte) 0xf2, (byte) 0xf3, (byte) 0xf4, (byte) 0xf5, (byte) 0xf6, (byte) 0xf7,
                                                 (byte) 0xf8, (byte) 0xf9, (byte) 0xfa, (byte) 0xfb, (byte) 0xfc, (byte) 0xfd, (byte) 0xfe, (byte) 0xff };
    private final static String TEST_STRING_2 = " 0001 0203 0405 0607 0809 0A0B 0C0D 0E0F | ................\n" +
                                                " 1011 1213 1415 1617 1819 1A1B 1C1D 1E1F | ................\n" +
                                                " 2021 2223 2425 2627 2829 2A2B 2C2D 2E2F |  !\"#$%&'()*+,-./\n" +
                                                " 3031 3233 3435 3637 3839 3A3B 3C3D 3E3F | 0123456789:;<=>?\n" +
                                                " 4041 4243 4445 4647 4849 4A4B 4C4D 4E4F | @ABCDEFGHIJKLMNO\n" +
                                                " 5051 5253 5455 5657 5859 5A5B 5C5D 5E5F | PQRSTUVWXYZ[\\]^_\n" +
                                                " 6061 6263 6465 6667 6869 6A6B 6C6D 6E6F | `abcdefghijklmno\n" +
                                                " 7071 7273 7475 7677 7879 7A7B 7C7D 7E7F | pqrstuvwxyz{|}~.\n" +
                                                " 8081 8283 8485 8687 8889 8A8B 8C8D 8E8F | ................\n" +
                                                " 9091 9293 9495 9697 9899 9A9B 9C9D 9E9F | ................\n" +
                                                " A0A1 A2A3 A4A5 A6A7 A8A9 AAAB ACAD AEAF | ................\n" +
                                                " B0B1 B2B3 B4B5 B6B7 B8B9 BABB BCBD BEBF | ................\n" +
                                                " C0C1 C2C3 C4C5 C6C7 C8C9 CACB CCCD CECF | ................\n" +
                                                " D0D1 D2D3 D4D5 D6D7 D8D9 DADB DCDD DEDF | ................\n" +
                                                " E0E1 E2E3 E4E5 E6E7 E8E9 EAEB ECED EEEF | ................\n" +
                                                " F0F1 F2F3 F4F5 F6F7 F8F9 FAFB FCFD FEFF | ................\n";

    private final static byte[] TEST_BYTES_3 = { 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f,
                                                 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
                                                 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f,
                                                 0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f,
                                                 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36 };
    private final static String TEST_STRING_3 = " 1011 1213 1415 1617 1819 1A1B 1C1D 1E1F | ................\n" +
                                                " 0001 0203 0405 0607 0809 0A0B 0C0D 0E0F | ................\n" +
                                                " 4041 4243 4445 4647 4849 4A4B 4C4D 4E4F | @ABCDEFGHIJKLMNO\n" +
                                                " 2021 2223 2425 2627 2829 2A2B 2C2D 2E2F |  !\"#$%&'()*+,-./\n" +
                                                " 3031 3233 3435 36                       | 0123456         \n";

    private StringBuilder testSb = new StringBuilder();

    @Test
    public void testHexDisplaySmall() throws Exception {
        HexDisplay.decodeByteArrayIntoStringBuilder(TEST_BYTES_1, testSb);
        assertEquals(TEST_STRING_1, testSb.toString());
    }

    @Test
    public void testHexDisplayAll() throws Exception {
        HexDisplay.decodeByteArrayIntoStringBuilder(TEST_BYTES_2, testSb);
        assertEquals(TEST_STRING_2, testSb.toString(), "Output does not match");
    }

    @Test
    public void testHexDisplaySome() throws Exception {
        HexDisplay.decodeByteArrayIntoStringBuilder(TEST_BYTES_3, testSb);
        assertEquals(TEST_STRING_3, testSb.toString(), "Output does not match");
    }

    @Test
    public void testHexDisplayNull() throws Exception {
        HexDisplay.decodeByteArrayIntoStringBuilder(new byte[0], testSb);
        assertEquals("", testSb.toString(), "Output does not match");
    }
}
