/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.serialization.serializer;

import de.eldoria.bloodnight.mob.serialization.NamedAttribute;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public abstract class BaseAttributeSerializer<T> extends ValueSerializer<T> {
    @Override
    public void serialize(T value, JsonGenerator gen, SerializationContext serializers) {
        gen.writePOJO(new NamedAttribute(String.valueOf(id(value)), name(value)));
    }

    public abstract String id(T value);

    public abstract String name(T value);
}
