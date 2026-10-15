/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.updates;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * Eldoria's release service Lyna, which answers whether a version of a product is outdated:
 * {@code GET /api/v1/update/check?id=<product>&version=<version>} gives {@code {"update": true, "latest": "1.1.0"}}.
 */
public final class LynaSource implements ReleaseSource {
    /**
     * Lyna's product id of BloodNight.
     */
    public static final int BLOODNIGHT = 4;

    private static final URI HOST = URI.create("https://lyna.eldoria.de/");
    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private final HttpClient client;
    private final URI host;
    private final int product;

    /**
     * @param client  sends the request
     * @param host    the Lyna instance
     * @param product the product id
     */
    public LynaSource(HttpClient client, URI host, int product) {
        this.client = client;
        this.host = host;
        this.product = product;
    }

    /**
     * @return BloodNight on Eldoria's Lyna
     */
    public static LynaSource bloodNight() {
        return new LynaSource(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), HOST, BLOODNIGHT);
    }

    @Override
    public Optional<String> newer(String current) {
        URI uri = host.resolve("api/v1/update/check?id=%d&version=%s"
                .formatted(product, URLEncoder.encode(current, StandardCharsets.UTF_8)));
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .header("User-Agent", "BloodNight/" + current)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return Optional.empty();
            JsonNode body = MAPPER.readTree(response.body());
            if (!body.path("update").asBoolean(false)) return Optional.empty();
            String latest = body.path("latest").asString("");
            return latest.isBlank() ? Optional.empty() : Optional.of(latest);
        } catch (IOException | JacksonException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
}
