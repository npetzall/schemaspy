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
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.output.dot.schemaspy.*;
import org.schemaspy.output.dot.schemaspy.graph.Digraph;
import org.schemaspy.output.dot.schemaspy.graph.Element;
import org.schemaspy.output.dot.schemaspy.graph.Orphan;
import org.schemaspy.output.dot.schemaspy.graph.Graph;
import org.schemaspy.util.Writers;
import org.schemaspy.view.FileNameGenerator;
import org.schemaspy.view.MustacheTableDiagram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Nils Petzaell
 */
public class MustacheOrphanDiagramFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final DotConfig dotConfig;
    private final MustacheDiagramFactory mustacheDiagramFactory;
    private final Path orphanDir;

    public MustacheOrphanDiagramFactory(DotConfig dotConfig, MustacheDiagramFactory mustacheDiagramFactory, File outputDir) {
        this.dotConfig = dotConfig;
        this.mustacheDiagramFactory = mustacheDiagramFactory;
        orphanDir = outputDir.toPath();
    }

    public List<MustacheTableDiagram> generateOrphanDiagrams(List<Table> orphanTables) {
        List<MustacheTableDiagram> mustacheTableDiagrams = new ArrayList<>();
        File dotFile = orphanDir.resolve("orphans.dot").toFile();
        try (PrintWriter dotOut = Writers.newPrintWriter(dotFile)) {
            Graph graph = new Digraph(
                    ()->"orphans",
                    new DotConfigHeader(dotConfig, false),
                    orphanTables.stream().map(table ->
                            new DotNode(
                                    table,
                                    true,
                                    new DotNodeConfig(true, true),
                                    dotConfig
                            )
                    ).toArray(Element[]::new)
            );
            dotOut.println(graph.dot());
            dotOut.flush();

            mustacheTableDiagrams.add(mustacheDiagramFactory.generateOrphanDiagram("orphans", dotFile, "orphans"));
        } catch (IOException e) {
            LOGGER.error("Failed to produce dot: {}", dotFile, e);
        } catch (DiagramException e) {
            LOGGER.error("Failed to produce diagram for: {}", dotFile, e);
        }
        return mustacheTableDiagrams;
    }
}
