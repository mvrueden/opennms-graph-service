/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.poc.graph.api.focus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.VertexRef;

import com.google.common.collect.Lists;

public class FocusStrategy {
    public static final Focus ALL = graphContext -> graphContext.getGraph().getVertices();

    public static final Focus EMPTY = graphContext -> Lists.newArrayList();

    public static final Focus FIRST = graphContext -> {
        Graph<Vertex, Edge<Vertex>> g = graphContext.getGraph();
        if (g.getVertexIds().isEmpty()) {
            return new ArrayList<>();
        }
        final Vertex vertex = g.getVertices().get(0);
        return Lists.newArrayList(vertex);
    };

    public static final Focus SPECIFIC(Collection<String> vertexIds) {
        return graphContext -> {
            final List<VertexRef> list = graphContext.getGraph().resolveVertices(vertexIds);
            return list;
        };
    }
}
