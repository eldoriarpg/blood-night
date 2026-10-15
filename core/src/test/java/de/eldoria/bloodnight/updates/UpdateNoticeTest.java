/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.updates;

import de.eldoria.bloodnight.announce.Texts;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UpdateNoticeTest {
    private final List<String> console = new ArrayList<>();
    private ServerMock server;
    private Optional<String> newest = Optional.empty();
    private boolean enabled = true;
    private UpdateNotice notice;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        Texts.create(MockBukkit.createMockPlugin(), "[BN]", "en_US");
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                console.add(record.getMessage());
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        });
        notice = new UpdateNotice(current -> newest, "1.0.0", () -> enabled, logger);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private PlayerMock join(boolean admin) {
        PlayerMock player = server.addPlayer();
        player.setOp(admin);
        notice.onJoin(new PlayerJoinEvent(player, Component.empty()));
        return player;
    }

    @Test
    void aNewVersionIsToldOnceAndToAdminsOnJoin() {
        newest = Optional.of("1.1.0");
        notice.check();
        notice.check();

        assertEquals(1, console.size());
        PlayerMock admin = join(true);
        assertEquals(
                "[BN] BloodNight 1.1.0 is out. You run 1.0.0. Download it",
                PlainTextComponentSerializer.plainText().serialize(admin.nextComponentMessage()));
        assertNull(join(false).nextComponentMessage());
    }

    @Test
    void nothingIsToldWithoutANewVersionOrWhenSwitchedOff() {
        notice.check();
        enabled = false;
        newest = Optional.of("1.1.0");
        notice.check();

        assertEquals(List.of(), console);
        assertNull(join(true).nextComponentMessage());
    }
}
