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
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphService;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.aware.NodeAware;
import org.opennms.poc.graph.api.search.SearchCriteria;
import org.opennms.poc.graph.api.search.SearchProvider;
import org.opennms.poc.graph.api.search.SearchSuggestion;
import org.opennms.poc.graph.impl.refs.NodeRef;
import org.opennms.poc.graph.impl.refs.NodeRefs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

@Service
public class CategorySearchProvider implements SearchProvider {

    public static final String PROVIDER_ID = CategorySearchProvider.class.getSimpleName();

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private GenericPersistenceAccessor persistenceAccessor;

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
        // Query data
        final List<Object[]> nodeRefObjects = persistenceAccessor.executeNativeQuery("select distinct node.nodeid, node.foreignsource, node.foreignid from node\n" +
                "  join category_node on node.nodeid = category_node.nodeid\n" +
                "  join categories on category_node.categoryid = categories.categoryid\n" +
                "  where categories.categoryname like :search", ImmutableMap.of("search", searchCriteria.getCriteria()));

        // Convert to node refs
        final List<NodeRef> nodeRefs = nodeRefObjects.stream().map(objects -> {
            if (objects[1] != null && objects[2] != null) {
                return NodeRefs.from(objects[1] + ":" + objects[2]);
            }
            return NodeRefs.from((Integer) objects[0]);
        }).collect(Collectors.toList());

        // Collect Vertices
        final Graph<Vertex, Edge<Vertex>> graph = graphService.getGraph(searchCriteria.getNamespace());
        final List<Vertex> verticesInCategory = nodeRefs.stream()
                .map(nodeRef -> graph.getVertex(nodeRef))
                .filter(v -> v != null)
                .collect(Collectors.toList());
        return verticesInCategory;
    }
}
