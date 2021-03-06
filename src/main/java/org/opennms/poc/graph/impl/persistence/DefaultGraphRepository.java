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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.generic.GenericEdge;
import org.opennms.poc.graph.api.generic.GenericGraph;
import org.opennms.poc.graph.api.generic.GenericProperties;
import org.opennms.poc.graph.api.generic.GenericVertex;
import org.opennms.poc.graph.api.info.DefaultGraphInfo;
import org.opennms.poc.graph.api.info.GraphInfo;
import org.opennms.poc.graph.api.persistence.EdgeEntity;
import org.opennms.poc.graph.api.persistence.GraphEntity;
import org.opennms.poc.graph.api.persistence.GraphRepository;
import org.opennms.poc.graph.api.persistence.PropertyEntity;
import org.opennms.poc.graph.api.persistence.VertexEntity;
import org.opennms.poc.graph.impl.change.ChangeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Component
@Transactional
public class DefaultGraphRepository implements GraphRepository {

    @FunctionalInterface
    interface Converter<T> {
        T toValue(Class<T> type, String string);
    }

    private final Map<Class<?>, Converter<?>> converterMap = new HashMap<>();
    private final List<Class> supportedClasses = Lists.newArrayList(
            Boolean.class, Float.class, Integer.class, Double.class, String.class, Short.class, Byte.class
    );

    @Autowired
    private GenericPersistenceAccessor accessor;

    public DefaultGraphRepository() {
        converterMap.put(Boolean.class, (type, string) -> Boolean.valueOf(string));
        converterMap.put(Float.class, (type, string) -> Float.valueOf(string));
        converterMap.put(Integer.class, (type, string) -> Integer.valueOf(string));
        converterMap.put(Double.class, (type, string) -> Double.valueOf(string));
        converterMap.put(String.class, (type, string) -> string);
        converterMap.put(Short.class, (type, string) -> Short.valueOf(string));
        converterMap.put(Byte.class, (type, string) -> Byte.valueOf(string));
        converterMap.put(Enum.class, (Converter<Enum>) (type, string) -> Enum.valueOf(type, string));

        for (Class eachSupportedClass : supportedClasses) {
            if (converterMap.get(eachSupportedClass) == null) {
                throw new IllegalStateException("Missing converter for supported class '" + eachSupportedClass + "'");
            }
        }
    }

    @Override
    @Transactional
    public <V extends Vertex, E extends Edge<V>, G extends Graph<V, E>> void save(G graph) {
        Objects.requireNonNull(graph);
        long start = System.currentTimeMillis();
        System.out.println("Converting graph to generic graph");
        final GenericGraph genericGraph = graph.asGenericGraph();
        System.out.println("DONE. Took " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        System.out.println("Converting to graph entity");
        final GraphEntity graphEntity = toEntity(genericGraph);
        System.out.println("DONE. Took " + (System.currentTimeMillis() - start) + "ms");

        // Here we detect if a graph must be updated or persisted
        // This way we always detect changes even if the entity persisted was not received from the persistence context
        // in the first place.
        final GenericGraph persistedGraph = findByNamespace(graph.getNamespace());
        if (persistedGraph == null) {
            System.out.println("ACTUALLY START PERSISTING NOW... WIU WIU WIU");
            start = System.currentTimeMillis();
            accessor.save(graphEntity);
            System.out.println("DONE. Took " + (System.currentTimeMillis() - start) + "ms");
        } else {
            final ChangeSet<GenericVertex, GenericEdge> changeSet = new ChangeSet<>(persistedGraph, genericGraph);
            if (changeSet.hasChanges()) {
                changeSet.getEdgesRemoved().forEach(edge -> persistedGraph.removeEdge(edge));
                changeSet.getEdgesAdded().forEach(edge -> persistedGraph.addEdge(edge));
                changeSet.getEdgesUpdated().forEach(edge -> {
                    final GenericEdge persistedEdge = persistedGraph.getEdge(edge.getId());
                    persistedEdge.setProperties(edge.getProperties());
                });
                changeSet.getVerticesRemoved().forEach(vertex -> persistedGraph.removeVertex(vertex));
                changeSet.getVerticesAdded().forEach(vertex -> persistedGraph.addVertex(vertex));
                changeSet.getVerticesUpdated().forEach(vertex -> {
                    final GenericVertex persistedVertex = persistedGraph.getVertex(vertex.getId());
                    persistedVertex.setProperties(vertex.getProperties());
                });
                accessor.update(persistedGraph);
            }
        }
    }

    @Override
    public GenericGraph findByNamespace(String namespace) {
        final List<GraphEntity> graphs = accessor.find("Select g from GraphEntity g where g.namespace = ?", namespace);
        if (graphs.isEmpty()) {
            return null;
        }
        final GraphEntity graphEntity = graphs.get(0);
        final GenericGraph genericGraph = fromEntity(graphEntity);
        return genericGraph;
    }

    @Override
    public GraphInfo findGraphInfo(String namespace) {
        List<GraphEntity> graphEntities = accessor.find("select ge from GraphEntity ge where ge.namespace = ?", namespace);
        if (graphEntities == null || graphEntities.isEmpty()) {
            return null;
        }
        return convert(graphEntities.get(0));
    }

    @Override
    public <G extends Graph<V, E>, V extends Vertex, E extends Edge<V>> G findByNamespace(String namespace, Function<GenericGraph, G> transformer) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(transformer);
        final GenericGraph genericGraph = findByNamespace(namespace);
        if (genericGraph != null) {
            final G convertedGraph = transformer.apply(genericGraph);
            return convertedGraph;
        }
        return null;
    }

