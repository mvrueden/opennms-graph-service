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

package org.opennms.poc.graph.impl.enrichment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.enrichment.Enrich;
import org.opennms.poc.graph.api.enrichment.EnrichmentProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

// TODO MVR scope protptype, singleton?!

/**
 * Responsible for enriching Vertices. Maybe later on it can also enrich edges or graph properties.
 */
@Service
public class EnrichmentService {

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    public void enrich(List<Vertex> vertices, EnrichedField field) {
        if (field.isEnrichable()) {
            // Only enrich if field's value is null
            final List<Vertex> enrichableVertices = vertices.stream().filter(v -> field.isNull(v)).collect(Collectors.toList());
            final Enrich enrich = field.getEnrichedAnnotation();
            final EnrichmentProcessor enrichmentProcessor = beanFactory.getBean(enrich.processor());
            final Map<Vertex, Object> valueMap = enrichmentProcessor.enrich(enrichableVertices);
            enrichableVertices.stream().forEach(v -> {
                if (valueMap.get(v) != null) {
                    field.setValue(v, valueMap.get(v)); // TODO MVR verify type compatibility
                }
            });
        }
    }

    public void enrich(Vertex vertex, EnrichedField field) {
        enrich(Lists.newArrayList(vertex), field);
    }

    public List<EnrichedField> enrich(Vertex vertex) {
        List<EnrichedField> enrichedFields = getEnrichableFields(vertex);
        enrichedFields.forEach(f -> enrich(vertex, f));
        return enrichedFields;
    }

    // In some cases we have to enrich more than one vertex.
    // In order to this more performant, we group them by enriched type to allow enriching the type at once, e.g.
    // fetch all OnmsNodes from the database instead of one by one)
    public void enrich(List<Vertex> vertices) {
        Objects.requireNonNull(vertices);

        // The types may differ and have different enriched fields,
        // therefore group by type before actually performing the enrichment
        final Set<Class<? extends Vertex>> vertexTypes = vertices.stream().map(vertex -> vertex.getClass()).collect(Collectors.toSet());
        final Map<Class<? extends Vertex>, List<Vertex>> vertexTypeMap = new HashMap<>();
        for (Vertex eachVertex : vertices) {
            vertexTypeMap.putIfAbsent(eachVertex.getClass(), new ArrayList<>());
            vertexTypeMap.get(eachVertex.getClass()).add(eachVertex);
        }
        for (Class<? extends Vertex> eachVertexType : vertexTypes) {
            final List<EnrichedField> enrichableFields = Arrays.stream(extractRealClass(eachVertexType).getDeclaredFields())
                    .map(field -> new EnrichedField(field))
                    .filter(field -> field.isEnrichable())
                    .collect(Collectors.toList());
            for (EnrichedField eachFieldToEnrich : enrichableFields) {
                enrich(vertexTypeMap.get(eachVertexType), eachFieldToEnrich);
            }
        }
    }

    public List<EnrichedField> getEnrichableFields(Vertex vertex) {
        return Arrays.stream(extractRealClass(vertex.getClass()).getDeclaredFields())
                .map(field -> new EnrichedField(field))
                .filter(field -> field.isEnrichable())
                .collect(Collectors.toList());
    }

    /**
     * The class may be proxied and therefore have a suffix "_$$_jvstXXX_XX".
     * In order to have reflections work accordingly, we have to fiddle out the real class to operate reflection calls on
     *
     * @param theClass The may be proxied class
     * @return The real not proxied class
     */
    private static Class<?> extractRealClass(Class theClass) {
        Objects.requireNonNull(theClass);
        final String excludePattern = "_$$_jvst";
        final String className = theClass.getName();
        if (className.contains(excludePattern)) {
            String realClassName = className.substring(0, className.indexOf(excludePattern));
            try {
                return Class.forName(realClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        // no proxy, return as is
        return theClass;
    }
}
