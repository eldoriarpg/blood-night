/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.assets;

/**
 * Routes of Minecraft's own item assets, served for the editor's item pickers. Public and cacheable for good, since a
 * Minecraft version's assets never change.
 * <ul>
 *   <li>{@code GET} {@value #ITEMS}: every item of a version with its icon and names ({@link ItemIndex});
 *   {@code 404} for a version Mojang does not know, {@code 503} while Mojang cannot be reached</li>
 *   <li>{@code GET} {@value #TEXTURE}: an icon as PNG, {@code folder} {@code item}, {@code block} or
 *   {@code particle}, {@code name} ending in {@code .png}</li>
 *   <li>{@code GET} {@value #PARTICLES}: the sprites of every particle of a version ({@link ParticleIndex}); answers
 *   like {@value #ITEMS}</li>
 *   <li>{@code GET} {@value #SOUNDS}: every sound of a version with its subtitles ({@link SoundIndex}); answers like
 *   {@value #ITEMS}</li>
 *   <li>{@code GET} {@value #SOUND}: a sound of {@value #SOUNDS} as Ogg Vorbis, by its key, e.g.
 *   {@code entity.zombie.ambient.ogg}</li>
 * </ul>
 */
public final class AssetRoutes {
    public static final String ITEMS = "/api/v1/assets/{version}/items";
    public static final String TEXTURE = "/api/v1/assets/{version}/textures/{folder}/{name}";
    public static final String SOUNDS = "/api/v1/assets/{version}/sounds";
    public static final String PARTICLES = "/api/v1/assets/{version}/particles";
    public static final String SOUND = "/api/v1/assets/{version}/sounds/{name}";

    private AssetRoutes() {}
}
