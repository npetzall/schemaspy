package org.schemaspy.input;

import org.schemaspy.model.Database;

public interface InputSource {
    Database createDatabaseModel();
}
