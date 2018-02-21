package org.schemaspy;

import org.schemaspy.api.progress.ProgressListenerFactory;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.service.SqlService;
import org.schemaspy.service.TableService;
import org.schemaspy.service.ViewService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WiringConfiguration {

    @Bean
    public SqlService sqlService() {
        return new SqlService();
    }

    @Bean
    public TableService tableService(SqlService sqlService, CommandLineArguments commandLineArguments) {
        return new TableService(sqlService, commandLineArguments);
    }

    @Bean
    public ViewService viewService(SqlService sqlService) {
        return new ViewService(sqlService);
    }

    @Bean
    public DatabaseService databaseService(TableService tableService, ViewService viewService, SqlService sqlService, ProgressListenerFactory progressListenerFactory) {
        return new DatabaseService(tableService, viewService, sqlService, progressListenerFactory);
    }

    @Bean
    public SchemaAnalyzer schemaAnalyzer(SqlService sqlService, DatabaseService databaseService, CommandLineArguments commandLineArguments, ProgressListenerFactory progressListenerFactory) {
        return new SchemaAnalyzer(sqlService, databaseService, commandLineArguments, progressListenerFactory);
    }

}