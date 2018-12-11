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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.enrichment.Enriched;
import org.opennms.poc.graph.api.enrichment.Enrichment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

// TODO MVR scope protptype, singleton?!

@Service
public class EnrichmentProcessor {

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    public void enrich(Vertex vertex, Field field) {
        // Verify value is != null
        try {
            field.setAccessible(true);
            if (field.get(vertex) == null) {
                final Enriched enriched = field.getAnnotation(Enriched.class);
                if (enriched != null) {
                    final Enrichment enrichment = beanFactory.getBean(enriched.enrichment());
                    Object value = enrichment.compute(vertex);
                    if (value != null) { // TODO MVR verify type compatibility
                        field.set(vertex, value);
                    }
                }
            }
        } catch (IllegalAccessException ex) {
            // TODO MVR
        }
    }

    public void enrich(EnrichedVertexExample vertex) {
        for (Field field : vertex.getClass().getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && field.getAnnotation(Enriched.class) != null) {
                enrich(vertex, field);
            }
        }
    }
}
