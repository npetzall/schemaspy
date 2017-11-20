package org.schemaspy.app.config;

import org.schemaspy.Config;
import org.schemaspy.SchemaAnalyzer;
import org.schemaspy.app.cli.CommandLineArguments;
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
    public XmlProducer xmlProducer() {
        return new DOMXmlProducer();
    }

    @Bean
    public SchemaAnalyzer schemaAnalyzer(SqlService sqlService, DatabaseService databaseService, CommandLineArguments commandLineArguments, Config config, XmlProducer xmlProducer) {
        return new SchemaAnalyzer(sqlService, databaseService, commandLineArguments, config, xmlProducer);
    }
}
