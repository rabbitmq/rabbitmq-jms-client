// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2014-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

class GenericVersion {

    private final int major;
    private final int minor;
    private final int micro;
    private final String qualifier;

    public GenericVersion(int major, int minor, int micro, String qualifier) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.qualifier = non_null(qualifier);
    }

    public GenericVersion(int major, int minor, int micro) {
        this(major, minor, micro, "");
    }

    public GenericVersion(int major, int minor) {
        this(major, minor, 0, "");
    }

    public GenericVersion(int major) {
        this(major, 0, 0, "");
    }

    public GenericVersion(String versionString) {
        int mode=0;
        int ver[] = new int[] {0,0,0};
        StringBuilder qual=new StringBuilder();
        for (char ch : non_null(versionString).toCharArray()) {
                 if (mode==3)               { qual.append(ch);                                    }
            else if (Character.isDigit(ch)) { ver[mode] = 10*ver[mode] + Character.digit(ch, 10); }
            else if (ch == '.')             { ++mode;                                             }
            else                            { mode=3; qual.append(ch);                            }
        }
        this.major = ver[0];
        this.minor = ver[1];
        this.micro = ver[2];
        this.qualifier = qual.toString();
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getMicro() {
        return micro;
    }

    public String getQualifier() {
        return qualifier;
    }

    public String toString() {
        return new StringBuilder()
            .append(this.major).append('.')
            .append(this.minor).append('.')
            .append(this.micro).append(this.qualifier)
            .toString();
    }

    private static final String non_null(String str) { return (str==null ? "" : str); }
}
