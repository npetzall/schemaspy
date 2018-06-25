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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Base62Test {

    @Test
    public void encodeDecodeZero() {
        String expectedEncoded = "0";
        long input = 0;
        long expectedDecoded = 0;
        String actualEncoded = Base62.encode(input);
        long actualDecoded = Base62.decode(actualEncoded);
        assertThat(actualEncoded).isEqualTo(expectedEncoded);
        assertThat(actualDecoded).isEqualTo(expectedDecoded);
    }

    @Test
    public void encodeDecode123() {
        String expectedEncoded = "z1";
        long input = 123;
        long expectedDecoded = 123;
        String actualEncoded = Base62.encode(input);
        long actualDecoded = Base62.decode(actualEncoded);
        assertThat(actualEncoded).isEqualTo(expectedEncoded);
        assertThat(actualDecoded).isEqualTo(expectedDecoded);
    }

    @Test
    public void encodeDecode66560() {
        String expectedEncoded = "YJH";
        long input = 66560;
        long expectedDecoded = 66560;
        String actualEncoded = Base62.encode(input);
        long actualDecoded = Base62.decode(actualEncoded);
        assertThat(actualEncoded).isEqualTo(expectedEncoded);
        assertThat(actualDecoded).isEqualTo(expectedDecoded);
    }

}