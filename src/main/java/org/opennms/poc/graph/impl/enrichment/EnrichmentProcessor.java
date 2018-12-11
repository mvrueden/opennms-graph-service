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

import org.opennms.poc.graph.api.enrichment.Computed;
import org.opennms.poc.graph.api.enrichment.Compution;
import org.opennms.poc.graph.api.enrichment.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

// TODO MVR scope protptype, singleton?!

@Service
public class EnrichmentProcessor {

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    public void enrich(ComputedVertexExample vertex) {
        for (Field f : vertex.getClass().getDeclaredFields()) {
            // Skip static fields
            if (!Modifier.isStatic(f.getModifiers())
                    && f.getAnnotation(Constant.class) != null
                    || f.getAnnotation(Computed.class) != null) {
                // Verify value is != null
                try {
                    f.setAccessible(true);
                    if (f.get(vertex) == null) {
                        final Constant constant = f.getAnnotation(Constant.class);
                        if (constant != null) {
                            if (constant.value() == null) continue;
                            if (f.getType() == Long.class) {
                                f.set(vertex, Long.valueOf(constant.value()));
                            } else if (f.getType() == Short.class) {
                                f.set(vertex, Short.valueOf(constant.value()));
                            }
                            // TODO MVR implement more
                            else {
                                f.set(vertex, constant.value());
                            }
                        }
                        final Computed computed = f.getAnnotation(Computed.class);
                        if (computed != null) {
                            final Compution compution = beanFactory.getBean(computed.compution());
                            Object value = compution.compute(vertex);
                            if (value != null) { // TODO MVR verify type compatibility
                                f.set(vertex, value);
                            }
                        }
                    }
                } catch (IllegalAccessException ex) {
                    // TODO MVR
                }
            }
        }
    }
}
