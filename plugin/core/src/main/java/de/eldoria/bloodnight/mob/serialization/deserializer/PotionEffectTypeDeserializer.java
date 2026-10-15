/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.serialization.deserializer;

import org.bukkit.potion.PotionEffectType;

public class PotionEffectTypeDeserializer extends BaseAttributeDeserializer<PotionEffectType> {
    @Override
    protected String id(PotionEffectType value) {
        return Integer.toHexString(value.getKey().hashCode());
    }

    @Override
    protected PotionEffectType[] values() {
        return PotionEffectType.values();
    }
}
