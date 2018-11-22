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

package org.opennms.poc.graph.api.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.generic.GenericGraph;

// TODO MVR enforce namespace
// TODO MVR this is basically a copy of GenericGraph :'(
public class SimpleGraph<V extends SimpleVertex, E extends SimpleEdge<V>> implements Graph<V, E> {
    private final List<V> vertices = new ArrayList<>();
    private final List<E> edges = new ArrayList<>();
    private final String namespace;

    public SimpleGraph(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public List<V> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    @Override
    public List<E> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public void addEdges(List<E> edges) {
        edges.addAll(edges);
    }

    @Override
    public void addVertices(List<V> vertices) {
        vertices.addAll(vertices);
    }

    @Override
    public void addVertices(V... vertices) {
        addVertices(Arrays.asList(vertices));
    }

    @Override
    public void addEdges(E... edges) {
        addEdges(Arrays.asList(edges));
    }

    @Override
    public V getVertex(String id) {
        return getVertices().stream().filter(v -> v.getId().equals(id)).findAny().orElseThrow(() -> new NoSuchElementException("No vertex available with id [" + id + "]"));
    }

    @Override
    public GenericGraph asGenericGraph() {
        final GenericGraph graph = new GenericGraph();
        graph.setNamespace(getNamespace());
        getVertices().stream().map(Vertex::asGenericVertex).forEach(graph::addVertices);
        getEdges().stream().map(Edge::asGenericEdge).forEach(graph::addEdges);
        return graph;
    }

}
