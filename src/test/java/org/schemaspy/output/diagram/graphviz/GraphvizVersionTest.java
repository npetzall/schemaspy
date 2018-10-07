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
package org.schemaspy.output.diagram.graphviz;

import junit.framework.TestCase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author John Currier
 * @author Rafal Kasa
 */
public class GraphvizVersionTest extends TestCase {
    private final GraphvizVersion twoNineOne = new GraphvizVersion("2.9.1");
    private final GraphvizVersion twoTen = new GraphvizVersion("2.10");
    private final GraphvizVersion twoTenOne = new GraphvizVersion("2.10.1");

    public void testCompareTo() {
        assertTrue(twoNineOne.compareTo(twoTen) < 0);
        assertTrue(twoTen.compareTo(twoTen) == 0);
        assertTrue(twoTenOne.compareTo(twoTen) > 0);

        assertTrue(twoNineOne.compareTo(twoTenOne) < 0);
        assertTrue(twoTen.compareTo(twoTenOne) < 0);
        assertTrue(twoTenOne.compareTo(twoTenOne) == 0);

        assertTrue(twoNineOne.compareTo(twoNineOne) == 0);
        assertTrue(twoTen.compareTo(twoNineOne) > 0);
        assertTrue(twoTenOne.compareTo(twoNineOne) > 0);
    }

    public void testEquals() {
        assertTrue(twoTen.equals(twoTen));
    }

    public void testNotEquals() {
        assertFalse(twoTenOne.equals(twoTen));
    }

    public void testExtract2203() {
        compareEquals("2.20.3", "dot - Graphviz version 2.20.3 (Wed Oct  8 06:02:12 UTC 2008");
    }

    public void testExtractMac2222() {
        compareEquals("2.22.2", "dot - graphviz version 2.22.2 (20090313.1817)");
    }

    private void compareEquals(String digits, String versionLine)
    {
        GraphvizVersion expected = new GraphvizVersion(digits);
        String actual = getVersionDigits(versionLine);
        assertEquals(expected, new GraphvizVersion(actual));
    }

    private String getVersionDigits(String versionLine) {
        Matcher matcher = Pattern.compile("[0-9][0-9.]+").matcher(versionLine);
        if (matcher.find())
            return matcher.group();
        fail("Failed to extract version digits from " + versionLine);
        return null;
    }
}
