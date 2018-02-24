package org.schemaspy;

import org.schemaspy.app.Context;
import org.schemaspy.app.SingletonContext;
import org.schemaspy.model.ConnectionFailure;
import org.schemaspy.model.EmptySchemaException;
import org.schemaspy.model.InvalidConfigurationException;
import org.schemaspy.model.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;

public class SchemaSpy {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        try (Context context = SingletonContext.getInstance().addContext(new Context("schemaspy",args))) {
            analyze(context.getSchemaAnalyzer(), args);
        }
    }

    private static void analyze(SchemaAnalyzer schemaAnalyzer, String...args) {
        int rc = 1;

        try {
            rc = schemaAnalyzer.analyze(new Config(args)) == null ? 1 : 0;
        } catch (ConnectionFailure couldntConnect) {
            LOGGER.warn("Connection Failure", couldntConnect);
            rc = 3;
        } catch (EmptySchemaException noData) {
            LOGGER.warn("Empty schema", noData);
            rc = 2;
        } catch (InvalidConfigurationException badConfig) {
            LOGGER.info("");
            if (badConfig.getParamName() != null)
                LOGGER.warn("Bad parameter specified for {}", badConfig.getParamName());
            LOGGER.warn("Bad config {}", badConfig.getMessage());
            if (badConfig.getCause() != null && !badConfig.getMessage().endsWith(badConfig.getMessage()))
                LOGGER.warn(" caused by {}", badConfig.getCause().getMessage());

            LOGGER.debug("Command line parameters: {}", Arrays.asList(args));
            LOGGER.debug("Invalid configuration detected", badConfig);
        } catch (ProcessExecutionException badLaunch) {
            LOGGER.warn(badLaunch.getMessage(), badLaunch);
        } catch (Exception exc) {
            LOGGER.error(exc.getMessage(), exc);
        }
    }

}
