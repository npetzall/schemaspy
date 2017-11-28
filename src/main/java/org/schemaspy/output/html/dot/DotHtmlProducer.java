package org.schemaspy.output.html.dot;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.schemaspy.analyzer.DbAnalyzer;
import org.schemaspy.model.*;
import org.schemaspy.output.dot.std.DotFormatter;
import org.schemaspy.output.html.HtmlConfig;
import org.schemaspy.output.html.HtmlProducer;
import org.schemaspy.util.LineWriter;
import org.schemaspy.util.WriteStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.*;

//TODO Separate module

public class DotHtmlProducer implements HtmlProducer {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final HtmlRelationshipsPage htmlRelationshipsPage = new HtmlRelationshipsPage();
    private final HtmlOrphansPage htmlOrphansPage;
    private final HtmlMainIndexPage htmlMainIndexPage;
    private final HtmlConstraintsPage htmlConstraintsPage;
    private final HtmlAnomaliesPage htmlAnomaliesPage = new HtmlAnomaliesPage();
    private final HtmlColumnsPage htmlColumnsPage;
    private final HtmlRoutinesPage htmlRoutinesPage = new HtmlRoutinesPage();
    private final HtmlComponentPage htmlComponentPage = new HtmlComponentPage();
    private final HtmlTablePage htmlTablePage;

    private final HtmlConfig config;
    private final DotFormatter dotFormatter;
    private final ProgressListener progressListener;

    public DotHtmlProducer(HtmlConfig config, DotFormatter dotFormatter, ProgressListener progressListener) {
        this.config = Objects.requireNonNull(config);
        this.dotFormatter = Objects.requireNonNull(dotFormatter);
        this.progressListener = Objects.requireNonNull(progressListener);


        htmlOrphansPage = new HtmlOrphansPage(config, dotFormatter);
        htmlMainIndexPage = new HtmlMainIndexPage(config);
        htmlConstraintsPage = new HtmlConstraintsPage(config);
        htmlColumnsPage = new HtmlColumnsPage(config);
        htmlTablePage = new HtmlTablePage(config, dotFormatter);
    }

    @Override
    public void generate(Database database) {
        Collection<Table> tables = new ArrayList<Table>(database.getTables());
        tables.addAll(database.getViews());
        new File(config.outputDir(), "tables").mkdirs();
        new File(config.outputDir(), "diagrams/summary").mkdirs();
        try {
            generateHtmlDoc(progressListener, database, tables);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        if (config.isHtmlGenerationEnabled()) {
            LOG.info("Wrote table details in " + duration / 1000 + " seconds");

            if (LOG.isInfoEnabled()) {
                LOG.info("Wrote relationship details of " + tables.size() + " tables/views to directory '" + config.outputDir() + "' in " + overallDuration / 1000 + " seconds.");
                LOG.info("View the results by opening " + new File(config.outputDir(), "index.html"));
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

        boolean showDetailedTables = tables.size() <= config.detailedTablesLimit();
        final boolean includeImpliedConstraints = config.includeImpliedConstraints();

        // if evaluating a 'ruby on rails-based' database then connect the columns
        // based on RoR conventions
        // note that this is done before 'hasRealRelationships' gets evaluated so
        // we get a relationships ER diagram
        //TODO Totally BAD
        if (config.includeRailsConstraints())
            DbAnalyzer.getRailsConstraints(db.getTablesByName());

        File summaryDir = new File(config.outputDir(), "diagrams/summary");

        // generate the compact form of the relationships .dot file
        String dotBaseFilespec = "relationships";
        out = new LineWriter(new File(summaryDir, dotBaseFilespec + ".real.compact.dot"), config.charset());
        WriteStats stats = new WriteStats(tables);
        dotFormatter.writeRealRelationships(db, tables, true, showDetailedTables, stats, out, config.outputDir());
        boolean hasRealRelationships = stats.getNumTablesWritten() > 0 || stats.getNumViewsWritten() > 0;
        out.close();

        if (hasRealRelationships) {
            // real relationships exist so generate the 'big' form of the relationships .dot file
            progressListener.graphingSummaryProgressed();
            out = new LineWriter(new File(summaryDir, dotBaseFilespec + ".real.large.dot"), config.charset());
            dotFormatter.writeRealRelationships(db, tables, false, showDetailedTables, stats, out, config.outputDir());
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
        out = new LineWriter(impliedDotFile, config.charset());
        boolean hasImplied = dotFormatter.writeAllRelationships(db, tables, true, showDetailedTables, stats, out, config.outputDir());

        Set<TableColumn> excludedColumns = stats.getExcludedColumns();
        out.close();
        if (hasImplied) {
            impliedDotFile = new File(summaryDir, dotBaseFilespec + ".implied.large.dot");
            out = new LineWriter(impliedDotFile, config.charset());
            dotFormatter.writeAllRelationships(db, tables, false, showDetailedTables, stats, out, config.outputDir());
            out.close();
        } else {
            impliedDotFile.delete();
        }

        htmlRelationshipsPage.write(db, summaryDir, dotBaseFilespec, hasRealRelationships, hasImplied, excludedColumns,
                progressListener, config.outputDir());

        progressListener.graphingSummaryProgressed();

        File orphansDir = new File(config.outputDir(), "diagrams/orphans");
        orphansDir.mkdirs();
        htmlOrphansPage.write(db, orphans, orphansDir, config.outputDir());
        out.close();

        progressListener.graphingSummaryProgressed();

        htmlMainIndexPage.write(db, tables, db.getRemoteTables(), config.outputDir());

        progressListener.graphingSummaryProgressed();

        List<ForeignKeyConstraint> constraints = DbAnalyzer.getForeignKeyConstraints(tables);
        htmlConstraintsPage.write(db, constraints, tables, config.outputDir());

        progressListener.graphingSummaryProgressed();

        htmlAnomaliesPage.write(db, tables, impliedConstraints, config.outputDir());

        progressListener.graphingSummaryProgressed();

        for (HtmlColumnsPage.ColumnInfo columnInfo : htmlColumnsPage.getColumnInfos().values()) {
            htmlColumnsPage.write(db, tables, columnInfo, config.outputDir());
        }

        progressListener.graphingSummaryProgressed();

        out = new LineWriter(new File(config.outputDir(), "routines.html"), 16 * 1024, config.charset());
        htmlRoutinesPage.write(db, out);
        out.close();

        // create detailed diagrams

        //duration = progressListener.startedGraphingDetails();

        //logger.info("Completed summary in " + duration / 1000 + " seconds");
        //logger.info("Writing/diagramming details");

        generateTables(progressListener, db, tables, stats);
        htmlComponentPage.write(db, tables, config.outputDir());
    }

    private void prepareLayoutFiles() throws IOException {
        URL url = getClass().getResource("/layout");
        File directory = new File(url.getPath());

        IOFileFilter notHtmlFilter = FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(".html"));
        FileFilter filter = FileFilterUtils.and(notHtmlFilter);
        //cleanDirectory(config.outputDir(),"/diagrams");
        //cleanDirectory(config.outputDir(),"/tables");
        ResourceWriter.copyResources(url, config.outputDir(), filter);
    }

    private void generateTables(ProgressListener progressListener, Database db, Collection<Table> tables, WriteStats stats) throws IOException {
        for (Table table : tables) {
            progressListener.graphingDetailsProgressed(table);
            /*
            if (fineEnabled)
                logger.fine("Writing details of " + table.getName());
            */
            htmlTablePage.write(db, table, config.outputDir(), stats);
        }
    }
}
