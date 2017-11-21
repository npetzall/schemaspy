package org.schemaspy.output.html;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.schemaspy.Config;
import org.schemaspy.analyzer.DbAnalyzer;
import org.schemaspy.model.*;
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
import java.util.*;

public class DotHtmlProducer implements HtmlProducer {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final HtmlConfig config;
    private final ProgressListener progressListener;

    public DotHtmlProducer(HtmlConfig config, ProgressListener progressListener) {
        this.config = Objects.requireNonNull(config);
        this.progressListener = Objects.requireNonNull(progressListener);
    }

    @Override
    public void generate(Database database) {
        Collection<Table> tables = new ArrayList<Table>(database.getTables());
        tables.addAll(database.getViews());
        new File(config.getOutputDir(), "tables").mkdirs();
        new File(config.getOutputDir(), "diagrams/summary").mkdirs();
        try {
            generateHtmlDoc(progressListener, database, tables);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        if (config.isHtmlGenerationEnabled()) {
            LOG.info("Wrote table details in " + duration / 1000 + " seconds");

            if (LOG.isInfoEnabled()) {
                LOG.info("Wrote relationship details of " + tables.size() + " tables/views to directory '" + config.getOutputDir() + "' in " + overallDuration / 1000 + " seconds.");
                LOG.info("View the results by opening " + new File(config.getOutputDir(), "index.html"));
            }
        }
        */
    }

    private void generateHtmlDoc(ProgressListener progressListener, Database db, Collection<Table> tables) throws IOException {
        LineWriter out;
        //logger.info("Gathered schema details in " + duration / 1000 + " seconds");
        //logger.info("Writing/graphing summary");

        prepareLayoutFiles();

        progressListener.graphingSummaryProgressed();

        boolean showDetailedTables = tables.size() <= config.getDetailedTablesLimit();
        final boolean includeImpliedConstraints = config.isIncludeImpliedConstraints();

        // if evaluating a 'ruby on rails-based' database then connect the columns
        // based on RoR conventions
        // note that this is done before 'hasRealRelationships' gets evaluated so
        // we get a relationships ER diagram
        //TODO Totally BAD
        if (config.isIncludeRailsConstraints())
            DbAnalyzer.getRailsConstraints(db.getTablesByName());

        File summaryDir = new File(config.getOutputDir(), "diagrams/summary");

        // generate the compact form of the relationships .dot file
        String dotBaseFilespec = "relationships";
        out = new LineWriter(new File(summaryDir, dotBaseFilespec + ".real.compact.dot"), Config.DOT_CHARSET);
        WriteStats stats = new WriteStats(tables);
        DotFormatter.getInstance().writeRealRelationships(db, tables, true, showDetailedTables, stats, out, config.getOutputDir());
        boolean hasRealRelationships = stats.getNumTablesWritten() > 0 || stats.getNumViewsWritten() > 0;
        out.close();

        if (hasRealRelationships) {
            // real relationships exist so generate the 'big' form of the relationships .dot file
            progressListener.graphingSummaryProgressed();
            out = new LineWriter(new File(summaryDir, dotBaseFilespec + ".real.large.dot"), Config.DOT_CHARSET);
            DotFormatter.getInstance().writeRealRelationships(db, tables, false, showDetailedTables, stats, out, config.getOutputDir());
            out.close();
        }

        // getting implied constraints has a side-effect of associating the parent/child tables, so don't do it
        // here unless they want that behavior
        //TODO Totally bad
        List<ImpliedForeignKeyConstraint> impliedConstraints;
        if (includeImpliedConstraints)
            impliedConstraints = DbAnalyzer.getImpliedConstraints(tables);
        else
            impliedConstraints = new ArrayList<>();

        List<Table> orphans = DbAnalyzer.getOrphans(tables);

        progressListener.graphingSummaryProgressed();

        File impliedDotFile = new File(summaryDir, dotBaseFilespec + ".implied.compact.dot");
        out = new LineWriter(impliedDotFile, Config.DOT_CHARSET);
        boolean hasImplied = DotFormatter.getInstance().writeAllRelationships(db, tables, true, showDetailedTables, stats, out, config.getOutputDir());

        Set<TableColumn> excludedColumns = stats.getExcludedColumns();
        out.close();
        if (hasImplied) {
            impliedDotFile = new File(summaryDir, dotBaseFilespec + ".implied.large.dot");
            out = new LineWriter(impliedDotFile, Config.DOT_CHARSET);
            DotFormatter.getInstance().writeAllRelationships(db, tables, false, showDetailedTables, stats, out, config.getOutputDir());
            out.close();
        } else {
            impliedDotFile.delete();
        }

        HtmlRelationshipsPage.getInstance().write(db, summaryDir, dotBaseFilespec, hasRealRelationships, hasImplied, excludedColumns,
                progressListener, config.getOutputDir());

        progressListener.graphingSummaryProgressed();

        File orphansDir = new File(config.getOutputDir(), "diagrams/orphans");
        orphansDir.mkdirs();
        HtmlOrphansPage.getInstance().write(db, orphans, orphansDir, config.getOutputDir());
        out.close();

        progressListener.graphingSummaryProgressed();

        HtmlMainIndexPage.getInstance().write(db, tables, db.getRemoteTables(), config.getOutputDir());

        progressListener.graphingSummaryProgressed();

        List<ForeignKeyConstraint> constraints = DbAnalyzer.getForeignKeyConstraints(tables);
        HtmlConstraintsPage constraintIndexFormatter = HtmlConstraintsPage.getInstance();
        constraintIndexFormatter.write(db, constraints, tables, config.getOutputDir());

        progressListener.graphingSummaryProgressed();

        HtmlAnomaliesPage.getInstance().write(db, tables, impliedConstraints, config.getOutputDir());

        progressListener.graphingSummaryProgressed();

        for (HtmlColumnsPage.ColumnInfo columnInfo : HtmlColumnsPage.getInstance().getColumnInfos().values()) {
            HtmlColumnsPage.getInstance().write(db, tables, columnInfo, config.getOutputDir());
        }

        progressListener.graphingSummaryProgressed();

        out = new LineWriter(new File(config.getOutputDir(), "routines.html"), 16 * 1024, Config.getInstance().getCharset());
        HtmlRoutinesPage.getInstance().write(db, out);
        out.close();

        // create detailed diagrams

        //duration = progressListener.startedGraphingDetails();

        //logger.info("Completed summary in " + duration / 1000 + " seconds");
        //logger.info("Writing/diagramming details");

        generateTables(progressListener, db, tables, stats);
        HtmlComponentPage.getInstance().write(db, tables, config.getOutputDir());
    }

    private void prepareLayoutFiles() throws IOException {
        URL url = getClass().getResource("/layout");
        File directory = new File(url.getPath());

        IOFileFilter notHtmlFilter = FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(".html"));
        FileFilter filter = FileFilterUtils.and(notHtmlFilter);
        //cleanDirectory(config.getOutputDir(),"/diagrams");
        //cleanDirectory(config.getOutputDir(),"/tables");
        ResourceWriter.copyResources(url, config.getOutputDir(), filter);
    }

    private void generateTables(ProgressListener progressListener, Database db, Collection<Table> tables, WriteStats stats) throws IOException {
        HtmlTablePage tableFormatter = HtmlTablePage.getInstance();
        for (Table table : tables) {
            progressListener.graphingDetailsProgressed(table);
            /*
            if (fineEnabled)
                logger.fine("Writing details of " + table.getName());
            */
            tableFormatter.write(db, table, config.getOutputDir(), stats);
        }
    }
}
