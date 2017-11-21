package org.schemaspy.app.config;

import org.schemaspy.Config;
import org.schemaspy.SchemaAnalyzer;
import org.schemaspy.app.cli.CommandLineArguments;
import org.schemaspy.model.ConsoleProgressListener;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.output.html.DotHtmlProducer;
import org.schemaspy.output.html.HtmlConfig;
import org.schemaspy.output.html.HtmlProducer;
import org.schemaspy.output.xml.DOMXmlProducer;
import org.schemaspy.output.xml.XmlProducer;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.service.SqlService;
import org.schemaspy.service.TableService;
import org.schemaspy.service.ViewService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                .includeRailsConstraints(config.isRailsEnabled());
    }

    @Bean
    public HtmlProducer htmlProducer(HtmlConfig config, ProgressListener progressListener) {
        return new DotHtmlProducer(config, progressListener);
    }

    @Bean
    public XmlProducer xmlProducer() {
        return new DOMXmlProducer();
    }

    @Bean
    public SchemaAnalyzer schemaAnalyzer(
            SqlService sqlService,
            DatabaseService databaseService,
            CommandLineArguments commandLineArguments,
            Config config,
            ProgressListener progressListener,
            HtmlProducer htmlProducer,
            XmlProducer xmlProducer) {
        return new SchemaAnalyzer(
                sqlService,
                databaseService,
                commandLineArguments,
                config,
                progressListener,
                htmlProducer,
                xmlProducer);
    }
}
