package org.schemaspy.output.text.insertdeleteorder;

import org.schemaspy.analyzer.TableOrderer;
import org.schemaspy.model.Database;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.util.LineWriter;
import org.schemaspy.util.RuntimeIOException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class InsertDeleteOrderProducer {

    private final InsertDeleteOrderConfig config;

    public InsertDeleteOrderProducer(InsertDeleteOrderConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    public void generate(Database database) {
        List<ForeignKeyConstraint> recursiveConstraints = new ArrayList<ForeignKeyConstraint>();

        // create an orderer to be able to determine insertion and deletion ordering of tables
        TableOrderer orderer = new TableOrderer();

        //TODO Side effects
        // side effect is that the RI relationships get trashed
        // also populates the recursiveConstraints collection
        List<Table> orderedTables = orderer.getTablesOrderedByRI(database.getTables(), recursiveConstraints);

        try {
            writeOrders(orderedTables);
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    private void writeOrders(List<Table> orderedTables) throws IOException {
        LineWriter out;
        out = new LineWriter(new File(config.getOutputDir(), "insertionOrder.txt"), 16 * 1024, config.getCharset());
        try {
            TextFormatter.getInstance().write(orderedTables, false, out);
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            out.close();
        }

        out = new LineWriter(new File(config.getOutputDir(), "deletionOrder.txt"), 16 * 1024, config.getCharset());
        try {
            Collections.reverse(orderedTables);
            TextFormatter.getInstance().write(orderedTables, false, out);
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            out.close();
        }
    }
}
