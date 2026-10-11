/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.serialization.serializer;

import de.eldoria.bloodnight.mob.serialization.wrapper.ItemStackWrapper;
import org.bukkit.inventory.ItemStack;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class SimpleItemStackSerializer extends ValueSerializer<ItemStack> {
    @Override
    public void serialize(ItemStack value, JsonGenerator gen, SerializationContext serializers) {
        gen.writePOJO(ItemStackWrapper.of(value));
    }
}
