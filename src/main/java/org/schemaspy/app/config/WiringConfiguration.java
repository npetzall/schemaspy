package org.schemaspy.app.config;

import org.schemaspy.Config;
import org.schemaspy.app.SchemaAnalyzer;
import org.schemaspy.app.cli.CommandLineArguments;
import org.schemaspy.input.db.DatabaseService;
import org.schemaspy.input.db.SqlService;
import org.schemaspy.input.db.TableService;
import org.schemaspy.input.db.ViewService;
import org.schemaspy.model.ConsoleProgressListener;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.output.dot.std.DotFormatter;
import org.schemaspy.output.html.HtmlConfig;
import org.schemaspy.output.html.HtmlProducer;
import org.schemaspy.output.html.dot.DotHtmlProducer;
import org.schemaspy.output.html.dot.StyleSheet;
import org.schemaspy.output.text.insertdeleteorder.InsertDeleteOrderConfig;
import org.schemaspy.output.text.insertdeleteorder.InsertDeleteOrderProducer;
import org.schemaspy.output.xml.XmlProducer;
import org.schemaspy.output.xml.dom.DOMXmlProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Configuration
public class WiringConfiguration {

    @Bean
    public SqlService sqlService(CommandLineArguments commandLineArguments, Config config) {
        return new SqlService(commandLineArguments, config);
    }

    @Bean
    public ViewService viewService(SqlService sqlService, Config config) {
        return new ViewService(sqlService, config);
    }

    @Bean
    public TableService tableService(SqlService sqlService, CommandLineArguments commandLineArguments, Config config) {
        return new TableService(sqlService, commandLineArguments, config);
    }

    @Bean
    public DatabaseService databaseService(TableService tableService, ViewService viewService, SqlService sqlService, Config config) {
        return new DatabaseService(tableService, viewService, sqlService, config);
    }

    @Bean
    public ProgressListener progressListener(Config config, CommandLineArguments commandLineArguments) {
        boolean render = config.isHtmlGenerationEnabled();
        return new ConsoleProgressListener(render, commandLineArguments);
    }

    @Bean
    public HtmlConfig htmlConfig(CommandLineArguments commandLineArguments, Config config) {
        return new HtmlConfig()
                .outputDir(commandLineArguments.getOutputDirectory())
                .detailedTablesLimit(config.getMaxDetailedTables())
                .includeImpliedConstraints(config.isImpliedConstraintsEnabled())
                .includeRailsConstraints(config.isRailsEnabled())
                .columnDetails(config.getColumnDetails())
                .pagination(config.isPaginationEnabled())
                .imageFormat(config.getImageFormat());
    }

    @Bean
    public StyleSheet styleSheet(Config config) {
        return StyleSheet.getInstance();
    }

    @Bean
    public DotConfig dotConfig(Config config, StyleSheet styleSheet) {
        return new DotConfig()
                .charset(StandardCharsets.UTF_8)
                .font(config.getFont())
                .fontSize(config.getFontSize())
                .rankDirBug(config.isRankDirBugEnabled())
                .showNumRows(config.isNumRowsEnabled())
                .oneOfMultipleSchemas(config.isOneOfMultipleSchemas())
                .tableHeadBackgroundColor(styleSheet.getTableHeadBackground())
                .tableBackgroundColor(styleSheet.getTableBackground())
                .bodyBackgroundColor(styleSheet.getBodyBackground())
                .excludedColumnBackgroundColor(styleSheet.getExcludedColumnBackgroundColor())
                .indexedColumnBackgroundColor(styleSheet.getIndexedColumnBackground());
    }

    @Bean
    public DotFormatter dotFormatter(DotConfig dotConfig) {
        return new DotFormatter(dotConfig);
    }

    @Bean
    public HtmlProducer htmlProducer(HtmlConfig config, DotFormatter dotFormatter, ProgressListener progressListener) {
        return new DotHtmlProducer(config, dotFormatter, progressListener);
    }

    @Bean
    public XmlProducer xmlProducer() {
        return new DOMXmlProducer();
    }

    @Bean
    public InsertDeleteOrderConfig insertDeleteOrderConfig(CommandLineArguments commandLineArguments, Config config) {
        return new InsertDeleteOrderConfig()
                .outputDir(commandLineArguments.getOutputDirectory())
                .charset(Charset.forName(config.DOT_CHARSET));
    }

    @Bean
    public InsertDeleteOrderProducer insertDeleteOrderProducer(InsertDeleteOrderConfig insertDeleteOrderConfig) {
        return new InsertDeleteOrderProducer(insertDeleteOrderConfig);
    }

    @Bean
    public SchemaAnalyzer schemaAnalyzer(
            SqlService sqlService,
            DatabaseService databaseService,
            CommandLineArguments commandLineArguments,
            Config config,
            ProgressListener progressListener,
            HtmlProducer htmlProducer,
            XmlProducer xmlProducer,
            InsertDeleteOrderProducer insertDeleteOrderProducer) {
        return new SchemaAnalyzer(
                sqlService,
                databaseService,
                commandLineArguments,
                config,
                progressListener,
                htmlProducer,
                xmlProducer,
                insertDeleteOrderProducer);
    }
}
