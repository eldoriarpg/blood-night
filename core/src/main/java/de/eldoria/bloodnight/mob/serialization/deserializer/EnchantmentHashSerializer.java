/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.serialization.deserializer;

import org.bukkit.enchantments.Enchantment;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class EnchantmentHashSerializer extends ValueSerializer<Enchantment> {
    @Override
    public void serialize(Enchantment value, JsonGenerator gen, SerializationContext serializers) {
        gen.writeName(Integer.toHexString(value.getKey().hashCode()));
    }
}
