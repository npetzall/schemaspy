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

import java.util.stream.Collectors;

public class FilenameSanitizer {

    private FilenameSanitizer() {}

    public static String sanitize(String filename) {
        return filename.codePoints().mapToObj(codePoint -> {
            if (!Character.isBmpCodePoint(codePoint)) {
                return "(" + Base62.encode(codePoint) + ")";
            }
            if (Character.isLetterOrDigit(codePoint)) {
                return new String(Character.toChars(codePoint));
            }
            if (Character.charCount(codePoint) == 1) {
                char c = Character.toChars(codePoint)[0];
                if (okToUse(c)) {
                    return Character.toString(c);
                } else {
                    return "(" + Base62.encode(codePoint) + ")";
                }
            } else {
                return "(" + Base62.encode(codePoint) + ")";
            }
        }).collect(Collectors.joining());
    }

    private static boolean okToUse(char c) {
        return c == '.' || c == '-' || c == '_';
    }
}
