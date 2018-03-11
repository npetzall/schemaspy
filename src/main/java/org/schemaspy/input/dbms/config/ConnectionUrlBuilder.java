package org.schemaspy.input.dbms.config;

import java.util.Map;

public class ConnectionUrlBuilder {

    private String connectionSpec;
    private Map<String,String> arguments;

    public ConnectionUrlBuilder withConnectionSpec(String connectionSpec) {
        this.connectionSpec = connectionSpec;
        return this;
    }

    public ConnectionUrlBuilder usingArguments(Map<String, String> arguments) {
        this.arguments = arguments;
        return this;
    }

    public String build() {

    }
}
