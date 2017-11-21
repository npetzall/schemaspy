package org.schemaspy.output.html;

import java.io.File;

public class HtmlConfig {

    File outputDir;
    int detailedTablesLimit;
    boolean includeImpliedConstraints;
    boolean includeRailsConstraints;

    /**
     * Getter for property 'outputDir'.
     *
     * @return Value for property 'outputDir'.
     */
    public File getOutputDir() {
        return outputDir;
    }

    /**
     * Setter for property 'outputDir'.
     *
     * @param outputDir Value to set for property 'outputDir'.
     */
    public HtmlConfig outputDir(File outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    /**
     * Getter for property 'detailedTablesLimit'.
     *
     * @return Value for property 'detailedTablesLimit'.
     */
    public int getDetailedTablesLimit() {
        return detailedTablesLimit;
    }

    /**
     * Setter for property 'detailedTablesLimit'.
     *
     * @param detailedTablesLimit Value to set for property 'detailedTablesLimit'.
     */
    public HtmlConfig detailedTablesLimit(int detailedTablesLimit) {
        this.detailedTablesLimit = detailedTablesLimit;
        return this;
    }

    /**
     * Getter for property 'includeImpliedConstraints'.
     *
     * @return Value for property 'includeImpliedConstraints'.
     */
    public boolean isIncludeImpliedConstraints() {
        return includeImpliedConstraints;
    }

    /**
     * Setter for property 'includeImpliedConstraints'.
     *
     * @param includeImpliedConstraints Value to set for property 'includeImpliedConstraints'.
     */
    public HtmlConfig includeImpliedConstraints(boolean includeImpliedConstraints) {
        this.includeImpliedConstraints = includeImpliedConstraints;
        return this;
    }

    /**
     * Getter for property 'includeRailsConstraints'.
     *
     * @return Value for property 'includeRailsConstraints'.
     */
    public boolean isIncludeRailsConstraints() {
        return includeRailsConstraints;
    }

    /**
     * Setter for property 'includeRailsConstraints'.
     *
     * @param includeRailsConstraints Value to set for property 'includeRailsConstraints'.
     */
    public HtmlConfig includeRailsConstraints(boolean includeRailsConstraints) {
        this.includeRailsConstraints = includeRailsConstraints;
        return this;
    }
}
