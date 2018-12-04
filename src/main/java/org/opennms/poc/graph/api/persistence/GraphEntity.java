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

package org.opennms.poc.graph.api.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Where;
import org.opennms.poc.graph.api.generic.GenericProperties;

@Entity
@DiscriminatorValue("graph")
public class GraphEntity extends AbstractGraphElementEntity {

    @Column(name = "namespace", unique = true)
    private String namespace;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "graph_elements_relations",
            joinColumns = { @JoinColumn(name = "graph_id", referencedColumnName = "id") },
            inverseJoinColumns = { @JoinColumn(name="element_id", referencedColumnName = "id") }
    )
    @Where(clause="TYPE='vertex'")
    private List<VertexEntity> vertices = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "graph_elements_relations",
            joinColumns = { @JoinColumn(name = "graph_id", referencedColumnName = "id") },
            inverseJoinColumns = { @JoinColumn(name="element_id", referencedColumnName = "id") }
    )
    @Where(clause="TYPE='edge'")
    private List<EdgeEntity> edges = new ArrayList<>();

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<EdgeEntity> getEdges() {
        return edges;
    }

    public void setEdges(List<EdgeEntity> edges) {
        this.edges = edges;
    }

    public List<VertexEntity> getVertices() {
        return vertices;
    }

    public void setVertices(List<VertexEntity> vertices) {
        this.vertices = vertices;
    }

    public VertexEntity getVertexByVertexId(String id) {
        Objects.requireNonNull(id);
        return vertices.stream()
                .filter(v -> v.getProperty(GenericProperties.ID).getValue().equalsIgnoreCase(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException());
    }
}
