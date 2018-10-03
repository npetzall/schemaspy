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
package org.schemaspy.output.html.mustache;

import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.util.Writers;
import org.schemaspy.view.DotFormatter;
import org.schemaspy.view.MustacheDiagram;
import org.schemaspy.view.WriteStats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MustacheTableDiagramFactory {

    private final MustacheDiagramFactory mustacheDiagramFactory;
    private final File outputDir;
    private final File diagramDir;

    public MustacheTableDiagramFactory(MustacheDiagramFactory mustacheDiagramFactory, File outputDir) {
        this.mustacheDiagramFactory = mustacheDiagramFactory;
        this.outputDir = outputDir;
        this.diagramDir = new File(outputDir, "diagrams");
    }

    public List<MustacheDiagram> generateTableDiagrams(Table table, WriteStats stats) throws IOException {
        List<MustacheDiagram> diagrams = new ArrayList<>();
        File oneDegreeDotFile = new File(diagramDir, table.getName() + ".1degree.dot");
        File twoDegreesDotFile = new File(diagramDir, table.getName() + ".2degrees.dot");
        File oneImpliedDotFile = new File(diagramDir, table.getName() + ".implied1degrees.dot");
        File twoImpliedDotFile = new File(diagramDir, table.getName() + ".implied2degrees.dot");

        // delete before we start because we'll use the existence of these files to determine
        // if they should be turned into pngs & presented
        Files.deleteIfExists(oneDegreeDotFile.toPath());
        Files.deleteIfExists(twoDegreesDotFile.toPath());
        Files.deleteIfExists(oneImpliedDotFile.toPath());
        Files.deleteIfExists(twoImpliedDotFile.toPath());


        if (table.getMaxChildren() + table.getMaxParents() > 0) {
            Set<ForeignKeyConstraint> impliedConstraints;

            DotFormatter formatter = DotFormatter.getInstance();

            WriteStats oneStats = new WriteStats(stats);
            try (PrintWriter dotOut = Writers.newPrintWriter(oneDegreeDotFile)) {
                formatter.writeRealRelationships(table, false, oneStats, dotOut, outputDir);
            }
            MustacheDiagram oneDiagram = mustacheDiagramFactory.generateTableDiagram("One", oneDegreeDotFile, table.getName() + ".1degree");
            oneDiagram.setActive(true);
            diagrams.add(oneDiagram);

            WriteStats twoStats = new WriteStats(stats);
            try (PrintWriter dotOut = Writers.newPrintWriter(twoDegreesDotFile)) {
                impliedConstraints = formatter.writeRealRelationships(table, true, twoStats, dotOut, outputDir);
            }

            if (sameWritten(oneStats, twoStats)) {
                Files.deleteIfExists(twoDegreesDotFile.toPath()); // no different than before, so don't show it
            } else {
                diagrams.add(mustacheDiagramFactory.generateTableDiagram("Two degrees", twoDegreesDotFile, table.getName() + ".2degrees"));
            }

            if (!impliedConstraints.isEmpty()) {
                WriteStats oneImplied = new WriteStats(stats);
                try (PrintWriter dotOut = Writers.newPrintWriter(oneImpliedDotFile)) {
                    formatter.writeAllRelationships(table, false, oneImplied, dotOut, outputDir);
                }
                MustacheDiagram oneImpliedDiagram = mustacheDiagramFactory.generateTableDiagram("One implied", oneImpliedDotFile, table.getName() + ".implied1degrees");
                oneImpliedDiagram.setImplied(true);
                diagrams.add(oneImpliedDiagram);
                WriteStats twoImplied = new WriteStats(stats);
                try (PrintWriter dotOut = Writers.newPrintWriter(twoImpliedDotFile)) {
                    formatter.writeAllRelationships(table, true, twoImplied, dotOut, outputDir);
                }
                if (sameWritten(oneImplied, twoImplied)) {
                    Files.deleteIfExists(twoImpliedDotFile.toPath());
                } else {
                    MustacheDiagram twoImpliedDiagram = mustacheDiagramFactory.generateTableDiagram("Two implied", twoImpliedDotFile, table.getName() + ".implied2degrees");
                    twoImpliedDiagram.setImplied(true);
                    diagrams.add(twoImpliedDiagram);
                }
                return diagrams;
            }
        }

        return diagrams;
    }

    private static boolean sameWritten(WriteStats first, WriteStats second) {
        return first.getNumTablesWritten() + first.getNumViewsWritten() == second.getNumTablesWritten() + second.getNumViewsWritten();
    }

}
