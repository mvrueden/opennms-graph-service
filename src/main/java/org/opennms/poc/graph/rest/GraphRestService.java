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

import java.util.stream.Collectors;

import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/graphs")
public class GraphRestService {

    @Autowired
    private GraphService graphService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public GraphDTO getGraph() {
        final Graph graph = graphService.getGraph("simple");
        GraphDTO g = new GraphDTO();
        g.setLinks(graph.getEdges().stream().map(e -> new LinkDTO(e)).collect(Collectors.toList()));
        g.setVertices(graph.getVertices().stream().map(v -> new VertexDTO(v)).collect(Collectors.toList()));
        return g;
    }

}
