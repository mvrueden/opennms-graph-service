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

package org.opennms.poc.graph.impl.nodes;

import java.util.function.Function;

import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphNotificationService;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.generic.GenericGraph;
import org.opennms.poc.graph.api.generic.GenericProperties;
import org.opennms.poc.graph.api.info.DefaultGraphInfo;
import org.opennms.poc.graph.api.info.GraphInfo;
import org.opennms.poc.graph.api.info.NodeInfo;
import org.opennms.poc.graph.api.persistence.GraphRepository;
import org.opennms.poc.graph.api.simple.SimpleEdge;
import org.opennms.poc.graph.api.simple.SimpleGraph;
import org.opennms.poc.graph.api.simple.SimpleVertex;
import org.opennms.poc.graph.impl.enrichment.VertexFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NodePerformanceTestGraphProvider implements GraphProvider<SimpleVertex, SimpleEdge<SimpleVertex>> {

    public static final String NAMESPACE = "nodes-performance";

    @Autowired
    private GraphRepository graphRepository;

    @Autowired
    private VertexFactory vertexFactory;

    @Override
    public Graph<SimpleVertex, SimpleEdge<SimpleVertex>> loadGraph() {
        final Function<GenericGraph, SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>>> generictoSimpleGraphTransformer = genericGraph -> {
            final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> simpleGraph = new SimpleGraph<>(genericGraph.getNamespace(), SimpleVertex.class);
            simpleGraph.setLabel(genericGraph.getLabel());
            simpleGraph.setDescription(genericGraph.getDescription());

            genericGraph.getVertices().forEach(genericVertex -> {
                try {
                    final SimpleVertex eachSimpleVertex = vertexFactory.createVertex(SimpleVertex.class, simpleGraph.getNamespace(), genericVertex.getId());
                    eachSimpleVertex.setLabel(genericVertex.getProperty(GenericProperties.LABEL));
                    eachSimpleVertex.setIconKey(genericVertex.getProperty(GenericProperties.ICON_KEY));
                    eachSimpleVertex.setTooltip(genericVertex.getProperty(GenericProperties.TOOLTIP));
                    eachSimpleVertex.setNodeRefString(genericVertex.getProperty(GenericProperties.NODE_REF));
                    eachSimpleVertex.setNodeInfo((NodeInfo) genericVertex.getComputedProperties().get("node"));
                    simpleGraph.addVertex(eachSimpleVertex);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

            genericGraph.getEdges().forEach(genericEdge -> {
                final SimpleVertex sourceVertex = simpleGraph.getVertex(genericEdge.getSource().getId());
                final SimpleVertex targetVertex = simpleGraph.getVertex(genericEdge.getTarget().getId());
                final SimpleEdge<SimpleVertex> eachSimpleEdge = new SimpleEdge<>(sourceVertex, targetVertex);
                simpleGraph.addEdge(eachSimpleEdge);
            });

            return simpleGraph;
        };

        return graphRepository.findByNamespace(NAMESPACE, generictoSimpleGraphTransformer);
    }

    @Override
    public GraphInfo getGraphInfo() {
        DefaultGraphInfo graphInfo = new DefaultGraphInfo(NAMESPACE, SimpleVertex.class);
        final GraphInfo persistedGraphInfo = graphRepository.findGraphInfo(NAMESPACE);
        if (persistedGraphInfo != null) { // TODO MVR this weird... hmmmm maybe do not include those if the graph info is not yet available?
            // We overwrite the settings here, as the findGraphInfo does not know the vertex type yet, so we programmatically set it.
            // TODO MVR probably not the best approach (-;
            graphInfo.setLabel(persistedGraphInfo.getLabel());
            graphInfo.setDescription(persistedGraphInfo.getDescription());
        }
        return graphInfo;
    }

    @Override
    public void setNotificationService(GraphNotificationService notificationService) {

    }
}
