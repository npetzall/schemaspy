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

public class HtmlProducerUsingMustache implements HtmlProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ProgressListener progressListener;

    public HtmlProducerUsingMustache(ProgressListener progressListener){
        this.progressListener = progressListener;
    }

    @Override
    public void generate(Database database, File outputDir) {
        Config config = Config.getInstance();
        Collection<Table> tables = new ArrayList<>(database.getTables());
        tables.addAll(database.getViews());
        LineWriter out;

        LOGGER.info("Writing/graphing summary");

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

        // generate the compact form of the relationships .dot file
        String dotBaseFilespec = "relationships";
        WriteStats stats = new WriteStats(tables);
        try {
            out = new LineWriter(new File(summaryDir, dotBaseFilespec + ".real.compact.dot"), Config.DOT_CHARSET);
            DotFormatter.getInstance().writeRealRelationships(database, tables, true, showDetailedTables, stats, out, outputDir);
            out.close();
        } catch (IOException e) {
            throw new HtmlProducerException("Failed to write real compact relationships", e);
        }
        boolean hasRealRelationships = stats.getNumTablesWritten() > 0 || stats.getNumViewsWritten() > 0;
        if (hasRealRelationships) {
            // real relationships exist so generate the 'big' form of the relationships .dot file
            progressListener.graphingSummaryProgressed();
            try {
                out = new LineWriter(new File(summaryDir, dotBaseFilespec + ".real.large.dot"), Config.DOT_CHARSET);
                DotFormatter.getInstance().writeRealRelationships(database, tables, false, showDetailedTables, stats, out, outputDir);
                out.close();
            } catch (IOException e) {
                throw new HtmlProducerException("Failed to write real large relationships", e);
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
        boolean hasImplied;
        File impliedDotFile = new File(summaryDir, dotBaseFilespec + ".implied.compact.dot");
        try {
            out = new LineWriter(impliedDotFile, Config.DOT_CHARSET);
            hasImplied = DotFormatter.getInstance().writeAllRelationships(database, tables, true, showDetailedTables, stats, out, outputDir);
            out.close();
        } catch (IOException e) {
            throw new HtmlProducerException("Failed to write implied compact relationships", e);
        }

        Set<TableColumn> excludedColumns = stats.getExcludedColumns();
        if (hasImplied) {
            impliedDotFile = new File(summaryDir, dotBaseFilespec + ".implied.large.dot");
            try {
                out = new LineWriter(impliedDotFile, Config.DOT_CHARSET);
                DotFormatter.getInstance().writeAllRelationships(database, tables, false, showDetailedTables, stats, out, outputDir);
                out.close();
            } catch (IOException e) {
                throw new HtmlProducerException("Failed to write implied large relationships", e);
            }
        } else {
            try {
                Files.deleteIfExists(impliedDotFile.toPath());
            } catch (IOException e) {
                throw new HtmlProducerException("Failed to delete implied compact relationships", e);
            }
        }

        HtmlRelationshipsPage.getInstance().write(database, summaryDir, dotBaseFilespec, hasRealRelationships, hasImplied, excludedColumns,
                progressListener, outputDir);

        progressListener.graphingSummaryProgressed();

        File orphansDir = new File(outputDir, "diagrams/orphans");
        try {
            FileUtils.forceMkdir(orphansDir);
            HtmlOrphansPage.getInstance().write(database, orphans, orphansDir, outputDir);
        } catch (IOException e) {
            throw new HtmlProducerException("Failed to generate orphans diagram/page", e);
        }

        progressListener.graphingSummaryProgressed();


        HtmlMainIndexPage.getInstance().write(database, tables, impliedConstraints, outputDir);


        progressListener.graphingSummaryProgressed();

        List<ForeignKeyConstraint> constraints = DbAnalyzer.getForeignKeyConstraints(tables);
        HtmlConstraintsPage constraintIndexFormatter = HtmlConstraintsPage.getInstance();
        try {
            constraintIndexFormatter.write(database, constraints, tables, outputDir);
        } catch (IOException e) {
            throw new HtmlProducerException("Failed to generate constraints page", e);
        }

        progressListener.graphingSummaryProgressed();

        try {
            HtmlAnomaliesPage.getInstance().write(database, tables, impliedConstraints, outputDir);
        } catch (IOException e) {
            throw new HtmlProducerException("Failed to generate anomalies page", e);
        }

        progressListener.graphingSummaryProgressed();

        for (HtmlColumnsPage.ColumnInfo columnInfo : HtmlColumnsPage.getInstance().getColumnInfos().values()) {
            try {
                HtmlColumnsPage.getInstance().write(database, tables, columnInfo, outputDir);
            } catch (IOException e) {
                throw new HtmlProducerException("Failed to generate column pages", e);
            }
        }

        progressListener.graphingSummaryProgressed();

        try {
            HtmlRoutinesPage.getInstance().write(database, outputDir);
        } catch (IOException e) {
            throw new HtmlProducerException("Failed to generate routines page", e);
        }

        // create detailed diagrams

        long duration = progressListener.startedGraphingDetails();

        LOGGER.info("Completed summary in {} seconds", duration / 1000);
        LOGGER.info("Writing/diagramming details");

        generateTables(outputDir, database, tables, stats);
        try {
            HtmlComponentPage.getInstance().write(database, tables, outputDir);
        } catch (IOException e) {
            throw new HtmlProducerException("Failed to generate component page", e);
        }
    }

    /**
     * This method is responsible to copy layout folder to destination directory and not copy template .html files
     *
     * @param outputDir File
     * @throws IOException when not possible to copy layout files to outputDir
     */

    public static void prepareLayoutFiles(File outputDir) {
        try {
            URL url = null;
            Enumeration<URL> possibleResources = HtmlProducerUsingMustache.class.getClassLoader().getResources("layout");
            while (possibleResources.hasMoreElements() && Objects.isNull(url)) {
                URL possibleResource = possibleResources.nextElement();
                if (!possibleResource.getPath().contains("test-classes")) {
                    url = possibleResource;
                }
            }

            IOFileFilter notHtmlFilter = FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(".html"));
            FileFilter filter = FileFilterUtils.and(notHtmlFilter);
            ResourceWriter.copyResources(url, outputDir, filter);
        } catch (IOException e) {
            throw new HtmlProducerException("Failed to prepare layout for html", e);
        }
    }

    private void generateTables(File outputDir, Database db, Collection<Table> tables, WriteStats stats) {
        HtmlTablePage tableFormatter = HtmlTablePage.getInstance();
        for (Table table : tables) {
            progressListener.graphingDetailsProgressed(table);
            LOGGER.debug("Writing details of {}", table.getName());

            try {
                tableFormatter.write(db, table, outputDir, stats);
            } catch (IOException e) {
                throw new HtmlProducerException("Failed to write table " + table.getFullName(), e);
            }
        }
    }
}
