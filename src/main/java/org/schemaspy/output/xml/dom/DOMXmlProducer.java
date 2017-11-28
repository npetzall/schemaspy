package org.schemaspy.output.xml.dom;

import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.output.xml.XmlProducer;
import org.schemaspy.util.LineWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class DOMXmlProducer implements XmlProducer {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final DOMXmlTableFormatter tableFormatter = new DOMXmlTableFormatter();

    @Override
    public void generate(Database database, File outputDir) {
        Collection<Table> tables = new ArrayList<>(database.getTables());
        tables.addAll(database.getViews());
        if (tables.isEmpty()) {
            LOG.warn("No tables to output");
            return;
        }
        LineWriter out = null;
        String xmlName = "";
        try {
            xmlName = database.getName();

            //TODO Couldnt this be done better?
            // some dbNames have path info in the name...strip it
            xmlName = new File(xmlName).getName();

            // some dbNames include jdbc driver details including :'s and @'s
            String[] unusables = xmlName.split("[:@]");
            xmlName = unusables[unusables.length - 1];

            //TODO schema could have illegal name
            if (database.getSchema() != null)
                xmlName += '.' + database.getSchema().getName();

            out = new LineWriter(new File(outputDir, xmlName + ".xml"), StandardCharsets.UTF_8);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();

            Document document = builder.newDocument();
            Element rootNode = document.createElement("database");
            document.appendChild(rootNode);
            DOMUtil.appendAttribute(rootNode, "name", database.getName());
            if (database.getSchema() != null)
                DOMUtil.appendAttribute(rootNode, "schema", database.getSchema().getName());
            DOMUtil.appendAttribute(rootNode, "type", database.getDatabaseProduct());

            tableFormatter.appendTables(rootNode, tables);

            document.getDocumentElement().normalize();
            DOMUtil.printDOM(document, out);

        } catch (TransformerException exc) {
            LOG.warn("Failed to write xml", exc);
        } catch (ParserConfigurationException exc) {
            LOG.warn("Failed to create document builder", exc);
        } catch (FileNotFoundException e) {
            LOG.warn("Could not create target file: {} in {}", xmlName, outputDir.getPath(), e);
        } finally {
            if (Objects.nonNull(out)) {
                try {
                    out.close();
                } catch (IOException exc) {
                    LOG.trace("Failed to close out", exc);
                }
            }
        }
    }
}
