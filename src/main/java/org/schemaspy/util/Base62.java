/*
 * Copyright (C) 2018 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.util;

/**
 * Inspired by https://gist.github.com/jdcrensh/4670128#gistcomment-1765587
 */

public class Base62 {

    private static char[] chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private Base62() {}

    public static String encode(long value) {
        final StringBuilder stringBuilder = new StringBuilder(4);
        do {
            stringBuilder.append(chars[(int)(value % chars.length)]);
            value /= chars.length;
        } while (value > 0);
        return stringBuilder.toString().trim();
    }

    public static long decode(String base64) {
        long value = 0;
        int power = 1;
        for (char c : base64.toCharArray()) {
            int digit = c - 48;
            if (digit > 42) {
                digit -= 13;
            } else if (digit > 9){
                digit -= 7;
            }
            value += digit * power;
            power *=62;
        }
        return value;
    }
}
