package org.schemaspy.output.xml;

import org.schemaspy.model.Database;

import java.io.File;

public interface XmlProducer {
    void create(Database database, File outputDir);
}
