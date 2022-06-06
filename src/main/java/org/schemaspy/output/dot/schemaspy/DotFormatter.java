/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
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
package org.schemaspy.output.dot.schemaspy;

import org.schemaspy.model.Table;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.output.dot.schemaspy.link.TableNodeLinkFactory;
import org.schemaspy.output.dot.schemaspy.relationship.ImpliedRelationships;
import org.schemaspy.output.dot.schemaspy.relationship.RealRelationships;
import org.schemaspy.view.WriteStats;

import java.io.PrintWriter;

/**
 * Format table data into .dot format to feed to Graphvis' dot program.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Nils Petzaell
 */
public class DotFormatter {

    private final DotConfig dotConfig;
    private final TableNodeLinkFactory tableNodeLinkFactory;

    public DotFormatter(DotConfig dotConfig, TableNodeLinkFactory tableNodeLinkFactory) {
        this.dotConfig = dotConfig;
        this.tableNodeLinkFactory = tableNodeLinkFactory;
    }

    public void writeTableRealRelationships(Table table, boolean twoDegreesOfSeparation, WriteStats stats, PrintWriter dot) {
        new RealRelationships(dotConfig, tableNodeLinkFactory, table, twoDegreesOfSeparation, stats, dot).write();
    }

    public void writeTableAllRelationships(Table table, boolean twoDegreesOfSeparation, WriteStats stats, PrintWriter dot) {
        new ImpliedRelationships(dotConfig, tableNodeLinkFactory, table, twoDegreesOfSeparation, stats, dot).write();
    }
}
