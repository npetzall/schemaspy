package org.schemaspy.output.html;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

public class HtmlConfig {

    //DOT & HTML
    private File outputDir;

    //Analyze
    private boolean includeImpliedConstraints;
    private boolean includeRailsConstraints;

    //HTML
    private boolean pagination;
    private int detailedTablesLimit;
    private List<String> columnDetails;

    //DOT Config
    private Charset charset;
    private String imageFormat;
    private String font;
    private int fontSize;
    private boolean rankDirBug;

    /**
     * Getter for property 'outputDir'.
     *
     * @return Value for property 'outputDir'.
     */
    public File outputDir() {
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
     * Getter for property 'includeImpliedConstraints'.
     *
     * @return Value for property 'includeImpliedConstraints'.
     */
    public boolean includeImpliedConstraints() {
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
    public boolean includeRailsConstraints() {
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

    /**
     * Getter for property 'pagination'.
     *
     * @return Value for property 'pagination'.
     */
    public boolean pagination() {
        return pagination;
    }

    /**
     * Setter for property 'pagination'.
     *
     * @param pagination Value to set for property 'pagination'.
     */
    public HtmlConfig pagination(boolean pagination) {
        this.pagination = pagination;
        return this;
    }

    /**
     * Getter for property 'detailedTablesLimit'.
     *
     * @return Value for property 'detailedTablesLimit'.
     */
    public int detailedTablesLimit() {
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
     * Getter for property 'columnDetails'.
     *
     * @return Value for property 'columnDetails'.
     */
    public List<String> columnDetails() {
        return columnDetails;
    }

    /**
     * Setter for property 'columnDetails'.
     *
     * @param columnDetails Value to set for property 'columnDetails'.
     */
    public HtmlConfig columnDetails(List<String> columnDetails) {
        this.columnDetails = columnDetails;
        return this;
    }

    /**
     * Getter for property 'charset'.
     *
     * @return Value for property 'charset'.
     */
    public Charset charset() {
        return charset;
    }

    /**
     * Setter for property 'charset'.
     *
     * @param charset Value to set for property 'charset'.
     */
    public HtmlConfig charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Getter for property 'imageFormat'.
     *
     * @return Value for property 'imageFormat'.
     */
    public String imageFormat() {
        return imageFormat;
    }

    /**
     * Setter for property 'imageFormat'.
     *
     * @param imageFormat Value to set for property 'imageFormat'.
     */
    public HtmlConfig imageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
        return this;
    }

    /**
     * Getter for property 'font'.
     *
     * @return Value for property 'font'.
     */
    public String font() {
        return font;
    }

    /**
     * Setter for property 'font'.
     *
     * @param font Value to set for property 'font'.
     */
    public HtmlConfig font(String font) {
        this.font = font;
        return this;
    }

    /**
     * Getter for property 'fontSize'.
     *
     * @return Value for property 'fontSize'.
     */
    public int fontSize() {
        return fontSize;
    }

    /**
     * Setter for property 'fontSize'.
     *
     * @param fontSize Value to set for property 'fontSize'.
     */
    public HtmlConfig fontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    /**
     * Getter for property 'rankDirBug'.
     *
     * @return Value for property 'rankDirBug'.
     */
    public boolean rankDirBug() {
        return rankDirBug;
    }

    /**
     * Setter for property 'rankDirBug'.
     *
     * @param rankDirBug Value to set for property 'rankDirBug'.
     */
    public HtmlConfig rankDirBug(boolean rankDirBug) {
        this.rankDirBug = rankDirBug;
        return this;
    }
}
