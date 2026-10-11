/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.service;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Entry point: reads the settings from the environment ({@link ServiceConfig}), migrates the database, starts the API
 * and deletes expired sessions every ten minutes.
 */
public final class EditorService {
    private static final Logger log = LoggerFactory.getLogger(EditorService.class);

    private EditorService() {}

    public static void main(String[] args) throws SQLException, IOException {
        ServiceConfig config = ServiceConfig.of(System.getenv());
        Database.open(config.database());
        SessionRepository sessions = new SessionRepository(config.sessionLifetime());
        Javalin app = new EditorApp(config, sessions, new CatalogRepository(), Clock.systemUTC())
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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            sweeper.shutdownNow();
            app.stop();
        }));
    }
}
