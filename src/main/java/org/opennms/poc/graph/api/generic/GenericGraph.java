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

package org.opennms.poc.graph.api.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.info.GraphInfo;

import com.google.common.collect.Lists;

// TODO MVR enforce namespace
public class GenericGraph extends AbstractElement implements Graph<GenericVertex, GenericEdge> {

    private final List<GenericVertex> vertices = new ArrayList<>();
    private final List<GenericEdge> edges = new ArrayList<>();

    public GenericGraph() {}

    public GenericGraph(Map<String, Object> properties) {
        setProperties(properties);
    }

    @Override
    public List<GenericVertex> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    @Override
    public List<GenericEdge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    @Override
    public GenericVertex getVertex(String id) {
        return getVertices().stream().filter(v -> v.getId().equals(id)).findAny().orElse(null);
    }

    @Override
    public GraphInfo getInfo() {
        final GraphInfo info = new GraphInfo();
        info.setNamespace(getNamespace());
        info.setDescription(getProperty(GenericProperties.DESCRIPTION));
        info.setName(getProperty(GenericProperties.LABEL));
        return info;
    }

    @Override
    public void addEdges(List<GenericEdge> edges) {
        for (GenericEdge eachEdge : edges) {
            addEdge(eachEdge);
        }
    }

    @Override
    public void addVertices(List<GenericVertex> vertices) {
        this.vertices.addAll(vertices); // TODO MVR verify only add if not already added
    }

    @Override
    public void addVertex(GenericVertex vertex) {
        Objects.requireNonNull(vertex);
        addVertices(Lists.newArrayList(vertex));
    }

    @Override
    public void addEdge(GenericEdge edge) {
        Objects.requireNonNull(edge);
        if (edge.getSource().getNamespace().equalsIgnoreCase(getNamespace()) && getVertex(edge.getSource().getId()) == null) {
            addVertex(edge.getSource());
        }
        if (edge.getTarget().getNamespace().equalsIgnoreCase(getNamespace()) && getVertex(edge.getTarget().getId()) == null) {
            addVertex(edge.getTarget());
        }
        this.edges.add(edge);
    }

    @Override
    public GenericGraph asGenericGraph() {
        return this;
    }
}
