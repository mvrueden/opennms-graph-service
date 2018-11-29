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

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GraphRepositoryTest {

//    private static final String NAMESPACE = "dummy";
//
//    @Autowired
//    private GraphRepository graphRepository;
//
//    @Test
//    public void verifyGraphPersistence() {
//        GraphEntity graphEntity = graphRepository.findByNamespace(NAMESPACE);
//        Assert.assertNull(graphEntity); // nothing persisted yet
//
//        GenericGraph g = new GenericGraph();
//        g.setNamespace(NAMESPACE);
//
//        GenericVertex v1 = new GenericVertex(NAMESPACE, "v1");
//        v1.setProperty("label", "Vertex 1"); // verify string
//        v1.setProperty("status", Response.Status.OK); // Verify enum
//        v1.setProperty("number", 10); // Verify int
//        v1.setProperty("enabled", true); // Verify boolean
//
//        GenericVertex v2 = new GenericVertex(NAMESPACE, "v2");
//        v2.setProperty("label", "Vertex 2");
//        v2.setProperty("status", Response.Status.BAD_REQUEST);
//        v2.setProperty("number", 20);
//        v2.setProperty("enabled", false);
//
//        g.addVertex(v1);
//        g.addVertex(v2);
//        g.addEdge(new GenericEdge(v1, v2));
//
//        graphEntity = convert(g);
//        graphRepository.persist(graphEntity);
//
//        final GraphEntity graphEntity2 = graphRepository.findByNamespace(NAMESPACE);
//        System.out.println(graphEntity2);
//
//    }
//
//    // TODO MVR write same test with simpleGraph
//
//    private List<Class> supportedClasses = Lists.newArrayList(
//            Boolean.class, Float.class, Integer.class, Double.class, String.class, Short.class, Byte.class
//    );
//
//    private GraphEntity convert(GenericGraph g) {
//        final GraphEntity ge = new GraphEntity();
//        ge.setNamespace(g.getNamespace());
//
//        g.getVertices().stream().forEach(v -> {
//            final VertexEntity ve = new VertexEntity();
//            for(Map.Entry<String, Object> property : v.getProperties().entrySet()) {
//                if (property.getValue() != null) {
//                    if (supportedClasses.contains(property.getValue().getClass()) || property.getValue().getClass().isEnum()) {
//                        final Property p = new Property();
//                        p.setName(property.getKey());
//                        p.setType(property.getValue().getClass());
//                        p.setValue(property.getValue().toString());
//                        ve.getProperties().add(p);
//                    } else {
//                        throw new IllegalStateException("Vertex contains properties which is neither a primitive nor an enum. Cannot persist");
//                    }
//                }
//            }
//        });
//        return ge;
//    }
}
