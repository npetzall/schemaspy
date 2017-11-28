package org.schemaspy.output.dot;

import java.nio.charset.Charset;

public class DotConfig {

    private boolean bottomJustify = true;
    private Charset charset;
    private String font;
    private int fontSize;
    private boolean rankDirBug;
    private boolean showNumRows;
    private boolean oneOfMultipleSchemas;

    private String tableHeadBackgroundColor;
    private String tableBackgroundColor;
    private String bodyBackgroundColor;
    private String excludedColumnBackgroundColor;
    private String indexedColumnBackgroundColor;

    /**
     * Getter for property 'bottomJustify'.
     *
     * @return Value for property 'bottomJustify'.
     */
    public boolean bottomJustify() {
        return bottomJustify;
    }

    /**
     * Setter for property 'bottomJustify'.
     *
     * @param bottomJustify Value to set for property 'bottomJustify'.
     */
    public DotConfig bottomJustify(boolean bottomJustify) {
        this.bottomJustify = bottomJustify;
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
    public DotConfig charset(Charset charset) {
        this.charset = charset;
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
    public DotConfig font(String font) {
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
    public DotConfig fontSize(int fontSize) {
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
    public DotConfig rankDirBug(boolean rankDirBug) {
        this.rankDirBug = rankDirBug;
        return this;
    }

    /**
     * Getter for property 'showNumRows'.
     *
     * @return Value for property 'showNumRows'.
     */
    public boolean showNumRows() {
        return showNumRows;
    }

    /**
     * Setter for property 'showNumRows'.
     *
     * @param showNumRows Value to set for property 'showNumRows'.
     */
    public DotConfig showNumRows(boolean showNumRows) {
        this.showNumRows = showNumRows;
        return this;
    }

    /**
     * Getter for property 'oneOfMultipleSchemas'.
     *
     * @return Value for property 'oneOfMultipleSchemas'.
     */
    public boolean oneOfMultipleSchemas() {
        return oneOfMultipleSchemas;
    }

    /**
     * Setter for property 'oneOfMultipleSchemas'.
     *
     * @param oneOfMultipleSchemas Value to set for property 'oneOfMultipleSchemas'.
     */
    public DotConfig oneOfMultipleSchemas(boolean oneOfMultipleSchemas) {
        this.oneOfMultipleSchemas = oneOfMultipleSchemas;
        return this;
    }

    /**
     * Getter for property 'tableHeadBackgroundColor'.
     *
     * @return Value for property 'tableHeadBackgroundColor'.
     */
    public String tableHeadBackgroundColor() {
        return tableHeadBackgroundColor;
    }

    /**
     * Setter for property 'tableHeadBackgroundColor'.
     *
     * @param tableHeadBackgroundColor Value to set for property 'tableHeadBackgroundColor'.
     */
    public DotConfig tableHeadBackgroundColor(String tableHeadBackgroundColor) {
        this.tableHeadBackgroundColor = tableHeadBackgroundColor;
        return this;
    }

    /**
     * Getter for property 'tableBackgroundColor'.
     *
     * @return Value for property 'tableBackgroundColor'.
     */
    public String tableBackgroundColor() {
        return tableBackgroundColor;
    }

    /**
     * Setter for property 'tableBackgroundColor'.
     *
     * @param tableBackgroundColor Value to set for property 'tableBackgroundColor'.
     */
    public DotConfig tableBackgroundColor(String tableBackgroundColor) {
        this.tableBackgroundColor = tableBackgroundColor;
        return this;
    }

    /**
     * Getter for property 'bodyBackgroundColor'.
     *
     * @return Value for property 'bodyBackgroundColor'.
     */
    public String bodyBackgroundColor() {
        return bodyBackgroundColor;
    }

    /**
     * Setter for property 'bodyBackgroundColor'.
     *
     * @param bodyBackgroundColor Value to set for property 'bodyBackgroundColor'.
     */
    public DotConfig bodyBackgroundColor(String bodyBackgroundColor) {
        this.bodyBackgroundColor = bodyBackgroundColor;
        return this;
    }

    /**
     * Getter for property 'excludedColumnBackgroundColor'.
     *
     * @return Value for property 'excludedColumnBackgroundColor'.
     */
    public String excludedColumnBackgroundColor() {
        return excludedColumnBackgroundColor;
    }

    /**
     * Setter for property 'excludedColumnBackgroundColor'.
     *
     * @param excludedColumnBackgroundColor Value to set for property 'excludedColumnBackgroundColor'.
     */
    public DotConfig excludedColumnBackgroundColor(String excludedColumnBackgroundColor) {
        this.excludedColumnBackgroundColor = excludedColumnBackgroundColor;
        return this;
    }

    /**
     * Getter for property 'indexedColumnBackgroundColor'.
     *
     * @return Value for property 'indexedColumnBackgroundColor'.
     */
    public String indexedColumnBackgroundColor() {
        return indexedColumnBackgroundColor;
    }

    /**
     * Setter for property 'indexedColumnBackgroundColor'.
     *
     * @param indexedColumnBackgroundColor Value to set for property 'indexedColumnBackgroundColor'.
     */
    public DotConfig indexedColumnBackgroundColor(String indexedColumnBackgroundColor) {
        this.indexedColumnBackgroundColor = indexedColumnBackgroundColor;
        return this;
    }
}
