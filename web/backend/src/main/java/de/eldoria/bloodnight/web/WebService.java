/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Entry point of BloodNight's web backend, for now the editor service: reads the settings from the environment ({@link ServiceConfig}), migrates the database, starts the API
 * deletes expired sessions every ten minutes and releases the sections of absent participants every half minute.
 */
public final class WebService {
    private static final Logger log = LoggerFactory.getLogger(WebService.class);

    private WebService() {}

    public static void main(String[] args) throws SQLException, IOException {
        ServiceConfig config = ServiceConfig.of(System.getenv());
        Database.open(config.database());
        SessionRepository sessions = new SessionRepository(config.sessionLifetime());
        Rooms rooms = new Rooms(Clock.systemUTC(), Duration.ofMinutes(2));
        Javalin app = new EditorApp(
                        config,
                        sessions,
                        new ParticipantRepository(),
                        new CatalogRepository(),
                        rooms,
                        ItemAssets.mojang(config.assetCache()),
                        Clock.systemUTC())
                .create()
                .start(config.port());
        ScheduledExecutorService sweeper = Executors.newSingleThreadScheduledExecutor();
        sweeper.scheduleAtFixedRate(
                () -> {
                    try {
                        int deleted = sessions.deleteExpired();
                        if (deleted > 0) log.info("Deleted {} expired sessions", deleted);
                    } catch (RuntimeException e) {
                        log.error("Deleting expired sessions failed", e);
                    }
                },
                1,
                10,
                TimeUnit.MINUTES);
        sweeper.scheduleAtFixedRate(rooms::expireAll, 30, 30, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            sweeper.shutdownNow();
            app.stop();
        }));
    }
}
