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

import static org.opennms.poc.graph.impl.nodes.NodePerformanceTestGraphProvider.NAMESPACE;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.network.IPAddress;
import org.opennms.core.network.IPAddressRange;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.poc.graph.api.persistence.GraphRepository;
import org.opennms.poc.graph.api.simple.SimpleEdge;
import org.opennms.poc.graph.api.simple.SimpleGraph;
import org.opennms.poc.graph.api.simple.SimpleVertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Lists;

/**
 * This test  will create a test graph, as follows:
 *
 *  - Create a Router Node
 *    - Name `Router <index>`
 *    - Add to Category `Routers,Test,Production,Development`
 *    - Add 5 interfaces
 *  - For each Router create 3 servers
 *     - Name `Server <index>`
 *     - Add all to Category `Servers`
 *     - Add one to Category `Test`
 *     - Add one to Category `Production`
 *     - Add one to Category `Development`
 *  - For each Router create 1 printer
 *     - Name `Printer <index>`
 *     - Add to category `Printers`
 *     - Add to category `Test,Production,Development`
 *
 *  - At the end, create a graph based on the node definition above. Afterwards all Routers are connected to a dummy vertex.
 *  - The graph generated consists of `5 * ROUTER_NODE_COUNT + 1` vertices and `5 * ROUTER_NODE_COUNT` edges.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DaoConfiguration.class, GraphGeneratorTest.class})
@Configuration
@ComponentScan(basePackages = "org.opennms.poc.graph.impl.persistence")
@EnableAutoConfiguration
public class GraphGeneratorTest {

    private static final int ROUTER_NODE_COUNT = 100000 / 5; // Total node count will be 5 * ROUTER_NODE_COUNT
    private static final int INTERFACE_COUNT = 5;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private MonitoringLocationDao locationDao;

    @Autowired
    private GraphRepository graphRepository;

    @Test
    public void generateGraph() {
        if (categoryDao.findByName("Printers") == null) {
            OnmsCategory category = new OnmsCategory("Printers", "All printers");
            categoryDao.save(category);
        }

        // 1st persist all nodes to database
        final IPAddressRange ipRange = new IPAddressRange("10.0.0.1", "10.254.254.254");
        final Iterator<IPAddress> iterator = ipRange.iterator();
        final List<OnmsCategory> categoryList = Lists.newArrayList(
                categoryDao.findByName("Test"),
                categoryDao.findByName("Development"),
                categoryDao.findByName("Production"));
        if (nodeDao.countAll() != 0) {
            throw new IllegalStateException("Cannot create graph if nodes exist. Bailing.");
        }
        // The graph to populate
        final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> nodeGraph = new SimpleGraph<>(NAMESPACE);
        nodeGraph.setLabel("Nodes Performance Test");
        nodeGraph.setDescription("Graph with roughly 100k nodes and > 500k links. Used to verify if the current approach is performant enough");

        // Reserved for focus strategy (not yet implemented) // TODO MVR implement
        final List<String> routerIds = Lists.newArrayList();

        // Dummy vertex all routers are used to connect
        final SimpleVertex connectingVertex = new SimpleVertex(NAMESPACE, Integer.toString(Integer.MAX_VALUE));
        connectingVertex.setLabel("Dummy Connection");

        // Create
        for (int i = 0; i< ROUTER_NODE_COUNT; i++) {
            // Create Router Node
            final OnmsNode routerNode = new OnmsNode();
            routerNode.setLabel("Router " + (i+1));
            routerNode.addCategory(categoryDao.findByName("Routers"));
            routerNode.addCategory(categoryDao.findByName("Test"));
            routerNode.addCategory(categoryDao.findByName("Development"));
            routerNode.addCategory(categoryDao.findByName("Production"));
            routerNode.setLocation(locationDao.getDefaultLocation());
            // Create Router interfaces
            for (int a=0; a<INTERFACE_COUNT; a++) {
                final OnmsIpInterface routerInterface = new OnmsIpInterface();
                routerInterface.setIpAddress(iterator.next().toInetAddress());
                routerInterface.setNode(routerNode);
                routerNode.addIpInterface(routerInterface);
            }
            nodeDao.save(routerNode);
            final SimpleVertex routerVertex = convert(routerNode);

            routerIds.add(routerNode.getNodeId());
            nodeGraph.addVertex(routerVertex);

            // Create 3 servers...
            for (int a=0; a<3; a++) {
                final OnmsNode serverNode = new OnmsNode();
                serverNode.setLabel("Server " + (i*a+1));
                serverNode.addCategory(categoryList.get(a));
                serverNode.addCategory(categoryDao.findByName("Servers"));
                serverNode.setLocation(locationDao.getDefaultLocation());
                final OnmsIpInterface serverInterface = new OnmsIpInterface();
                serverInterface.setIpAddress(iterator.next().toInetAddress());
                serverInterface.setNode(serverNode);
                routerNode.addIpInterface(serverInterface);
                nodeDao.save(serverNode);

                // Add server vertex
                final SimpleVertex serverVertex = convert(serverNode);
                nodeGraph.addVertex(serverVertex);
                nodeGraph.addEdge(new SimpleEdge<>(routerVertex, serverVertex));
            }

            // And 1 printer
            final OnmsNode printerNode = new OnmsNode();
            printerNode.setLabel("Printer " + (i+1));
            printerNode.addCategory(categoryList.get(0));
            printerNode.addCategory(categoryList.get(1));
            printerNode.addCategory(categoryList.get(2));
            printerNode.addCategory(categoryDao.findByName("Printers"));
            printerNode.setLocation(locationDao.getDefaultLocation());
            final OnmsIpInterface printerInterface = new OnmsIpInterface();
            printerInterface.setIpAddress(iterator.next().toInetAddress());
            printerInterface.setNode(printerNode);
            routerNode.addIpInterface(printerInterface);
            nodeDao.save(printerNode);

            // Add printer vertex
            final SimpleVertex printerVertex = convert(printerNode);
            nodeGraph.addVertex(printerVertex);
            nodeGraph.addEdge(new SimpleEdge(routerVertex, printerVertex));

            // Finally connect router
            nodeGraph.addEdge(new SimpleEdge<>(connectingVertex, routerVertex));

            if (i > 0 && i % 1000 == 0) {
                System.out.println("Generated router nodes " + (i - 1000) + " - " + i);
            }
        }
        nodeDao.flush();

        // Finally persist
        System.out.println("Persisting Graph...");
        graphRepository.save(nodeGraph);
    }

    private static SimpleVertex convert(OnmsNode node) {
        final SimpleVertex vertex = new SimpleVertex(NAMESPACE, node.getNodeId());
        if (node.getNodeId() != null) {
            vertex.setNodeRefString(node.getNodeId());
        }
        vertex.setLabel(node.getLabel());
        return vertex;
    }

}
