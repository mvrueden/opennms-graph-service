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

package org.opennms.poc.graph.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.poc.graph.api.Vertex;

import com.google.common.collect.ImmutableMap;

public class DefaultVertex implements Vertex {

    private final Map<String, Object> properties = new HashMap<>();

    public DefaultVertex(String namespace, int id) {
        this(ImmutableMap.of("namespace", namespace, "id", "" + id));
    }

    public DefaultVertex(Map<String, Object> properties) {
        this.properties.putAll(Objects.requireNonNull(properties));
    }

    @Override
    public String getNamespace() {
        return getProperty("namespace");
    }

    @Override
    public String getId() {
        return getProperty("id");
    }

    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    public <T> T getProperty(String key, T defaultValue) {
        return (T) properties.getOrDefault(key, defaultValue);
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }
}
