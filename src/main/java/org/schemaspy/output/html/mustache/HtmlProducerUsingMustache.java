/*
 * Copyright (C) 2004-2011 John Currier
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.schemaspy.DbAnalyzer;
import org.schemaspy.model.*;
import org.schemaspy.output.html.HtmlConfig;
import org.schemaspy.output.html.HtmlProducer;
import org.schemaspy.output.html.HtmlProducerException;
import org.schemaspy.output.html.mustache.pages.*;
import org.schemaspy.util.Dot;
import org.schemaspy.util.LineWriter;
import org.schemaspy.util.ResourceWriter;
import org.schemaspy.view.DotFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.file.StandardOpenOption.*;

/**
 * @author John Currier
 * @author Nils Petzaell
 */
public class HtmlProducerUsingMustache implements HtmlProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final long SEC_IN_MS = 1000;

    private final ProgressListener progressListener;
    private final HtmlConfig htmlConfig;



    public HtmlProducerUsingMustache(HtmlConfig htmlConfig, ProgressListener progressListener) {
        this.htmlConfig = htmlConfig;
        this.progressListener = progressListener;
    }

    @Override
    public void generate(Database database, File outputDir) {

        Collection<Table> tables = new ArrayList<>(database.getTables());
        tables.addAll(database.getViews());

        if (tables.isEmpty()) {
            LOGGER.info("No tables to output, nothing written to disk");
            return;
        }

        prepareLayoutFiles(outputDir);

        MustacheCompiler mustacheCompiler = new MustacheCompiler(getDatabaseName(database), htmlConfig.getTemplateDirectory(), htmlConfig.isOneOfMultipleSchemas());

        HtmlRelationshipsPage htmlRelationshipsPage = new HtmlRelationshipsPage(mustacheCompiler);
        HtmlOrphansPage htmlOrphansPage = new HtmlOrphansPage(mustacheCompiler);
        HtmlMainIndexPage htmlMainIndexPage = new HtmlMainIndexPage(mustacheCompiler, htmlConfig);
        HtmlConstraintsPage htmlConstraintsPage = new HtmlConstraintsPage(mustacheCompiler, htmlConfig);
        HtmlAnomaliesPage htmlAnomaliesPage = new HtmlAnomaliesPage(mustacheCompiler, htmlConfig);
        HtmlColumnsPage htmlColumnsPage = new HtmlColumnsPage(mustacheCompiler, htmlConfig);
        HtmlRoutinesPage htmlRoutinesPage = new HtmlRoutinesPage(mustacheCompiler, htmlConfig);
        HtmlRoutinePage htmlRoutinePage = new HtmlRoutinePage(mustacheCompiler);
        HtmlTablePage htmlTablePage = new HtmlTablePage(mustacheCompiler, htmlConfig);
        HtmlComponentPage htmlComponentPage = new HtmlComponentPage(mustacheCompiler);

        progressListener.graphingSummaryProgressed();

        boolean showDetailedTables = tables.size() <= htmlConfig.getMaxDetailedTables();
        final boolean includeImpliedConstraints = htmlConfig.isImpliedConstraintsEnabled();

        // if evaluating a 'ruby on rails-based' database then connect the columns
        // based on RoR conventions
        // note that this is done before 'hasRealRelationships' gets evaluated so
        // we get a relationships ER diagram
        if (htmlConfig.isRailsEnabled())
            DbAnalyzer.getRailsConstraints(database.getTablesByName());

        File summaryDir = new File(outputDir, "diagrams/summary");
        try {
            // generate the compact form of the relationships .dot file
            String dotBaseFilespec = "relationships";
            WriteStats stats = new WriteStats(tables);
            try (LineWriter out = new LineWriter(new File(summaryDir, dotBaseFilespec + ".real.compact.dot"), StandardCharsets.UTF_8.name())) {
                DotFormatter.getInstance().writeRealRelationships(database, tables, true, showDetailedTables, stats, out, outputDir);
            }
            boolean hasRealRelationships = stats.getNumTablesWritten() > 0 || stats.getNumViewsWritten() > 0;

            if (hasRealRelationships) {
                // real relationships exist so generate the 'big' form of the relationships .dot file
                progressListener.graphingSummaryProgressed();
                try (LineWriter out = new LineWriter(new File(summaryDir, dotBaseFilespec + ".real.large.dot"), StandardCharsets.UTF_8.name())) {
                    DotFormatter.getInstance().writeRealRelationships(database, tables, false, showDetailedTables, stats, out, outputDir);
                }
            }

            // getting implied constraints has a side-effect of associating the parent/child tables, so don't do it
            // here unless they want that behavior
            List<ImpliedForeignKeyConstraint> impliedConstraints = new ArrayList();
            if (includeImpliedConstraints)
                impliedConstraints.addAll(DbAnalyzer.getImpliedConstraints(tables));

            List<Table> orphans = DbAnalyzer.getOrphans(tables);
            htmlConfig.setHasOrphans(!orphans.isEmpty() && Dot.getInstance().isValid());
            htmlConfig.setHasRoutines(!database.getRoutines().isEmpty());

            progressListener.graphingSummaryProgressed();

            File impliedDotFile = new File(summaryDir, dotBaseFilespec + ".implied.compact.dot");
            boolean hasImplied;
            try (LineWriter out = new LineWriter(impliedDotFile, StandardCharsets.UTF_8.name())) {
                hasImplied = DotFormatter.getInstance().writeAllRelationships(database, tables, true, showDetailedTables, stats, out, outputDir);
            }
            if (hasImplied) {
                impliedDotFile = new File(summaryDir, dotBaseFilespec + ".implied.large.dot");
                try (LineWriter out = new LineWriter(impliedDotFile, StandardCharsets.UTF_8.name())) {
                    DotFormatter.getInstance().writeAllRelationships(database, tables, false, showDetailedTables, stats, out, outputDir);
                }
            } else {
                Files.deleteIfExists(impliedDotFile.toPath());
            }

            try (Writer writer = createWriter(outputDir, "relationships")) {
                htmlRelationshipsPage.write(summaryDir, dotBaseFilespec, hasRealRelationships, hasImplied,
                        progressListener, writer);
            }

            progressListener.graphingSummaryProgressed();

            File orphansDir = new File(outputDir, "diagrams/orphans");
            FileUtils.forceMkdir(orphansDir);

            try (Writer writer = createWriter(outputDir, "orphans")) {
                htmlOrphansPage.write(orphans, orphansDir, outputDir,
                        writer);
            }

            progressListener.graphingSummaryProgressed();

            try (Writer writer = createWriter(outputDir, "index")) {
                htmlMainIndexPage.write(database, getDatabaseName(database), tables, impliedConstraints, writer);
            }
            progressListener.graphingSummaryProgressed();

            List<ForeignKeyConstraint> constraints = DbAnalyzer.getForeignKeyConstraints(tables);
            try (Writer writer = createWriter(outputDir, "constraints")) {
                htmlConstraintsPage.write(constraints, tables, writer);
            }

            progressListener.graphingSummaryProgressed();
            try (Writer writer = createWriter(outputDir, "anomalies")) {
                htmlAnomaliesPage.write(tables, impliedConstraints, writer);
            }
            progressListener.graphingSummaryProgressed();

            try (Writer writer = createWriter(outputDir, "columns")) {
                htmlColumnsPage.write(tables, writer);
            }

            progressListener.graphingSummaryProgressed();

            try (Writer writer = createWriter(outputDir, "routines")) {
                htmlRoutinesPage.write(database.getRoutines(), writer);
            }

            for (Routine routine : database.getRoutines()) {
                try (Writer writer = createWriter(outputDir, "routines/" + routine.getName())) {
                    htmlRoutinePage.write(routine, writer);
                }
            }

            // create detailed diagrams

            long duration = progressListener.startedGraphingDetails();

            LOGGER.info("Completed summary in {} seconds", duration / SEC_IN_MS);
            LOGGER.info("Writing/diagramming details");

            for (Table table : tables) {
                progressListener.graphingDetailsProgressed(table);
                LOGGER.debug("Writing details of {}", table.getName());
                try (Writer writer = createWriter(outputDir, "tables/" + table.getName())) {
                    htmlTablePage.write(database, table, outputDir, stats, writer);
                }
            }
            try (Writer writer = createWriter(outputDir, "components")) {
                htmlComponentPage.write(database, writer);
            }
        } catch (IOException ioException) {
            throw new HtmlProducerException("Failed to write html output for '"+ database.getName()+ "'", ioException);
        }
    }

    //TODO: I think this should be schemaName.
    private static String getDatabaseName(Database database) {
        StringBuilder databaseName = new StringBuilder();

        databaseName.append(database.getName());
        if (database.getSchema() != null) {
            databaseName.append('.');
            databaseName.append(database.getSchema().getName());
        } else if (database.getCatalog() != null) {
            databaseName.append('.');
            databaseName.append(database.getCatalog().getName());
        }

        return databaseName.toString();
    }

    /**
     * This method is responsible to copy layout folder to destination directory and not copy template .html files
     *
     * @param outputDir File
     * @throws HtmlProducerException when not possible to copy layout files to outputDir
     */
    private void prepareLayoutFiles(File outputDir) {
        try {
            URL url = null;
            Enumeration<URL> possibleResources = getClass().getClassLoader().getResources("layout");
            while (possibleResources.hasMoreElements() && Objects.isNull(url)) {
                URL possibleResource = possibleResources.nextElement();
                if (!possibleResource.getPath().contains("test-classes")) {
                    url = possibleResource;
                }
            }

            IOFileFilter notHtmlFilter = FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(".html"));
            FileFilter filter = FileFilterUtils.and(notHtmlFilter);
            ResourceWriter.copyResources(url, outputDir, filter);
        } catch (IOException ioException) {
            throw new HtmlProducerException("Failed to prepare output at '"+outputDir.getPath()+"'", ioException);
        }
    }

    private static Writer createWriter(File outputDir, String filename) throws IOException {
        Path destination = outputDir.toPath().resolve(filename + ".html");
        return Files.newBufferedWriter(destination, StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING, WRITE);
    }
}
