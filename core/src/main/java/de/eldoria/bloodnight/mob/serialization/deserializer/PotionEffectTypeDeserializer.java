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
