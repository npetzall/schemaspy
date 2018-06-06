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
import org.schemaspy.Config;
import org.schemaspy.DbAnalyzer;
import org.schemaspy.model.*;
import org.schemaspy.output.html.HtmlProducer;
import org.schemaspy.output.html.HtmlProducerException;
import org.schemaspy.util.Dot;
import org.schemaspy.util.LineWriter;
import org.schemaspy.util.ResourceWriter;
import org.schemaspy.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

/**
 * @author John Currier
 * @author Nils Petzaell
 */
public class HtmlProducerUsingMustache implements HtmlProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ProgressListener progressListener;
    private final Config config;

    public HtmlProducerUsingMustache(ProgressListener progressListener, Config config) {
        this.progressListener = progressListener;
        this.config = config;
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

        progressListener.graphingSummaryProgressed();

        boolean showDetailedTables = tables.size() <= config.getMaxDetailedTables();
        final boolean includeImpliedConstraints = config.isImpliedConstraintsEnabled();

        // if evaluating a 'ruby on rails-based' database then connect the columns
        // based on RoR conventions
        // note that this is done before 'hasRealRelationships' gets evaluated so
        // we get a relationships ER diagram
        if (config.isRailsEnabled())
            DbAnalyzer.getRailsConstraints(database.getTablesByName());

        File summaryDir = new File(outputDir, "diagrams/summary");
        try {
            // generate the compact form of the relationships .dot file
            String dotBaseFilespec = "relationships";
            WriteStats stats = new WriteStats(tables);
            try (LineWriter out = new LineWriter(new File(summaryDir, dotBaseFilespec + ".real.compact.dot"), Config.DOT_CHARSET)) {
                DotFormatter.getInstance().writeRealRelationships(database, tables, true, showDetailedTables, stats, out, outputDir);
            }
            boolean hasRealRelationships = stats.getNumTablesWritten() > 0 || stats.getNumViewsWritten() > 0;

            if (hasRealRelationships) {
                // real relationships exist so generate the 'big' form of the relationships .dot file
                progressListener.graphingSummaryProgressed();
                try (LineWriter out = new LineWriter(new File(summaryDir, dotBaseFilespec + ".real.large.dot"), Config.DOT_CHARSET)) {
                    DotFormatter.getInstance().writeRealRelationships(database, tables, false, showDetailedTables, stats, out, outputDir);
                }
            }

            // getting implied constraints has a side-effect of associating the parent/child tables, so don't do it
            // here unless they want that behavior
            List<ImpliedForeignKeyConstraint> impliedConstraints = new ArrayList();
            if (includeImpliedConstraints)
                impliedConstraints.addAll(DbAnalyzer.getImpliedConstraints(tables));

            List<Table> orphans = DbAnalyzer.getOrphans(tables);
            config.setHasOrphans(!orphans.isEmpty() && Dot.getInstance().isValid());
            config.setHasRoutines(!database.getRoutines().isEmpty());

            progressListener.graphingSummaryProgressed();

            File impliedDotFile = new File(summaryDir, dotBaseFilespec + ".implied.compact.dot");
            boolean hasImplied;
            try (LineWriter out = new LineWriter(impliedDotFile, Config.DOT_CHARSET)) {
                hasImplied = DotFormatter.getInstance().writeAllRelationships(database, tables, true, showDetailedTables, stats, out, outputDir);
            }
            Set<TableColumn> excludedColumns = stats.getExcludedColumns();
            if (hasImplied) {
                impliedDotFile = new File(summaryDir, dotBaseFilespec + ".implied.large.dot");
                try (LineWriter out = new LineWriter(impliedDotFile, Config.DOT_CHARSET)) {
                    DotFormatter.getInstance().writeAllRelationships(database, tables, false, showDetailedTables, stats, out, outputDir);
                }
            } else {
                Files.deleteIfExists(impliedDotFile.toPath());
            }

            HtmlRelationshipsPage.getInstance().write(database, summaryDir, dotBaseFilespec, hasRealRelationships, hasImplied, excludedColumns,
                    progressListener, outputDir);

            progressListener.graphingSummaryProgressed();

            File orphansDir = new File(outputDir, "diagrams/orphans");
            FileUtils.forceMkdir(orphansDir);
            HtmlOrphansPage.getInstance().write(database, orphans, orphansDir, outputDir);

            progressListener.graphingSummaryProgressed();

            HtmlMainIndexPage.getInstance().write(database, tables, impliedConstraints, outputDir);

            progressListener.graphingSummaryProgressed();

            List<ForeignKeyConstraint> constraints = DbAnalyzer.getForeignKeyConstraints(tables);
            HtmlConstraintsPage constraintIndexFormatter = HtmlConstraintsPage.getInstance();
            constraintIndexFormatter.write(database, constraints, tables, outputDir);

            progressListener.graphingSummaryProgressed();

            HtmlAnomaliesPage.getInstance().write(database, tables, impliedConstraints, outputDir);

            progressListener.graphingSummaryProgressed();

            for (HtmlColumnsPage.ColumnInfo columnInfo : HtmlColumnsPage.getInstance().getColumnInfos().values()) {
                HtmlColumnsPage.getInstance().write(database, tables, columnInfo, outputDir);
            }

            progressListener.graphingSummaryProgressed();

            HtmlRoutinesPage.getInstance().write(database, outputDir);

            // create detailed diagrams

            long duration = progressListener.startedGraphingDetails();

            LOGGER.info("Completed summary in {} seconds", duration / 1000);
            LOGGER.info("Writing/diagramming details");

            generateTables(progressListener, outputDir, database, tables, stats);
            HtmlComponentPage.getInstance().write(database, tables, outputDir);
        } catch (IOException ioException) {
            throw new HtmlProducerException("Failed to write html output for '"+ database.getName()+ "'", ioException);
        }
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

    private void generateTables(ProgressListener progressListener, File outputDir, Database database, Collection<Table> tables, WriteStats stats) throws IOException {
        HtmlTablePage tableFormatter = HtmlTablePage.getInstance();
        for (Table table : tables) {
            progressListener.graphingDetailsProgressed(table);
            LOGGER.debug("Writing details of {}", table.getName());

            tableFormatter.write(database, table, outputDir, stats);
        }
    }
}
