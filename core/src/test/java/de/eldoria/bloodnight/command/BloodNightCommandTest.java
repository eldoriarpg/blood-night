/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import de.eldoria.bloodnight.BloodNight;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BloodNightCommandTest {
    private ServerMock server;
    private Plugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
    }

    @AfterEach
    void tearDown() {
        MobIds.clear();
        MockBukkit.unmock();
    }

    private static CommandSourceStack source(CommandSender sender) {
        return (CommandSourceStack) Proxy.newProxyInstance(
                CommandSourceStack.class.getClassLoader(),
                new Class<?>[] {CommandSourceStack.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getSender" -> sender;
                    case "getExecutor" -> null;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    @SuppressWarnings("DataFlowIssue")
    private static CommandNode<CommandSourceStack> tree() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        dispatcher.getRoot().addChild(BloodNightCommandBrigadier.create((BloodNight) null));
        return dispatcher.getRoot().getChild("bloodnight");
    }

    @Test
    void mobIdsAreSuggestedByWhatIsTyped() {
        MobIds.use(() -> List.of("speed_zombie", "armored_zombie", "speed_creeper"));

        List<String> suggested =
                MobIds.suggest(null, new SuggestionsBuilder("bloodnight spawn spe", 17)).join().getList().stream()
                        .map(Suggestion::getText)
                        .toList();

        assertEquals(List.of("speed_creeper", "speed_zombie"), suggested);
    }

    @Test
    void eachSubcommandNeedsItsOwnPermission() {
        List<String> subcommands = List.of("reload", "spawn", "problems", "source", "debug", "export");
        CommandNode<CommandSourceStack> root = tree();
        assertFalse(root.canUse(source(server.addPlayer())));

        for (String granted : subcommands) {
            PlayerMock player = server.addPlayer();
            player.addAttachment(plugin, "bloodnight.command." + granted, true);
            CommandSourceStack source = source(player);

            assertTrue(root.canUse(source), granted);
            for (String subcommand : subcommands) {
                assertEquals(
                        subcommand.equals(granted),
                        root.getChild(subcommand).canUse(source),
                        granted + " grants " + subcommand);
            }
        }
    }
}
