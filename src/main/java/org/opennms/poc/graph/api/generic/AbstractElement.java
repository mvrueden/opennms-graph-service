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

package org.opennms.poc.graph.api.generic;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public class AbstractElement {

    protected final Map<String, Object> properties = new HashMap<>();

    protected final Map<String, Object> computedProperties = new HashMap<>();

    public AbstractElement() {

    }

    public AbstractElement(String namespace, String id) {
        setNamespace(namespace);
        setId(id);
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    public <T> T getProperty(String key, T defaultValue) {
        return (T) properties.getOrDefault(key, defaultValue);
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties.clear();
        this.properties.putAll(properties);
    }

    public void setComputedProperty(String key, Object value) {
        computedProperties.put(key, value);
    }

    public Map<String, Object> getComputedProperties() {
        return computedProperties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getNamespace() {
        return getProperty(GenericProperties.NAMESPACE);
    }

    public void setNamespace(String namespace) {
        setProperty(GenericProperties.NAMESPACE, namespace);
    }

    public String getId() {
        return getProperty(GenericProperties.ID);
    }

    public void setId(String id) {
        setProperty(GenericProperties.ID, id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractElement that = (AbstractElement) o;
        return Objects.equals(properties, that.properties) &&
                Objects.equals(computedProperties, that.computedProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(properties, computedProperties);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("properties", properties)
                .add("computedProperties", computedProperties)
                .toString();
    }
}
