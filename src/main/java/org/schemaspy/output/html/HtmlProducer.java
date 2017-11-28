package org.schemaspy.output.html;

import org.schemaspy.model.Database;

public interface HtmlProducer {
    void generate(Database database);
}
