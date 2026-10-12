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
 *   <li>{@code GET} {@value #TEXTURE}: an icon as PNG, {@code folder} {@code item} or {@code block}, {@code name}
 *   ending in {@code .png}</li>
 * </ul>
 */
public final class AssetRoutes {
    public static final String ITEMS = "/api/v1/assets/{version}/items";
    public static final String TEXTURE = "/api/v1/assets/{version}/textures/{folder}/{name}";

    private AssetRoutes() {}
}
