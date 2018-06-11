/*
 * Copyright (C) 2011 John Currier
 * Copyright (C) 2017 Daniel Watt
 *
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy.view;

import org.schemaspy.model.Database;
import org.schemaspy.model.Routine;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * The page that lists all of the routines (stored procedures and functions)
 * in the schema.
 *
 * @author John Currier
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class HtmlRoutinesPage extends HtmlFormatter {

    private final HtmlConfig htmlConfig;

    public HtmlRoutinesPage(HtmlConfig htmlConfig) {
        this.htmlConfig = htmlConfig;
    }

    public void write(
            Database db,
            File outputDir
    ) {
        Collection<Routine> routines = new TreeSet<>(db.getRoutines());

        HashMap<String, Object> scopes = new HashMap<>();
        scopes.put("routines", routines);
        scopes.put("paginationEnabled", htmlConfig.isPaginationEnabled());

        MustacheWriter mw = new MustacheWriter(outputDir, scopes, getPathToRoot(), db.getName(), false);
        mw.write("routines.html", "routines.html", "routines.js");
    }

}