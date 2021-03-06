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

package org.opennms.poc.graph.api.search;

import java.util.Objects;

/**
 * A {@link SearchSuggestion} is provided to the user and was created from a (partial) search query.
 * A suggestion is a very abstract and may not directly related to a vertex. For example a category.
 * The main idea is, that a {@link SearchSuggestion} represents an item a user can select, which
 * afterwards is resolved to a List of vertices/edges.
 */
public class SearchSuggestion {

    // The context of the suggestion, e.g. category, attribute, node, etc. to allow for a more fine grain suggestion
    // This may be achieved later by sub-classing
    private String context;
    // The user-friendly label for the suggestion, which is shown to the user
    private String label;
    // The provider from which the suggestion is. This is required to resolve later on
    // This ensures that the originating SearchProvider can actually resolve it
    private String provider;

    public SearchSuggestion(Class providerClass, String context, String label) {
        this(Objects.requireNonNull(providerClass).getSimpleName(), context, label);
    }

    public SearchSuggestion(String providerId, String context, String label) {
        setLabel(Objects.requireNonNull(label));
        setContext(Objects.requireNonNull(context));
        setProvider(Objects.requireNonNull(providerId));
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
