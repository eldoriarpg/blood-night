package de.eldoria.bloodnight.editor;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.serializastion.DataTypeSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class DataTypes {
    List<EntityType> entityTypes = List.of(EntityType.values());
    @JsonSerialize(using = DataTypeSerializer.class)
    List<DataType> nodeDataTypes = List.of(DataType.values());
    List<PotionEffectType> potionEffectTypes = List.of(PotionEffectType.values());
    List<EntityDamageEvent.DamageCause> damageCauses = List.of(EntityDamageEvent.DamageCause.values());
    List<Material> blockType = Arrays.stream(Material.values()).filter(Material::isBlock).toList();
    List<Material> itemType = Arrays.stream(Material.values()).filter(Material::isItem).toList();
    List<Enchantment> enchantments = List.of(Enchantment.values());
}
