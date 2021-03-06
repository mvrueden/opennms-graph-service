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

package org.opennms.poc.graph.api.info;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultGraphContainerInfo implements GraphContainerInfo {

    private final String id;
    private List<GraphInfo> graphInfos = new ArrayList<>();
    private String description;
    private String label;

    public DefaultGraphContainerInfo(String id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public List<String> getNamespaces() {
        return graphInfos.stream().map(gi -> gi.getNamespace()).collect(Collectors.toList());
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public GraphInfo getGraphInfo(String namespace) {
        Objects.requireNonNull(namespace);
        return graphInfos.stream().filter(gi -> namespace.equalsIgnoreCase(gi.getNamespace())).findFirst().orElse(null);
    }

    @Override
    public void addGraphInfo(GraphInfo graphInfo) {
        graphInfos.add(graphInfo);
    }

    @Override
    public GraphInfo getPrimaryGraphInfo() {
        return graphInfos.get(0);
    }

    @Override
    public String getId() {
        return id;
    }

    public List<GraphInfo> getGraphInfos() {
        return graphInfos;
    }

    public void setGraphInfos(List<GraphInfo> graphInfos) {
        this.graphInfos = graphInfos;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
