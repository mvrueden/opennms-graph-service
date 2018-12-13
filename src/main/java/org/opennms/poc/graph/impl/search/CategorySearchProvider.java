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
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphService;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.aware.NodeAware;
import org.opennms.poc.graph.api.search.SearchCriteria;
import org.opennms.poc.graph.api.search.SearchProvider;
import org.opennms.poc.graph.api.search.SearchSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategorySearchProvider implements SearchProvider {

    public static final String PROVIDER_ID = CategorySearchProvider.class.getSimpleName();

    @Autowired
    private CategoryDao categoryDao;

    @Override
    public boolean canSuggest(GraphService graphService, String namespace) {
        return NodeAware.class.isAssignableFrom(graphService.getGraphInfo(namespace).getVertexType());
    }

    @Transactional
    @Override
    public List<SearchSuggestion> getSuggestions(GraphService graphService, String namespace, String input) {
        return categoryDao.findMatching(new CriteriaBuilder(OnmsCategory.class).ilike("name", "%" + input + "%").toCriteria())
            .stream()
            .map(c -> new SearchSuggestion(PROVIDER_ID, "category", c.getName()))
            .collect(Collectors.toList());
    }

    @Override
    public boolean canResolve(String providerId) {
        return PROVIDER_ID.equalsIgnoreCase(providerId);
    }

    @Override
    public List<Vertex> resolve(GraphService graphService, SearchCriteria searchCriteria) {
        final Graph<Vertex, Edge<Vertex>> graph = graphService.getGraph(searchCriteria.getNamespace());
        return graph.getVertices().stream()
                .map(v -> (NodeAware) v)
                .filter(v -> v.getNodeInfo().getCategories().contains(searchCriteria.getCriteria()))
                .map(v -> (Vertex) v)
                .collect(Collectors.toList());
    }
}
