package org.schemaspy.output.text.insertdeleteorder;

import java.io.File;
import java.nio.charset.Charset;

public class InsertDeleteOrderConfig {

    private File outputDir;
    private Charset charset;

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
    public InsertDeleteOrderConfig outputDir(File outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    /**
     * Getter for property 'charset'.
     *
     * @return Value for property 'charset'.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Setter for property 'charset'.
     *
     * @param charset Value to set for property 'charset'.
     */
    public InsertDeleteOrderConfig charset(Charset charset) {
        this.charset = charset;
        return this;
    }
}
