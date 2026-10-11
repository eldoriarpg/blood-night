package de.eldoria.bloodnight.editor;

import org.mockbukkit.mockbukkit.MockBukkit;
import tools.jackson.core.JacksonException;
import de.eldoria.bloodnight.configuration.elements.ConfigurationScope;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.Drop;
import de.eldoria.bloodnight.mob.meta.Equipment;
import de.eldoria.bloodnight.mob.meta.Extension;
import de.eldoria.bloodnight.mob.meta.ExtensionType;
import de.eldoria.bloodnight.mob.meta.MobDrops;
import de.eldoria.bloodnight.mob.meta.ValueModifier;
import de.eldoria.bloodnight.mobs.MobRegistry;
import de.eldoria.bloodnight.nodes.registry.DefaultNodes;
import de.eldoria.bloodnight.nodes.registry.NodeRegistry;
import de.eldoria.eldoutilities.builder.ItemStackBuilder;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.IntNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class EditorPayloadTest {

    @BeforeAll
    static void beforeAll() {
        MockBukkit.mock();
    }

    @Test
    void asJson() throws JacksonException {
        ItemRegistry itemRegistry = new ItemRegistry();
        itemRegistry.register(ItemStackBuilder.of(Material.NETHERITE_SWORD)
                .withUnsafeEnchantment(Enchantment.UNBREAKING, 3)
                .withUnsafeEnchantment(Enchantment.SHARPNESS, 4)
                .withDisplayName("Awesome Sword")
                .withLore("The most awesome sword", "much wow")
                .withAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(new NamespacedKey("bloodnight", "damage"), 2, AttributeModifier.Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.ANY))
                .build());
        itemRegistry.register(ItemStackBuilder.of(Material.DIAMOND_CHESTPLATE).withDisplayName("Shiny Armor").build());

        var itemIds = new ArrayList<>(itemRegistry.registrations().keySet());

        MobRegistry mobs = new MobRegistry();

        Equipment equipment = new Equipment(null, itemIds.get(1), null, null, itemIds.get(0), null);
        Attributes attributes = new Attributes("Test Mob", EntityType.ZOMBIE, false, ValueModifier.DEFAULT, 2, ValueModifier.MULTIPLY, 3);

        Graph behaviour = Graph.builder()
                .node("0", NodeInstance.of("value.integer").config("value", IntNode.valueOf(5)))
                .node("1", NodeInstance.of("math.add").input("first", "0", "value").literal("second", IntNode.valueOf(10)))
                .node("2", NodeInstance.of("debug.print").input("value", "1", "result"))
                .node("3", NodeInstance.of("mob.tick").chain("next", "2"))
                .build();

        MobDrops mobDrops = new MobDrops(ConfigurationScope.MOB,
                new HashMap<>() {{
                    put(1, 90);
                    put(2, 50);
                    put(3, 10);
                }},
                List.of(ConfigurationScope.MOB, ConfigurationScope.WORLD),
                List.of(new Drop(itemIds.get(0), 10, 5),
                        new Drop(itemIds.get(1), 1, 100)),
                ConfigurationScope.MOB,
                ValueModifier.MULTIPLY,
                2);

        Extension extension = new Extension(ExtensionType.CARRIER, EntityType.SPIDER, new Equipment(null, null, null, null, null, null));
        CustomMob customMob = new CustomMob("test", equipment, attributes, behaviour, mobDrops, extension);
        mobs.add(customMob);

        NodeRegistry.register(DefaultNodes.defaultNodes());

        EditorPayload editorPayload = new EditorPayload(mobs, NodeRegistry.registrations(), new DataTypes(), itemRegistry, mobDrops);

        System.out.println(EditorPayload.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(editorPayload));
    }

    @AfterAll
    static void afterAll() {
        NodeRegistry.unregisterAll();
        MockBukkit.unmock();
    }
}
