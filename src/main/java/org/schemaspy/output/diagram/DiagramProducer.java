package org.schemaspy.output.diagram;

import java.io.File;

public interface DiagramProducer {
    String generate(File input, File output);
}
