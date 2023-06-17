package de.eldoria.bloodnight.mob.serialization.serializer;

import org.bukkit.potion.PotionEffectType;

public class PotionEffectTypeSerializer extends BaseAttributeSerializer<PotionEffectType> {
    @Override
    public String id(PotionEffectType value) {
        return Integer.toHexString(value.getKey().hashCode());
    }

    @Override
    public String name(PotionEffectType value) {
        return value.getName();
    }
}
