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

package org.opennms.poc.graph.impl.search;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphService;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.generic.GenericVertex;
import org.opennms.poc.graph.api.search.SearchCriteria;
import org.opennms.poc.graph.api.search.SearchProvider;
import org.opennms.poc.graph.api.search.SearchSuggestion;
import org.springframework.stereotype.Service;

@Service
public class GenericAttributeSearchProvider implements SearchProvider {

    public static final String PROVIDER_ID = GenericAttributeSearchProvider.class.getSimpleName();

    @Override
    public boolean canSuggest(GraphService graphService, String namespace) {
        return true; // always return true
    }

    @Override
    public List<SearchSuggestion> getSuggestions(GraphService graphService, String namespace, String input) {
        final Graph<Vertex, Edge<Vertex>> graph = graphService.getGraph(namespace);
        final List<SearchSuggestion> searchSuggestions = graph.getVertices().stream()
                .map(v -> v.asGenericVertex())
                .flatMap(v -> v.getProperties().entrySet().stream())
                .filter(e -> e.getValue().toString().contains(input))
                .map(e -> new SearchSuggestion(PROVIDER_ID, e.getKey(), e.getValue().toString()))
                .collect(Collectors.toList());
        return searchSuggestions;
    }

    @Override
    public boolean canResolve(String providerId) {
        return PROVIDER_ID.equalsIgnoreCase(providerId);
    }

    @Override
    public List<Vertex> resolve(GraphService graphService, SearchCriteria searchCriteria) {
        final Graph<Vertex, Edge<Vertex>> graph = graphService.getGraph(searchCriteria.getNamespace());
        final List<Vertex> collect = graph.getVertices().stream()
                .filter(v -> {
                    final GenericVertex genericVertex = v.asGenericVertex();
                    final Map<String, Object> properties = genericVertex.getProperties();
                    final Object property = properties.get(searchCriteria.getContext());
                    return property != null && property.toString().equalsIgnoreCase(searchCriteria.getCriteria());
                })
                .collect(Collectors.toList());
        return collect;
    }
}
