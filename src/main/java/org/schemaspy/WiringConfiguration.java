package org.schemaspy;

import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.jdbc.DatabaseService;
import org.schemaspy.input.dbms.jdbc.SqlService;
import org.schemaspy.input.dbms.jdbc.TableService;
import org.schemaspy.input.dbms.jdbc.ViewService;
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
    public DatabaseService databaseService(TableService tableService, ViewService viewService, SqlService sqlService) {
        return new DatabaseService(tableService, viewService, sqlService);
    }

    @Bean
    public SchemaAnalyzer schemaAnalyzer(SqlService sqlService, DatabaseService databaseService, CommandLineArguments commandLineArguments) {
        return new SchemaAnalyzer(sqlService, databaseService, commandLineArguments);
    }

}
