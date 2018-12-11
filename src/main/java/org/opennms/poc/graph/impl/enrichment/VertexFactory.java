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


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.enrichment.Enriched;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

@Service
public class VertexFactory {

    @Autowired
    private EnrichmentProcessor processor;

    public <T extends Vertex> T createVertex(final Class<T> vertexTypeToCreate, Object... constructorArguments) throws IntrospectionException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        final BeanInfo beanInfo = Introspector.getBeanInfo(vertexTypeToCreate);
        final Map<String, PropertyDescriptor> pdMap = Arrays.stream(beanInfo.getPropertyDescriptors())
                .filter(pd -> pd.getReadMethod() != null)
                .filter(pd -> {
                    try {
                        Field field = vertexTypeToCreate.getDeclaredField(pd.getName());
                        return !Modifier.isStatic(field.getModifiers()) && field.getAnnotation(Enriched.class) != null;
                    } catch (NoSuchFieldException e) {
                        return false;
                    }
                }).collect(Collectors.toMap(pd -> pd.getReadMethod().getName(), pd -> pd));

        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(vertexTypeToCreate);
        factory.setFilter(method -> pdMap.get(method.getName()) != null);

        MethodHandler handler = new MethodHandler() {
            @Override
            public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                if (!(self instanceof Vertex)) {
                    throw new IllegalStateException("Object (self) must be of type Vertex. Bailing");
                }
                final PropertyDescriptor propertyDescriptor = pdMap.get(thisMethod.getName());
                final Field field = vertexTypeToCreate.getDeclaredField(propertyDescriptor.getName());
                field.setAccessible(true);
                if (field.get(self) == null) { // TODO MVR already enriched, but null?
                    processor.enrich((Vertex) self, field);
                }
                try {
                    return proceed.invoke(self, args);
                } catch (InvocationTargetException ex) {
                    throw ex.getTargetException();
                }
            }
        };

        final Class<?> [] types = new Class<?>[constructorArguments.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = constructorArguments[i].getClass(); // TODO MVR null values?!
        }

        final Object o = factory.create(types, constructorArguments, handler);
        return (T) o;
    }
}
