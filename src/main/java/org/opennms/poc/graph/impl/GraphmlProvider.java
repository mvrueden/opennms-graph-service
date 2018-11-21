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

package org.opennms.poc.graph.impl;

import static org.opennms.poc.graph.impl.GraphmlProvider.GraphMLProperties.NAMESPACE;

import java.io.InputStream;
import java.util.stream.Collectors;

import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLGraph;
import org.opennms.features.graphml.model.GraphMLReader;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.poc.graph.DefaultEdge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.GraphProviderDescriptor;

public class GraphmlProvider implements GraphProvider {

    // TODO MVR copied over from OpenNMS
    public interface GraphMLProperties {
        String ID = "id";
        String DESCRIPTION = "description";
        String NAMESPACE = "namespace";
        String ICON_KEY = "iconKey";
        String IP_ADDRESS = "ipAddr";
        String LABEL = "label";
        String LOCKED = "locked";
        String NODE_ID = "nodeID";
        String FOREIGN_SOURCE = "foreignSource";
        String FOREIGN_ID = "foreignID";
        String SELECTED = "selected";
        String STYLE_NAME = "styleName";
        String TOOLTIP_TEXT = "tooltipText";
        String X = "x";
        String Y = "y";
        String PREFERRED_LAYOUT = "preferred-layout";
        String FOCUS_STRATEGY = "focus-strategy";
        String FOCUS_IDS = "focus-ids";
        String SEMANTIC_ZOOM_LEVEL = "semantic-zoom-level";
        String VERTEX_STATUS_PROVIDER = "vertex-status-provider";
        String LEVEL = "level";
        String EDGE_PATH_OFFSET = "edge-path-offset";
        String BREADCRUMB_STRATEGY = "breadcrumb-strategy";
    }

    private final Graph graph;

    public GraphmlProvider(InputStream inputStream) throws InvalidGraphException {
        final GraphML metaGraph = GraphMLReader.read(inputStream);
        // TODO MVR NPE...
        final GraphMLGraph theGraph = metaGraph.getGraphs().get(1);
        final DefaultGraph graph = new DefaultGraph(theGraph.getProperty(NAMESPACE));
        graph.addVertices(theGraph.getNodes().stream().map(n -> new DefaultVertex(n.getProperties())).collect(Collectors.toList()));
        graph.addEdges(theGraph.getEdges().stream().map(e -> {
            final DefaultEdge edge = new DefaultEdge(graph.getVertex(e.getSource().getId()), graph.getVertex(e.getTarget().getId()));
            edge.setProperties(e.getProperties());
            return edge;
        }).collect(Collectors.toList()));
        this.graph = graph;
    }

    @Override
    public GraphProviderDescriptor getGraphProviderDescriptor() {
        return new GraphProviderDescriptor(graph.getNamespace());
    }

    @Override
    public String getNamespace() {
        return graph.getNamespace();
    }

    @Override
    public Graph getGraph() {
        return graph;
    }
}
