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

package org.opennms.poc.graph;

import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.search.SearchCriteria;
import org.opennms.poc.graph.api.search.SearchSuggestion;
import org.opennms.poc.graph.impl.nodes.NodeGraphProvider;
import org.opennms.poc.graph.impl.search.CategorySearchProvider;
import org.opennms.poc.graph.impl.service.DefaultGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CategorySearchProviderTest {

    @Autowired
    private CategorySearchProvider categorySearchProvider;

    @Autowired
    private DefaultGraphService graphService;

    @Autowired
    private NodeGraphProvider nodeGraphProvider;

    @Before
    public void setUp() throws InterruptedException {
        graphService.onBind(nodeGraphProvider, new HashMap());
        int retries = 3;
        while (retries > 0 && graphService.getGraph("nodes") == null) {
            retries --;
            Thread.sleep(500);
        }
    }

    @Test
    public void verifyCategorySuggestion() {
        final List<SearchSuggestion> suggestions = categorySearchProvider.getSuggestions(graphService, "nodes", "Routers");
        Assert.assertEquals(1, suggestions.size());
        Assert.assertEquals("Routers", suggestions.get(0).getLabel());
        Assert.assertEquals("category", suggestions.get(0).getContext());
        Assert.assertEquals(CategorySearchProvider.class.getSimpleName(), suggestions.get(0).getProvider());
    }

    @Test
    public void verifyCategorySearch() {
        final SearchCriteria criteria = new SearchCriteria();
        criteria.setProviderId(CategorySearchProvider.PROVIDER_ID);
        criteria.setNamespace("nodes");
        criteria.setCriteria("Routers");
        final List<Vertex> resolve = categorySearchProvider.resolve(graphService, criteria);
        Assert.assertEquals(1, resolve.size());
    }

}
