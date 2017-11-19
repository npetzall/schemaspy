package org.schemaspy.service;

import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.View;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by rkasa on 2016-12-10.
 */

public class ViewService {

    private final SqlService sqlService;
    private final Config config;

    private static final Logger LOGGER = Logger.getLogger(ViewService.class.getName());

    public ViewService(SqlService sqlService, Config config) {
        this.sqlService = Objects.requireNonNull(sqlService);
        this.config = Objects.requireNonNull(config);
    }

    /**
     * Extract the SQL that describes this view from the database
     *
     * @return
     * @throws SQLException
     */
    public String fetchViewSql(Database db, View view) throws SQLException {
        String selectViewSql = config.getDbProperties().getProperty("selectViewSql");
        if (selectViewSql == null) {
            return null;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        StringBuilder viewDefinition = new StringBuilder();
        try {
            stmt = sqlService.prepareStatement(selectViewSql,db, view.getName());
            rs = stmt.executeQuery();
            while (rs.next()) {
                try {
                    viewDefinition.append(rs.getString("view_definition"));
                } catch (SQLException tryOldName) {
                    viewDefinition.append(rs.getString("text"));
                }
            }
            return viewDefinition.toString();
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, selectViewSql);
            throw sqlException;
        } finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
        }
    }
}
