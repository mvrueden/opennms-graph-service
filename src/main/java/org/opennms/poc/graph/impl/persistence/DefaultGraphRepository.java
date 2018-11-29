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

package org.opennms.poc.graph.impl.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.info.GraphInfo;
import org.opennms.poc.graph.api.persistence.GraphRepository;
import org.opennms.poc.graph.api.persistence.PersistenceStrategy;
import org.springframework.stereotype.Component;

@Component
public class DefaultGraphRepository implements GraphRepository {

//    @Autowired
//    private GenericPersistenceAccessor accessor;

    private Map<String, Graph> storage = new HashMap<>();

    @Override
    public <V extends Vertex, E extends Edge<V>, G extends Graph<V, E>> void save(G graph) {
        save(graph, PersistenceStrategy.Memory);
    }

    @Override
    public <V extends Vertex, E extends Edge<V>, G extends Graph<V, E>, P extends PersistenceStrategy> void save(G graph, P persistenceStrategy) {
        if (graph != null) {
            if (persistenceStrategy == null || persistenceStrategy instanceof PersistenceStrategy.MemoryPersistenceStrategy) {
                storage.put(graph.getNamespace(), graph);
                // TODO MVR implement eviction
            }
            if (persistenceStrategy instanceof PersistenceStrategy.HibernatePersistenceStrategy) {
                // TODO MVR implement persistence to disk, for now push to memory storage
                storage.put(graph.getNamespace(), graph);
            }
        }
    }

    @Override
    public Graph<Vertex, Edge<Vertex>> findByNamespace(String namespace) {
        return storage.get(namespace); // TODO MVR we should also ask hibernate storage ...
//        final List<GraphEntity> objects = accessor.find("Select g from GraphEntity g where g.namespace = ?", namespace);
//        if (!objects.isEmpty()) {
//            return objects.get(0);
//        }
//        return null;
    }

    @Override
    public List<GraphInfo> findAll() {
        return storage.values().stream().map(g -> g.getInfo()).collect(Collectors.toList());
    }
}
