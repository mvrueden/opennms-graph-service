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

package org.opennms.poc.graph.rest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphService;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.generic.GenericEdge;
import org.opennms.poc.graph.api.generic.GenericVertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/graphs")
public class GraphRestService {

    @Autowired
    private GraphService graphService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> listNamespaces() {
        return graphService.getGraphs().stream().map(g -> g.getNamespace()).collect(Collectors.toList());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path="{namespace}")
    public JSONObject getGraph(@PathVariable(name="namespace") String namespace) {
        final Graph<Vertex, Edge> graph = graphService.getGraph(namespace);
        final JSONObject jsonGraph = new JSONObject();
        final JSONArray edgesArray = new JSONArray();
        final JSONArray verticesArray = new JSONArray();
        jsonGraph.put("edges", edgesArray);
        jsonGraph.put("vertices", verticesArray);

        jsonGraph.putAll(graph.asGenericGraph().getProperties());
        graph.getEdges().stream().forEach(edge -> {
            final GenericEdge genericEdge = edge.asGenericEdge();
            final Map<String, Object> edgeProperties = genericEdge.getProperties();
            edgeProperties.put("source", genericEdge.getSource().getId());
            edgeProperties.put("target", genericEdge.getTarget().getId());
            edgesArray.add(edgeProperties);
        });
        graph.getVertices().stream().forEach(vertex -> {
            final GenericVertex genericVertex = vertex.asGenericVertex();
            verticesArray.add(genericVertex.getProperties());
        });
        return jsonGraph;
    }

}
