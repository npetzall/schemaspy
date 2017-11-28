/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011 John Currier
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
package org.schemaspy.output.dot.std;

import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.model.TableIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DotNode {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Table table;
    private final String path;
    private final String outputDir;
    private final DotNodeConfig config;
    private final Set<TableColumn> excludedColumns = new HashSet<>();
    private final String lineSeparator = System.getProperty("line.separator");
    private boolean showImpliedRelationships = false;

    /**
     * Create a DotNode and specify whether it displays its columns.
     * The details of the optional columns (e.g. type, size) are not displayed.
     *
     * @param table       Table
     * @param path        String
     * @param outputDir File
     * @param config DotNodeConfig
     */
    public DotNode(Table table, String path, File outputDir, DotNodeConfig config) {
        this.table = table;
        this.path = path + (table.isRemote() ? ("../../" + table.getContainer() + "/tables/") : "");
        this.outputDir = outputDir.toString();
        this.config = config;
    }

    public void setShowImplied(boolean showImplied) {
        showImpliedRelationships = showImplied;
    }

    public Table getTable() {
        return table;
    }

    public void excludeColumn(TableColumn column) {
        excludedColumns.add(column);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        String tableName = table.getName();
        // fully qualified table name (optionally prefixed with schema)
        String fqTableName = (table.isRemote() ? table.getContainer() + "." : "") + tableName;
        String colspan = this.config.showColumnDetails() ? "COLSPAN=\"2\" " : "COLSPAN=\"3\" ";
        String colspanHeader = this.config.showColumnDetails() ? "COLSPAN=\"4\" " : "COLSPAN=\"3\" ";
        String tableOrView = table.isView() ? "view" : "table";

        buf.append("  \"" + fqTableName + "\" [" + lineSeparator);
        buf.append("   label=<" + lineSeparator);
        buf.append("    <TABLE BORDER=\"" + (this.config.showColumnDetails() ? "2" : "0") + "\" CELLBORDER=\"1\" CELLSPACING=\"0\" BGCOLOR=\"" + config.tableBackgroundColor() + "\">" + lineSeparator);
        buf.append("      <TR>");
        buf.append("<TD " + colspanHeader + " BGCOLOR=\"" + config.tableHeadBackgroundColor() + "\">");
        buf.append("<TABLE BORDER=\"0\" CELLSPACING=\"0\">");
        buf.append("<TR>");
        buf.append("<TD ALIGN=\"LEFT\"><B>" + fqTableName + "</B></TD>");
        buf.append("<TD ALIGN=\"RIGHT\">[" + tableOrView + "]</TD>");
        buf.append("</TR>");
        buf.append("</TABLE>");
        buf.append("</TD>");
        buf.append("</TR>" + lineSeparator);


        boolean skippedTrivial = false;

        if (this.config.showColumns()) {
            List<TableColumn> primaryColumns = table.getPrimaryColumns();
            Set<TableColumn> indexColumns = new HashSet<>();

            for (TableIndex index : table.getIndexes()) {
                indexColumns.addAll(index.getColumns());
            }
            indexColumns.removeAll(primaryColumns);

            int maxwidth = getColumnMaxWidth();

            for (TableColumn column : table.getColumns()) {
                if (this.config.showTrivialColumns() || this.config.showColumnDetails() || column.isPrimary() || column.isForeignKey() || indexColumns.contains(column)) {
                    buf.append("      <TR>");
                    buf.append("<TD PORT=\"" + column.getName() + "\" " + colspan);
                    if (excludedColumns.contains(column))
                        buf.append("BGCOLOR=\"" + config.excludedColumnBackgroundColor() + "\" ");
                    else if (indexColumns.contains(column))
                        buf.append("BGCOLOR=\"" + config.indexedColumnBackgroundColor() + "\" ");
                    buf.append("ALIGN=\"LEFT\">");
                    buf.append("<TABLE BORDER=\"0\" CELLSPACING=\"0\" ALIGN=\"LEFT\">");
                    buf.append("<TR ALIGN=\"LEFT\">");
                    buf.append("<TD ALIGN=\"LEFT\" FIXEDSIZE=\"TRUE\" WIDTH=\"15\" HEIGHT=\"16\">");
                    if (column.isPrimary()) {
                        buf.append("<IMG SRC=\"" + outputDir + "/images/primaryKeys.png\"/>");
                    } else if (column.isForeignKey()) {
                        buf.append("<IMG SRC=\"" + outputDir + "/images/foreignKeys.png\"/>");
                    }
                    buf.append("</TD>");
                    buf.append("<TD ALIGN=\"LEFT\" FIXEDSIZE=\"TRUE\" WIDTH=\"" + maxwidth + "\" HEIGHT=\"16\">");
                    buf.append(column.getName());
                    buf.append("</TD>");
                    buf.append("</TR>");
                    buf.append("</TABLE>");
                    buf.append("</TD>");

                    if (this.config.showColumnDetails()) {
                        buf.append("<TD PORT=\"");
                        buf.append(column.getName());
                        buf.append(".type\" ALIGN=\"LEFT\">");
                        buf.append(column.getShortTypeName().toLowerCase());
                        buf.append("[");
                        buf.append(column.getDetailedSize());
                        buf.append("]</TD>");
                    }
                    buf.append("</TR>" + lineSeparator);
                } else {
                    skippedTrivial = true;
                }
            }
        }

        if (skippedTrivial || !this.config.showColumns()) {
            buf.append("      <TR><TD PORT=\"elipses\" COLSPAN=\"3\" ALIGN=\"LEFT\">...</TD></TR>" + lineSeparator);
        }

        if (!table.isView()) {
            buf.append("      <TR>");
            buf.append("<TD ALIGN=\"LEFT\" BGCOLOR=\"" + config.bodyBackgroundColor() + "\">");
            int numParents = showImpliedRelationships ? table.getNumParents() : table.getNumNonImpliedParents();
            if (numParents > 0 || this.config.showColumnDetails())
                buf.append("&lt; " + numParents);
            else
                buf.append("  ");

            buf.append("</TD>");
            buf.append("<TD ALIGN=\"RIGHT\" BGCOLOR=\"" + config.bodyBackgroundColor() + "\">");
            final long numRows = table.getNumRows();
            if (this.config.showNumRows() && numRows >= 0) {
                buf.append(NumberFormat.getInstance().format(numRows));
                buf.append(" row");
                if (numRows != 1)
                    buf.append('s');
            } else {
                buf.append("  ");
            }
            buf.append("</TD>");

            buf.append("<TD ALIGN=\"RIGHT\" BGCOLOR=\"" + config.bodyBackgroundColor() + "\">");
            int numChildren = showImpliedRelationships ? table.getNumChildren() : table.getNumNonImpliedChildren();
            if (numChildren > 0 || this.config.showColumnDetails())
                buf.append(numChildren + " &gt;");
            else
                buf.append("  ");
            buf.append("</TD></TR>" + lineSeparator);
        }

        buf.append("    </TABLE>>" + lineSeparator);
        if (!table.isRemote() || this.config.oneOfMultipleSchemas())
            buf.append("    URL=\"" + path + urlEncodeLink(tableName) + ".html\"" + lineSeparator);
        buf.append("    tooltip=\"" + escapeHtml(fqTableName) + "\"" + lineSeparator);
        buf.append("  ];");

        return buf.toString();
    }

    private int getColumnMaxWidth() {
        int maxWidth = getTextWidth(table.getName());
        for (TableColumn column : table.getColumns()) {
            int size = getTextWidth(column.getName());
            if (maxWidth < size) {
                maxWidth = size;
            }
        }
        return maxWidth;
    }

    private int getTextWidth(String text) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        int fontSize = config.fontSize() + 1;
        Font font = new Font(config.font(), Font.BOLD, fontSize);
        int fontWidth = (int) (font.getStringBounds(text, frc).getWidth());
        return fontWidth;
    }

    // https://stackoverflow.com/a/32155260
    private String escapeHtml(String string) {
        StringBuilder escapedTxt = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char tmp = string.charAt(i);
            switch (tmp) {
                case '<':
                    escapedTxt.append("&lt;");
                    break;
                case '>':
                    escapedTxt.append("&gt;");
                    break;
                case '&':
                    escapedTxt.append("&amp;");
                    break;
                case '"':
                    escapedTxt.append("&quot;");
                    break;
                case '\'':
                    escapedTxt.append("&#x27;");
                    break;
                case '/':
                    escapedTxt.append("&#x2F;");
                    break;
                default:
                    escapedTxt.append(tmp);
            }
        }
        return escapedTxt.toString();
    }

    private String urlEncodeLink(String string) {
        try {
            return URLEncoder.encode(string, config.charset().name()).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            LOG.info("Error trying to urlEncode string [" + string + "] with encoding [" + config.charset().name() + "]");
            return string;
        }
    }
}