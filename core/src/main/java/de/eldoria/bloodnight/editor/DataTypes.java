package de.eldoria.bloodnight.editor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.serialization.DataTypeSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class DataTypes {
    @JsonProperty
    List<EntityType> entityTypes = List.of(EntityType.values());
    @JsonProperty
    @JsonSerialize(contentUsing = DataTypeSerializer.class)
    List<DataType> nodeDataTypes = List.of(DataType.values());
    @JsonProperty
    List<PotionEffectType> potionEffectTypes = List.of(PotionEffectType.values());
    @JsonProperty
    List<EntityDamageEvent.DamageCause> damageCauses = List.of(EntityDamageEvent.DamageCause.values());
    @JsonProperty
    List<Material> blockType = Arrays.stream(Material.values()).filter(Material::isBlock).toList();
    @JsonProperty
    List<Material> itemType = Arrays.stream(Material.values()).filter(Material::isItem).toList();
    @JsonProperty
    List<Enchantment> enchantments = List.of(Enchantment.values());
}
