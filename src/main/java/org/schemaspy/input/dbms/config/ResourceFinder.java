/*
 * Copyright (C) 2017 Nils Petzaell
 */
package org.schemaspy.input.dbms.config;

import java.net.URL;

/**
 * @author Nils Petzaell
 */
public interface ResourceFinder {
    URL find(String resource);
}
