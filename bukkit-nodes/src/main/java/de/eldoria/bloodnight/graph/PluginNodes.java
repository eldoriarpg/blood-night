/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.bloodnight.graph.runtime.Integrations;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.definition.StatementDefinition;
import de.eldoria.graph.platform.NodeRule;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * Nodes for other plugins: PlaceholderAPI, Vault and MythicMobs. They are always there, so a graph using them can be
 * opened and compiled anywhere. On a server without the plugin such a node does nothing (see {@link Integrations})
 * and the graph shows a warning that names the missing plugin.
 */
public final class PluginNodes {
    private static final String NEXT = "next";
    private static final String RESULT = "result";
    private static final String PLAYER = "player";

    public static final ExpressionDefinition PLACEHOLDERS = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.PLACEHOLDER_PARSE, NodeKind.PURE)
                    .category("plugins")
                    .name("Fill In Placeholders")
                    .description("Fills PlaceholderAPI placeholders such as %player_name% into a text. Needs"
                            + " PlaceholderAPI")
                    .input("text", String.class)
                    .optionalInput(PLAYER, Player.class)
                    .output(RESULT, String.class)
                    .describe("text", "The text with placeholders")
                    .describe(PLAYER, "The player the placeholders are about; leave it empty when there is none")
                    .describe(RESULT, "The text with the placeholders filled in")
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "$T.placeholders($L, $L)",
                    Integrations.class,
                    ctx.input("text"),
                    ctx.connected(PLAYER) ? ctx.input(PLAYER) : CodeBlock.of("null")));

    public static final StatementDefinition DEPOSIT =
            money(BloodNightNodes.ECONOMY_DEPOSIT, "Give Money", "Gives a player money through Vault", "deposit");

    public static final StatementDefinition WITHDRAW = money(
            BloodNightNodes.ECONOMY_WITHDRAW, "Take Money", "Takes money from a player through Vault", "withdraw");

    public static final ExpressionDefinition BALANCE = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.ECONOMY_BALANCE, NodeKind.PURE)
                    .category("plugins")
                    .name("Balance")
                    .description("How much money a player has in Vault's economy. Needs Vault")
                    .input(PLAYER, Player.class)
                    .output(RESULT, double.class)
                    .describe(PLAYER, "The player")
                    .describe(RESULT, "Their money; 0 without Vault")
                    .build(),
            (port, ctx) -> CodeBlock.of("$T.balance($L)", Integrations.class, ctx.input(PLAYER)));

    public static final StatementDefinition CAST_SKILL = new StatementDefinition(
            NodeDescriptor.builder(BloodNightNodes.MYTHIC_CAST_SKILL, NodeKind.ACTION)
                    .category("plugins")
                    .name("Cast Mythic Skill")
                    .description("Lets an entity cast a MythicMobs skill by its name. Needs MythicMobs")
                    .input("caster", "Caster", PortType.of(Entity.class), false)
                    .input("skill", String.class)
                    .execution(NEXT)
                    .describe("caster", "Who casts it, usually the mob itself")
                    .describe("skill", "The skill's name in MythicMobs")
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement("$T.castSkill($L, $L)", Integrations.class, ctx.input("caster"), ctx.input("skill"))
                    .add(ctx.chain(NEXT))
                    .build());

    /**
     * Which plugin each node needs, by node id.
     */
    static final Map<String, String> NEEDS = Map.of(
            BloodNightNodes.PLACEHOLDER_PARSE, "PlaceholderAPI",
            BloodNightNodes.ECONOMY_DEPOSIT, "Vault",
            BloodNightNodes.ECONOMY_WITHDRAW, "Vault",
            BloodNightNodes.ECONOMY_BALANCE, "Vault",
            BloodNightNodes.MYTHIC_CAST_SKILL, "MythicMobs");

    private PluginNodes() {}

    /**
     * @return the plugins these nodes are for
     */
    public static Set<String> plugins() {
        return new TreeSet<>(NEEDS.values());
    }

    /**
     * @return every node of this kind
     */
    public static List<NodeDefinition> all() {
        return List.of(PLACEHOLDERS, DEPOSIT, WITHDRAW, BALANCE, CAST_SKILL);
    }

    /**
     * @param present whether a plugin, by name, runs on the server
     * @return a rule warning that a node does nothing because its plugin is missing
     */
    static NodeRule needs(String plugin, Predicate<String> present) {
        return (node, report) -> {
            if (!present.test(plugin)) {
                report.warning(null, "Needs %s, which is not installed. This node does nothing.".formatted(plugin));
            }
        };
    }

    private static StatementDefinition money(String id, String name, String description, String method) {
        return new StatementDefinition(
                NodeDescriptor.builder(id, NodeKind.ACTION)
                        .category("plugins")
                        .name(name)
                        .description(description + ". Needs Vault and an economy plugin")
                        .input(PLAYER, Player.class)
                        .input("amount", double.class)
                        .execution(NEXT)
                        .describe(PLAYER, "The player")
                        .describe("amount", "How much money")
                        .build(),
                ctx -> CodeBlock.builder()
                        .addStatement(
                                "$T.$L($L, $L, $L)",
                                Integrations.class,
                                method,
                                ctx.context(),
                                ctx.input(PLAYER),
                                ctx.input("amount"))
                        .add(ctx.chain(NEXT))
                        .build());
    }
}
