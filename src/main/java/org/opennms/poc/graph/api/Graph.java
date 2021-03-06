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

package org.opennms.poc.graph.api;

import java.util.Collection;
import java.util.List;

import org.opennms.poc.graph.api.generic.GenericGraph;
import org.opennms.poc.graph.api.info.GraphInfo;
import org.opennms.poc.graph.impl.refs.NodeRef;

public interface Graph<V extends Vertex, E extends Edge<V>> extends GraphInfo {
    List<V> getVertices();

    List<E> getEdges();

    String getNamespace();

    void addEdges(Collection<E> edges);

    void addVertices(Collection<V> vertices);

    void addVertex(V vertex);
    void addEdge(E edge);

    void removeEdge(E edge);
    void removeVertex(V vertex);

    // TODO MVR make this more generic...
    V getVertex(String id);

    E getEdge(String id);

    List<String> getVertexIds();
    List<String> getEdgeIds();

    // TODO MVR also provide List<String> vertexIds, int szl method
    Graph<V,E> getSnapshot(Collection<V> verticesInFocus, int szl);

    List<V> resolveVertices(Collection<String> vertexIds);
    List<E> resolveEdges(Collection<String> edgeIds);
    Collection<V> getNeighbors(V eachVertex);
    Collection<E> getConnectingEdges(V eachVertex);

    List<Vertex> getDefaultFocus();

    Vertex getVertex(NodeRef nodeRef);

    GenericGraph asGenericGraph();

}
