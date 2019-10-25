/*
 * Copyright (C) 2019 Nils Petzaell
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
package org.schemaspy.input.dbms.xml;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Objects;

public class RoutineMeta {

    public final String name;
    public final String comments;

    public static RoutineMeta from(Node node) {
        String name, comments;
        NamedNodeMap attributes = node.getAttributes();
        name = attributes.getNamedItem("name").getNodeValue().trim();
        Node commentNode = attributes.getNamedItem("comments");
        comments = Objects.nonNull(commentNode) ? commentNode.getNodeValue().trim(): "";
        return new RoutineMeta(name, comments);
    }

    public RoutineMeta(String name, String comments) {
        this.name = name;
        this.comments = comments;
    }
}
