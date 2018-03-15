package org.schemaspy.input.dbms.config;

import org.schemaspy.input.dbms.exceptions.MissingArgumentException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionUrlBuilder {

    private static final Pattern PATTER = Pattern.compile("(<([a-zA-Z0-0]*)>)");

    private String connectionSpec;
    private String hostPortSeparator;
    private Map<String,String> arguments = new HashMap<>();

    public ConnectionUrlBuilder usingDatabaseProperties(Properties dbProperties) {
        this.connectionSpec = dbProperties.getProperty("connectionSpec");
        this.hostPortSeparator = dbProperties.getProperty("hostPortSeparator", ":");
        return this;
    }

    public ConnectionUrlBuilder usingArguments(Map<String, String> arguments) {
        this.arguments.putAll(arguments);
        return this;
    }

    public ConnectionUrlBuilder withArgument(String name, String value) {
        if (Objects.nonNull(value) && !value.trim().isEmpty()) {
            this.arguments.put(name, value);
        }
        return this;
    }

    public String build() {
        addHostOptionalPort();
        String connectionUrl = connectionSpec;
        Matcher matcher = PATTER.matcher(connectionSpec);
        while(matcher.find()) {
            String argumentValue = arguments.get(matcher.group(2));
            if (Objects.isNull(argumentValue)) {
                throw new MissingArgumentException("ConnectionSpec contains " + matcher.group(1) + " but was not supplied");
            }
            connectionUrl = connectionUrl.replace(matcher.group(1), argumentValue);
        }
        return connectionUrl;
    }

    private void addHostOptionalPort() {
        String hostOptionalPort = arguments.get("host");
        if (Objects.isNull(hostOptionalPort)) {
            return;
        }
        String port = arguments.get("port");
        if (!hostOptionalPort.contains(hostPortSeparator) && Objects.nonNull(port)) {
            hostOptionalPort += hostPortSeparator + port;
        }
        arguments.putIfAbsent("hostOptionalPort", hostOptionalPort);
    }
}
