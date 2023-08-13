package de.eldoria.bloodnight.editor;

import be.seeseemelk.mockbukkit.MockBukkit;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import de.eldoria.bloodnight.nodes.action.impl.PrintNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.registry.DefaultNodes;
import de.eldoria.bloodnight.nodes.registry.NodeRegistry;
import de.eldoria.bloodnight.nodes.transform.impl.math.AddNode;
import de.eldoria.bloodnight.nodes.trigger.impl.TickNode;
import de.eldoria.bloodnight.nodes.value.impl.IntegerNode;
import de.eldoria.eldoutilities.builder.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

class EditorPayloadTest {

    @BeforeAll
    static void beforeAll() {
        MockBukkit.mock();
    }

    @Test
    void asJson() throws JsonProcessingException {
        ItemRegistry itemRegistry = new ItemRegistry();
        itemRegistry.register(ItemStackBuilder.of(Material.NETHERITE_SWORD)
                .withUnsafeEnchantment(Enchantment.DURABILITY, 3)
                .withUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4)
                .withDisplayName("Awesome Sword")
                .withLore("The most awesome sword", "much wow")
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier("Damage", 2, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
                .build());
        itemRegistry.register(ItemStackBuilder.of(Material.DIAMOND_CHESTPLATE).withDisplayName("Shiny Armor").build());

        var itemIds = new ArrayList<String>(itemRegistry.registrations().keySet());

        MobRegistry mobs = new MobRegistry();

        Equipment equipment = new Equipment(null, itemIds.get(1), null, null, itemIds.get(0), null);
        Attributes attributes = new Attributes("Test Mob", EntityType.ZOMBIE, false, ValueModifier.DEFAULT, 2, ValueModifier.MULTIPLY, 3);

        NodeContainer container = new NodeContainer();
        container.add(0, new IntegerNode(5));
        container.add(1, new IntegerNode(10));
        container.add(2, new AddNode())
                .input()
                .connect(AddNode.Input.FIRST, Edge.to(0, IntegerNode.Output.VALUE))
                .connect(AddNode.Input.SECOND, Edge.to(1, IntegerNode.Output.VALUE));
        container.add(3, new PrintNode())
                .input(in -> in.connect(PrintNode.Input.VALUE, Edge.to(2, AddNode.Output.RESULT)));
        container.add(4, new TickNode())
                .chain(TickNode.Executions.NEXT, 3);

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

        Extension extension = new Extension(ExtensionType.CARRIER, EntityType.SPIDER);
        CustomMob customMob = new CustomMob("test", equipment, attributes, container, mobDrops, extension);
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
