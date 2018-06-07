/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
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
package org.schemaspy.output.html.mustache.pages;

import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.output.html.mustache.MustacheWriter;
import org.schemaspy.output.html.mustache.dto.MustacheTable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * The main index that contains all tables and views that were evaluated
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 */
public class HtmlComponentPage extends HtmlFormatter {

    public void write(Database database, Collection<Table> tables, File outputDir) {
        List<MustacheTable> mustacheTables = new ArrayList<>();

        for(Table table: tables) {
            String comment = table.getComments();
            if (comment != null) {
//                InputStream inputStream = IOUtils.toInputStream(comment, "UTF-8");
//                YamlDecoder dec = new YamlDecoder(inputStream);
//                TableComment tableComment = dec.readObjectOfType(TableComment.class);
//                String comments = Markdown.toHtml(tableComment.getDoc(),"");
            }
        }

        HashMap<String, Object> scopes = new HashMap<>();
        scopes.put("tables", mustacheTables);
        scopes.put("database", database);

        MustacheWriter mw = new MustacheWriter(outputDir, scopes, "", database.getName(), false);
        mw.write("components.html", "components.html", "components.js");
    }


}
