package org.schemaspy.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

public class ApplicationContextInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static String getVersion() {
        String version = ApplicationContextInfo.class.getPackage().getImplementationVersion();
        if (Objects.isNull(version)) {
            version = "";
        }
        return version;
    }

    public static String getContext() {
        return new StringBuilder()
                .append("running ")
                .append(getRunningFrom())
                .append(" from ")
                .append(getWorkdir())
                .toString();
    }

    private static String getRunningFrom() {
        return ApplicationContextInfo.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    private static String getWorkdir() {
        return System.getProperty("user.dir");
    }
}
