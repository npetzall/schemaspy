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

import org.schemaspy.output.diagram.DiagramProducer;
import org.schemaspy.output.diagram.DiagramResults;
import org.schemaspy.view.MustacheDiagram;

import java.io.File;

public class MustacheDiagramFactory {

    private final DiagramProducer diagramProducer;

    public MustacheDiagramFactory(DiagramProducer diagramProducer) {
        this.diagramProducer = diagramProducer;
    }

    public MustacheDiagram generateOrphanDiagram(String name, File dotFile, String diagramName) {
        DiagramResults results = diagramProducer.generateOrphanDiagram(dotFile, diagramName);
        MustacheDiagram mustacheDiagram = new MustacheDiagram(name, results);
        return mustacheDiagram;
    }

    public MustacheDiagram generateTableDiagram(String name, File dotFile, String diagramName) {
        DiagramResults results = diagramProducer.generateTableDiagram(dotFile, diagramName);
        MustacheDiagram mustacheDiagram = new MustacheDiagram(name, results);
        return mustacheDiagram;
    }

    public MustacheDiagram generateSummaryDiagram(String name, File dotFile, String diagramName) {
        DiagramResults results = diagramProducer.generateSummaryDiagram(dotFile, diagramName);
        MustacheDiagram mustacheDiagram = new MustacheDiagram(name, results);
        return mustacheDiagram;
    }

}
