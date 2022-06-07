/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2018 Nils Petzaell
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

import org.schemaspy.model.Table;
import org.schemaspy.output.diagram.DiagramResults;
import org.schemaspy.output.html.mustache.diagrams.MustacheOrphanDiagramFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.Collection;

/**
 * The page that contains the all tables that aren't related to others (orphans)
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Wojciech Kasa
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class HtmlOrphansPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final MustacheCompiler mustacheCompiler;
    private final MustacheOrphanDiagramFactory mustacheOrphanDiagramFactory;
    private final Collection<Table> orphanTables;

    public HtmlOrphansPage(
            MustacheCompiler mustacheCompiler,
            MustacheOrphanDiagramFactory mustacheOrphanDiagramFactory,
            Collection<Table> orphanTables
    ) {
        this.mustacheCompiler = mustacheCompiler;
        this.mustacheOrphanDiagramFactory = mustacheOrphanDiagramFactory;
        this.orphanTables = orphanTables;
    }

    public void write(Writer writer) {
        PageData pageData = createPageData();
        try {
            mustacheCompiler.write(pageData, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write orphans page", e);
        }
    }

    private PageData createPageData() {
        if (orphanTables.isEmpty()) {
            return new PageData.Builder()
                    .templateName("orphans.html")
                    .addToScope("diagram", null)
                    .getPageData();
        }

        DiagramResults orphansDiagram = mustacheOrphanDiagramFactory.generate(orphanTables);

        return new PageData.Builder()
                .templateName("orphans.html")
                .addToScope("diagram", orphansDiagram)
                .getPageData();
    }
}
