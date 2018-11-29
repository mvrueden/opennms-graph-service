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

import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.generic.GenericGraph;
import org.opennms.poc.graph.api.generic.GenericProperties;
import org.opennms.poc.graph.api.generic.GenericVertex;
import org.opennms.poc.graph.api.persistence.GraphRepository;
import org.opennms.poc.graph.api.persistence.PersistenceStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// Dummy GraphProvider, which shows all nodes. Required to simulate Node Enrichment
@Component
public class NodeGraphProvider implements GraphProvider {

    @Autowired
    private NodeDao nodeDao;

//    @Override
    public String getNamespace() {
        return "nodes";
    }

//    @Override
    public Graph getGraph() {
        final GenericGraph graph = new GenericGraph();
        graph.setNamespace(getNamespace());

        nodeDao.findAll().forEach(n -> {
            final GenericVertex v = new GenericVertex();
            v.setProperty(GenericProperties.NODE_ID, n.getId());
            v.setNamespace(getNamespace());
            v.setId("" + n.getId());

            graph.addVertex(v);
        });

        return graph;
    }

    @Override
    public void provideGraph(GraphRepository repository) {
        final Graph graph = getGraph();
        repository.save(graph, PersistenceStrategy.Memory.evictAfter(10, TimeUnit.SECONDS).reloadHook(this));
    }
}
