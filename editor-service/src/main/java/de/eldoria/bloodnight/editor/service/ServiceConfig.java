/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The service's settings, read from the environment.
 *
 * @param port               HTTP port ({@code PORT}, 8080)
 * @param editorUrl          where a session is edited, {@code {id}} replaced by its id ({@code EDITOR_URL})
 * @param allowedOrigins     origins the editor runs on, comma separated ({@code ALLOWED_ORIGINS}); empty allows any
 * @param sessionLifetime    how long a session lives without being used ({@code SESSION_HOURS}, 24)
 * @param sessionsPerHour    sessions one address may open per hour ({@code SESSIONS_PER_HOUR}, 20)
 * @param maxPayloadBytes    largest request body ({@code MAX_PAYLOAD_MB}, 10)
 * @param database           the database connection
 */
public record ServiceConfig(
        int port,
        String editorUrl,
        List<String> allowedOrigins,
        Duration sessionLifetime,
        int sessionsPerHour,
        long maxPayloadBytes,
        DatabaseConfig database) {

    /**
     * @param host     {@code DB_HOST}, localhost
     * @param port     {@code DB_PORT}, 5432
     * @param name     {@code DB_NAME}, postgres
     * @param user     {@code DB_USER}, postgres
     * @param password {@code DB_PASSWORD}, empty
     * @param schema   {@code DB_SCHEMA}, bloodnight_editor
     */
    public record DatabaseConfig(String host, int port, String name, String user, String password, String schema) {}

    /**
     * @param environment the environment, e.g. {@link System#getenv()}
     * @return the settings, defaults where a variable is missing
     */
    public static ServiceConfig of(Map<String, String> environment) {
        Function<String, String> env = environment::get;
        return new ServiceConfig(
                integer(env, "PORT", 8080),
                text(env, "EDITOR_URL", "http://localhost:3000/session/{id}"),
                Arrays.stream(text(env, "ALLOWED_ORIGINS", "").split(","))
                        .map(String::strip)
                        .filter(origin -> !origin.isEmpty())
                        .toList(),
                Duration.ofHours(integer(env, "SESSION_HOURS", 24)),
                integer(env, "SESSIONS_PER_HOUR", 20),
                integer(env, "MAX_PAYLOAD_MB", 10) * 1024L * 1024L,
                new DatabaseConfig(
                        text(env, "DB_HOST", "localhost"),
                        integer(env, "DB_PORT", 5432),
                        text(env, "DB_NAME", "postgres"),
                        text(env, "DB_USER", "postgres"),
                        text(env, "DB_PASSWORD", ""),
                        text(env, "DB_SCHEMA", "bloodnight_editor")));
    }

    private static String text(Function<String, String> env, String name, String fallback) {
        String value = env.apply(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int integer(Function<String, String> env, String name, int fallback) {
        return Integer.parseInt(text(env, name, Integer.toString(fallback)));
    }
}