    @Override
    public List<GraphInfo> findAll() {
        final List<GraphEntity> graphs = accessor.find("Select g from GraphEntity g");
        final List<GraphInfo> graphInfos = graphs.stream().map(g -> convert(g)).collect(Collectors.toList());
        return graphInfos;
    }

    @Override
    public void deleteByNamespace(String namespace) {
        // TODO MVR implement delete cascade automatically
        final GenericGraph graph = findByNamespace(namespace);
        if (graph != null) {
            accessor.delete(graph);
        }
    }

    private GenericGraph fromEntity(final GraphEntity graphEntity) {
        final GenericGraph genericGraph = new GenericGraph();
        genericGraph.setId(graphEntity.getNamespace());  // TODO MVR the properties contain the id and namespace as well, this is kinda pointless
        genericGraph.setNamespace(graphEntity.getNamespace()); // TODO MVR the properties contain the id and namespace as well, this is kinda pointless
        graphEntity.getProperties().forEach(property -> {
            final Object value = convert(property);
            genericGraph.setProperty(property.getName(), value);
        });

        graphEntity.getVertices().stream().forEach(vertexEntity -> {
            final GenericVertex genericVertex = new GenericVertex();
            genericVertex.setNamespace(graphEntity.getNamespace()); // TODO MVR the properties contain the namespace as well, this is kinda pointless
            vertexEntity.getProperties().forEach(property -> {
                final Object value = convert(property);
                genericVertex.setProperty(property.getName(), value);
            });
            genericGraph.addVertex(genericVertex);
        });

        graphEntity.getEdges().stream().forEach(edgeEntity -> {
            final GenericEdge genericEdge = new GenericEdge(
                    genericGraph.getVertex(edgeEntity.getSource().getProperty(GenericProperties.ID).getValue()),
                    genericGraph.getVertex(edgeEntity.getTarget().getProperty(GenericProperties.ID).getValue()));
            genericEdge.setNamespace(graphEntity.getNamespace()); // TODO MVR the properties contain the namespace as well, this is kinda pointless
            edgeEntity.getProperties().forEach(property -> {
                final Object value = convert(property);
                genericEdge.setProperty(property.getName(), value);
            });
            genericGraph.addEdge(genericEdge);
        });

        return genericGraph;
    }

    private GraphEntity toEntity(GenericGraph genericGraph) {
        final GraphEntity graphEntity = new GraphEntity();
        graphEntity.setNamespace(genericGraph.getNamespace());
        graphEntity.setProperties(convertToPropertyEntities(genericGraph.getProperties()));

        // Map Vertices
        long start = System.currentTimeMillis();
        System.out.println("\tConverting vertices");
        final List<VertexEntity> vertexEntities = genericGraph.getVertices().stream().map(genericVertex -> {
            final VertexEntity vertexEntity = new VertexEntity();
            final List<PropertyEntity> vertexProperties = convertToPropertyEntities(genericVertex.getProperties());
            vertexEntity.setProperties(vertexProperties);
            return vertexEntity;
        }).collect(Collectors.toList());
        graphEntity.setVertices(vertexEntities);
        System.out.println("\tDONE. Took " + (System.currentTimeMillis() - start) + "ms");

        // Map Edges
        start = System.currentTimeMillis();
        System.out.println("\tConverting edges");
        final List<EdgeEntity> edgeEntities = genericGraph.getEdges().stream().map(genericEdge -> {
            final EdgeEntity edgeEntity = new EdgeEntity();
            edgeEntity.setSource(graphEntity.getVertexByVertexId(genericEdge.getSource().getId()));
            edgeEntity.setTarget(graphEntity.getVertexByVertexId(genericEdge.getTarget().getId()));
            final List<PropertyEntity> edgeProperties = convertToPropertyEntities(genericEdge.getProperties());
            edgeEntity.setProperties(edgeProperties);
            return edgeEntity;
        }).collect(Collectors.toList());
        graphEntity.setEdges(edgeEntities);
        System.out.println("\tDONE. Took " + (System.currentTimeMillis() - start) + "ms");
        return graphEntity;
    }

    private List<PropertyEntity> convertToPropertyEntities(Map<String, Object> properties) {
        Objects.requireNonNull(properties);
        final List<PropertyEntity> propertyEntities = new ArrayList<>();
        for(Map.Entry<String, Object> property : properties.entrySet()) {
            final Object value = property.getValue();
            if (value != null) {
                final Class<?> clazz = value.getClass();
                if (supportedClasses.contains(clazz) || clazz.isEnum()) {
                    final PropertyEntity propertyEntity = new PropertyEntity();
                    propertyEntity.setName(property.getKey());
                    propertyEntity.setType(clazz);
                    propertyEntity.setValue(clazz.isEnum() ? ((Enum) value).name() : value.toString());
                    propertyEntities.add(propertyEntity);
                } else {
                    throw new IllegalStateException("Entity contains properties which are neither a primitive nor an enum. Cannot persist.");
                }
            }
        }
        return propertyEntities;
    }

    private Object convert(final PropertyEntity propertyEntity) {
        final Converter<?> converter = propertyEntity.getType().isEnum()
                                        ? converterMap.get(Enum.class)
                                        : converterMap.get(propertyEntity.getType());
        final Object value = converter.toValue(propertyEntity.getType(), propertyEntity.getValue());
        return value;
    }

    private GraphInfo convert(GraphEntity graphEntity) {
        final DefaultGraphInfo graphInfo = new DefaultGraphInfo(graphEntity.getNamespace(), GenericVertex.class /* TODO MVR this is not correct */);
        graphInfo.withDescription(graphEntity.getDescription());
        graphInfo.withLabel(graphEntity.getLabel());
        return graphInfo;
    }
}
