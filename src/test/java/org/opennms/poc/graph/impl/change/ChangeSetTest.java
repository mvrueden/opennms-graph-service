package org.opennms.poc.graph.impl.change;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.opennms.poc.graph.api.simple.SimpleEdge;
import org.opennms.poc.graph.api.simple.SimpleGraph;
import org.opennms.poc.graph.api.simple.SimpleVertex;

public class ChangeSetTest {

    private static final String NAMESPACE = "simple";

    private ChangeSet<SimpleVertex, SimpleEdge<SimpleVertex>> changeSet;

    @Before
    public void setUp() {
        changeSet = new ChangeSet<>(NAMESPACE, new Date());
    }

    @Test
    public void verifyNoOldGraph() {
        final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> newGraph = new SimpleGraph<>(NAMESPACE);
        changeSet.detectChanges(null, newGraph);

        assertEquals(Boolean.FALSE, changeSet.hasGraphInfoChanged());
        assertEquals(Boolean.TRUE, changeSet.getVerticesAdded().isEmpty());
        assertEquals(Boolean.TRUE, changeSet.getVerticesRemoved().isEmpty());
        assertEquals(Boolean.TRUE, changeSet.getVerticesUpdated().isEmpty());
        assertEquals(Boolean.TRUE, changeSet.getEdgesAdded().isEmpty());
        assertEquals(Boolean.TRUE, changeSet.getEdgesRemoved().isEmpty());
        assertEquals(Boolean.TRUE, changeSet.getEdgesUpdated().isEmpty());
    }

    @Test
    public void verifyUpdate() {
        final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> oldGraph = new SimpleGraph<>(NAMESPACE);
        oldGraph.addVertex(new SimpleVertex(NAMESPACE, "1"));
        oldGraph.addVertex(new SimpleVertex(NAMESPACE, "2"));
        oldGraph.addVertex(new SimpleVertex(NAMESPACE, "3"));

        final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> newGraph = new SimpleGraph<>(NAMESPACE);
        newGraph.addVertex(new SimpleVertex(NAMESPACE, "3"));
        newGraph.addVertex(new SimpleVertex(NAMESPACE, "4"));
        newGraph.getVertex("3").setLabel("Three");

        changeSet.detectChanges(oldGraph, newGraph);

        assertEquals(Boolean.FALSE, changeSet.hasGraphInfoChanged());
        assertEquals(Boolean.FALSE, changeSet.getVerticesAdded().isEmpty());
        assertEquals(Boolean.FALSE, changeSet.getVerticesRemoved().isEmpty());
        assertEquals(Boolean.FALSE, changeSet.getVerticesUpdated().isEmpty());
        assertEquals(Boolean.TRUE, changeSet.getEdgesAdded().isEmpty());
        assertEquals(Boolean.TRUE, changeSet.getEdgesRemoved().isEmpty());
        assertEquals(Boolean.TRUE, changeSet.getEdgesUpdated().isEmpty());

        assertEquals("4", changeSet.getVerticesAdded().get(0).getId());
        assertEquals("1", changeSet.getVerticesRemoved().get(0).getId());
        assertEquals("2", changeSet.getVerticesRemoved().get(1).getId());
        assertEquals("3", changeSet.getVerticesUpdated().get(0).getId());
    }

    @Test
    public void verifyNamespaceCannotChange() {
        final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> oldGraph = new SimpleGraph<>(NAMESPACE);
        final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> newGraph = new SimpleGraph<>(NAMESPACE + ".opennms");
        try {
            changeSet.detectChanges(oldGraph, newGraph);
            fail("Expected an exception to be thrown, but succeeded. Bailing");
        } catch (IllegalStateException ex) {
            // expected, as namespace changes are not supported
        }
    }

}