package org.schemaspy.cli;

import com.beust.jcommander.IDefaultProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link IDefaultProvider} that provides values reading from a {@link Properties} file.
 *
 * TODO
 * JCommander already provides a com.beust.jcommander.defaultprovider.PropertyFileDefaultProvider.
 * But it always reports "cannot find file on classpath" although it exists. Maybe open an issue at the JCommander project?
 */
public class PropertyFileDefaultProvider implements IDefaultProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Properties properties;

    private final List<String> booleans = Arrays.asList("schemaspy.sso","schemaspy.debug");

    public PropertyFileDefaultProvider(String propertiesFilename) {
        Objects.requireNonNull(propertiesFilename);
        properties = loadProperties(propertiesFilename);
    }

    private static Properties loadProperties(String path) {
        Properties properties = new Properties();
        try (Stream<String> lineStream = Files.lines(Paths.get(path))) {
            String content = lineStream
                    .map(l -> l.replace("\\", "\\\\"))
                    .collect(Collectors.joining(System.lineSeparator()));
            properties.load(new StringReader(content));
        } catch (IOException e) {
            LOGGER.info("Configuration file not found");
        }
        return properties;
    }

    @Override
    public String getDefaultValueFor(String optionName) {
        if (booleans.contains(optionName)) {
            String value = properties.getProperty(optionName, Boolean.FALSE.toString());
            return value.isEmpty() ? Boolean.TRUE.toString() : value;
        }
        return properties.getProperty(optionName);
    }
}
