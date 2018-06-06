/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2017 Rafal Kasa
 * Copyright (C) 2017 Daniel Watt
 *
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy.output.html.mustache.pages;

import org.schemaspy.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * @author John Currier
 * @author Rafal Kasa
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class HtmlFormatter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected final boolean encodeComments = Config.getInstance().isEncodeCommentsEnabled();
    private   final boolean isMetered = Config.getInstance().isMeterEnabled();
    protected final boolean displayNumRows = Config.getInstance().isNumRowsEnabled();

    protected HtmlFormatter() {
    }

    /**
     * Override if your output doesn't live in the root directory.
     * If non blank must end with a trailing slash.
     *
     * @return String
     */
    protected String getPathToRoot() {
        return "";
    }

    protected boolean sourceForgeLogoEnabled() {
        return Config.getInstance().isLogoEnabled();
    }

}
