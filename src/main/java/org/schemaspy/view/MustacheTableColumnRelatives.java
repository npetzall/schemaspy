/*
 * Copyright (C) 2016, 2017 Rafal Kasa
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.view;

import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;

import java.util.Set;

/**
 * Created by rkasa on 2016-03-24.
 *
 * @author Rafal Kasa
 */
public class MustacheTableColumnRelatives {
    private TableColumn column;
    private Table table;
    private Set<ForeignKeyConstraint> constraints;
    private String path;

    public MustacheTableColumnRelatives(Set<ForeignKeyConstraint> constraints) {
        this.constraints = constraints;
    }

    public MustacheTableColumnRelatives(TableColumn column, Set<ForeignKeyConstraint> constraints) {
        this(constraints);
        this.column = column;
        this.table = column.getTable();
        this.path = table.isRemote() ? ("../../" + FileNameGenerator.generate(table.getContainer()) + "/tables/") : "";
    }

    public Table getTable() {
        return table;
    }

    public String getPath() {
        return path;
    }

    public Set<ForeignKeyConstraint> getConstraint() {
        return constraints;
    }

    public TableColumn getColumn() {
        return column;
    }
}
