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
import java.util.stream.Collectors;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphService;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.info.NodeInfo;
import org.opennms.poc.graph.api.search.SearchCriteria;
import org.opennms.poc.graph.api.search.SearchProvider;
import org.opennms.poc.graph.api.search.SearchSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationSearchProvider implements SearchProvider {

    public static final String PROVIDER_ID = LocationSearchProvider.class.getSimpleName();

    // TODO MVR instead of requiring certain properties, we could implement certain types, like NodeAwareVertex, LocationAwareVertex (or graph)
    // in order to suggest/resolve?!

    @Autowired
    private MonitoringLocationDao locationDao;

    @Override
    public boolean canSuggest(String namespace) {
        return true; // Basically all locations may be seaerched
    }

    @Override
    public List<SearchSuggestion> getSuggestions(GraphService graphService, String namespace, String input) {
        final List<SearchSuggestion> searchSuggestions = locationDao.findMatching(new CriteriaBuilder(OnmsMonitoringLocation.class).ilike("id", "%" + input + "%").toCriteria())
                .stream()
                .map(l -> new SearchSuggestion(PROVIDER_ID, "location", l.getLocationName()))
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
        final List<Vertex> vertices = graph.getVertices().stream()
                .filter(v -> v.asGenericVertex().getProperty("node") != null && ((NodeInfo) v.asGenericVertex().getProperty("node")).getLocation() != null)
                .filter(v -> ((NodeInfo) v.asGenericVertex().getProperty("node")).getLocation().equalsIgnoreCase(searchCriteria.getCriteria()))
                .collect(Collectors.toList());
        return vertices;
    }
}
