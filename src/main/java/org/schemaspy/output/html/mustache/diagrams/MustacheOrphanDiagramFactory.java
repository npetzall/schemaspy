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
package org.schemaspy.output.html.mustache.diagrams;

import org.schemaspy.model.Table;
import org.schemaspy.output.diagram.DiagramException;
import org.schemaspy.output.diagram.DiagramProducer;
import org.schemaspy.output.diagram.DiagramResults;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.output.dot.schemaspy.DotConfigHeader;
import org.schemaspy.output.dot.schemaspy.DotNode;
import org.schemaspy.output.dot.schemaspy.DotNodeConfig;
import org.schemaspy.output.dot.schemaspy.graph.Digraph;
import org.schemaspy.output.dot.schemaspy.graph.Element;
import org.schemaspy.output.dot.schemaspy.graph.Graph;
import org.schemaspy.util.Writers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.util.Collection;

/**
 * @author Nils Petzaell
 */
public class MustacheOrphanDiagramFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final DotConfig dotConfig;
    private final DiagramProducer diagramProducer;
    private final File outputDir;

    public MustacheOrphanDiagramFactory(DotConfig dotConfig, DiagramProducer diagramProducer, File outputDir) {
        this.dotConfig = dotConfig;
        this.diagramProducer = diagramProducer;
        this.outputDir = outputDir;
    }

    public DiagramResults generate(Collection<Table> orphanTables) {
        File dotFile = generateDot(orphanTables);
        return generateDiagram(dotFile);
    }

    private File generateDot(Collection<Table> orphanTables) {
        File dotFile = new File(outputDir, "orphans.dot");
        try (PrintWriter dotOut = Writers.newPrintWriter(dotFile)) {
            Graph graph = new Digraph(
                    ()->"orphans",
                    new DotConfigHeader(dotConfig, false),
                    orphanTables.stream()
                            .filter(t -> t.isOrphan(true) && !t.isView())
                            .sorted(Table::compareTo)
                            .map(this::asDotNode)
                            .toArray(Element[]::new)
            );
            dotOut.println(graph.dot());
            dotOut.flush();
        } catch (IOException e) {
            LOGGER.error("Failed to produce dot: {}", dotFile, e);
        }
        return dotFile;
    }

    private DotNode asDotNode(Table table) {
        return new DotNode(
                table,
                true,
                new DotNodeConfig(true, true),
                dotConfig
        );
    }

    private DiagramResults generateDiagram(File dotFile) {
        try {
            File diagramFile = new File(outputDir, "orphans" + "." + diagramProducer.getDiagramFormat());
            String diagramMap = diagramProducer.generateDiagram(dotFile, diagramFile);
            return new DiagramResults(diagramFile, diagramMap, diagramProducer.getDiagramFormat());
        } catch (DiagramException diagramException) {
            throw new DiagramException("Failed to generate Orphan diagram", diagramException);
        }
    }
}
