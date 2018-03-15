package org.schemaspy;

import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.InputSource;
import org.schemaspy.input.dbms.DbmsInputSource;
import org.schemaspy.model.ConsoleProgressListener;
import org.schemaspy.model.ProgressListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WiringConfiguration {

    @Bean
    public ProgressListener progressListener(CommandLineArguments commandLineArguments) {
        return new ConsoleProgressListener(!commandLineArguments.isSkipHtml());
    }

    @Bean
    public InputSource inputSource(CommandLineArguments commandLineArguments, ProgressListener progressListener) {
        return new DbmsInputSource(commandLineArguments.getDbmsCommandLineArguments(), progressListener);
    }

    @Bean
    public SchemaAnalyzer schemaAnalyzer(InputSource inputSource, CommandLineArguments commandLineArguments, ProgressListener progressListener) {
        return new SchemaAnalyzer(inputSource, commandLineArguments, progressListener);
    }

}
