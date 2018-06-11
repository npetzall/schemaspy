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
package org.schemaspy.view;

import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.util.DiagramUtil;
import org.schemaspy.util.Dot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The page that contains the overview entity relationship diagrams.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 */
public class HtmlRelationshipsPage extends HtmlDiagramFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final HtmlRelationshipsPage instance = new HtmlRelationshipsPage();

    /**
     * Singleton: Don't allow instantiation
     */
    private HtmlRelationshipsPage() {
    }

    /**
     * Singleton accessor
     *
     * @return the singleton instance
     */
    public static HtmlRelationshipsPage getInstance() {
        return instance;
    }

    public boolean write(
            Database db,
            File diagramDir,
            String dotBaseFilespec,
            boolean hasRealRelationships,
            boolean hasImpliedRelationships,
    		ProgressListener listener,
            File outputDir
    ) {
        try {
            Dot dot = getDot();

            if (dot == null) //if null mean that it was problem with dot Graphviz initialization
                return false;

            File compactRelationshipsDotFile = new File(diagramDir, dotBaseFilespec + ".real.compact.dot");
            File compactRelationshipsDiagramFile = new File(diagramDir, dotBaseFilespec + ".real.compact." + dot.getFormat());
            File largeRelationshipsDotFile = new File(diagramDir, dotBaseFilespec + ".real.large.dot");
            File largeRelationshipsDiagramFile = new File(diagramDir, dotBaseFilespec + ".real.large." + dot.getFormat());
            File compactImpliedDotFile = new File(diagramDir, dotBaseFilespec + ".implied.compact.dot");
            File compactImpliedDiagramFile = new File(diagramDir, dotBaseFilespec + ".implied.compact." + dot.getFormat());
            File largeImpliedDotFile = new File(diagramDir, dotBaseFilespec + ".implied.large.dot");
            File largeImpliedDiagramFile = new File(diagramDir, dotBaseFilespec + ".implied.large." + dot.getFormat());

            List<MustacheTableDiagram> diagrams = new ArrayList<>();

            if (hasRealRelationships) {
                generateRelationshipDiagrams(listener, dot, compactRelationshipsDotFile, compactRelationshipsDiagramFile, largeRelationshipsDotFile, largeRelationshipsDiagramFile, diagrams);
            }

            if (hasImpliedRelationships) {
                generateImpliedRelationshipDiagrams(listener, dot, compactImpliedDotFile, compactImpliedDiagramFile, largeImpliedDotFile, largeImpliedDiagramFile, diagrams);
            }

        	listener.graphingSummaryProgressed();

            DiagramUtil.markFirstAsActive(diagrams);

            String graphvizVersion = Dot.getInstance().getSupportedVersions().substring(4);
            Object graphvizExists = dot;
            HashMap<String, Object> scopes = new HashMap<>();
            scopes.put("graphvizExists", graphvizExists);
            scopes.put("graphvizVersion", graphvizVersion);
            scopes.put("diagramExists", DiagramUtil.diagramExists(diagrams));
            scopes.put("hasOnlyImpliedRelationships", hasOnlyImpliedRelationships(hasRealRelationships, hasImpliedRelationships));
            scopes.put("anyRelationships", anyRelationships(hasRealRelationships, hasImpliedRelationships));
            scopes.put("diagrams", diagrams);

            MustacheWriter mw = new MustacheWriter(outputDir, scopes, getPathToRoot(), db.getName(), false);
            mw.write("relationships.html", "relationships.html", "relationships.js");
            return true;
        } catch (IOException ioExc) {
            LOGGER.error("Failed to generate output for relationship page", ioExc);
            return false;
        }
    }

    private static void generateRelationshipDiagrams(ProgressListener listener, Dot dot, File compactRelationshipsDotFile, File compactRelationshipsDiagramFile, File largeRelationshipsDotFile, File largeRelationshipsDiagramFile, List<MustacheTableDiagram> diagrams) throws IOException {
        listener.graphingSummaryProgressed();
        try {
            DiagramUtil.generateDiagram("Compact", dot, compactRelationshipsDotFile, compactRelationshipsDiagramFile, diagrams, false, false);
        } catch (Dot.DotFailure dotFailure) {
            LOGGER.error("Failed to render compact relationship diagrams", dotFailure);
        }
        listener.graphingSummaryProgressed();
        try {
            DiagramUtil.generateDiagram("Large", dot, largeRelationshipsDotFile, largeRelationshipsDiagramFile, diagrams, false, false);
        } catch (Dot.DotFailure dotFailure) {
            LOGGER.error("Failed to render large relationship diagram", dotFailure);
        }
    }

    private static void generateImpliedRelationshipDiagrams(ProgressListener listener, Dot dot, File compactImpliedDotFile, File compactImpliedDiagramFile, File largeImpliedDotFile, File largeImpliedDiagramFile, List<MustacheTableDiagram> diagrams) throws IOException {
        listener.graphingSummaryProgressed();
        try {
            DiagramUtil.generateDiagram("Compact Implied", dot, compactImpliedDotFile, compactImpliedDiagramFile, diagrams, false, true);
        } catch (Dot.DotFailure dotFailure) {
            LOGGER.error("Failed to render compact implied relationship diagrams", dotFailure);
        }
        listener.graphingSummaryProgressed();
        try {
            DiagramUtil.generateDiagram("Large Implied", dot, largeImpliedDotFile, largeImpliedDiagramFile, diagrams, false, true);
        } catch (Dot.DotFailure dotFailure) {
            LOGGER.error("Failed to render large implied relationship diagrams", dotFailure);
        }
    }

    private static Object hasOnlyImpliedRelationships(boolean hasRealRelationships, boolean hasImpliedRelationships) {
        return !hasRealRelationships && hasImpliedRelationships ? new Object() : null;
    }

    private static Object anyRelationships(boolean hasRealRelationships, boolean hasImpliedRelationships) {
        return !hasRealRelationships && !hasImpliedRelationships ? new Object() : null;
    }
}