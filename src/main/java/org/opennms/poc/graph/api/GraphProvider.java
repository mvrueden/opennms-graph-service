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

package org.opennms.poc.graph.api;

import org.opennms.poc.graph.api.info.GraphInfo;

// TODO MVR the provider must provide information such as namespace, label, descriptin, etc. even if the graph itself is not loaded yet.
// TODO MVR the graph provider should probably return multiple graphs (e.g. graphml)

/**
 * @author mvrueden
 * @param <V>
 * @param <E>
 */
public interface GraphProvider<V extends Vertex, E extends Edge<V>> {

    /**
     * The provider may need to inform about graph changes.
     * Whith this method the {@link GraphNotificationService} is passed to the provider.
     * @param notificationService
     */
    void setNotificationService(GraphNotificationService notificationService);

    /**
     * Loads the graph, this {@link GraphProvider} handles.
     * Loading may be performed very quickly, but also may take some time.
     *
     * @return
     */
    Graph<V, E> loadGraph();

    /**
     * The {@link GraphInfo} should be used to provide details of the graph's nature, e.g. the namespace, label or description
     * A {@link Graph} should also embed this information. The difference is, that the info should always be available,
     * even if the graph is not yet loaded, and should also never change during the provider's live time, whereas the
     * graph itself may change (e.g. different vertices/edges and properties (besides the ones defining the info)).
     * @return
     */
    GraphInfo getGraphInfo();
}
