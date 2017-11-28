package org.schemaspy.output.dot.std;

import org.schemaspy.output.dot.DotConfig;

import java.nio.charset.Charset;

public class DotNodeConfig {

    private DotConfig dotConfig;

    private boolean showColumns = false;
    private boolean showTrivialColumns = false;
    private boolean showColumnDetails = false;

    public static DotNodeConfig dotNodeConfig(DotConfig dotConfig) {
        return new DotNodeConfig(dotConfig);
    }

    public DotNodeConfig(DotConfig dotConfig) {
        this.dotConfig = dotConfig;
    }

    public Charset charset() {
        return dotConfig.charset();
    }

    public String font() {
        return dotConfig.font();
    }

    public int fontSize() {
        return dotConfig.fontSize();
    }

    public boolean showNumRows() {
        return dotConfig.showNumRows();
    }

    public boolean oneOfMultipleSchemas() {
        return dotConfig.oneOfMultipleSchemas();
    }

    public String tableHeadBackgroundColor() {
        return dotConfig.tableHeadBackgroundColor();
    }

    public String tableBackgroundColor() {
        return dotConfig.tableBackgroundColor();
    }

    public String bodyBackgroundColor() {
        return dotConfig.bodyBackgroundColor();
    }

    public String excludedColumnBackgroundColor() {
        return dotConfig.excludedColumnBackgroundColor();
    }

    public String indexedColumnBackgroundColor() {
        return dotConfig.indexedColumnBackgroundColor();
    }

    /**
     * Getter for property 'showColumns'.
     *
     * @return Value for property 'showColumns'.
     */
    public boolean showColumns() {
        return showColumns;
    }

    /**
     * Setter for property 'showColumns'.
     *
     * @param showColumns Value to set for property 'showColumns'.
     */
    public DotNodeConfig showColumns(boolean showColumns) {
        this.showColumns = showColumns;
        return this;
    }

    /**
     * Getter for property 'showTrivialColumns'.
     *
     * @return Value for property 'showTrivialColumns'.
     */
    public boolean showTrivialColumns() {
        return showTrivialColumns;
    }

    /**
     * Setter for property 'showTrivialColumns'.
     *
     * @param showTrivialColumns Value to set for property 'showTrivialColumns'.
     */
    public DotNodeConfig showTrivialColumns(boolean showTrivialColumns) {
        this.showTrivialColumns = showTrivialColumns;
        return this;
    }

    /**
     * Getter for property 'showColumnDetails'.
     *
     * @return Value for property 'showColumnDetails'.
     */
    public boolean showColumnDetails() {
        return showColumnDetails;
    }

    /**
     * Setter for property 'showColumnDetails'.
     *
     * @param showColumnDetails Value to set for property 'showColumnDetails'.
     */
    public DotNodeConfig showColumnDetails(boolean showColumnDetails) {
        this.showColumnDetails = showColumnDetails;
        return this;
    }

}
