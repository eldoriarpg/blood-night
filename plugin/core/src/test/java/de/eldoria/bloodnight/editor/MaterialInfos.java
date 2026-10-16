/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import com.google.common.collect.ImmutableMultimap;
import org.bukkit.Material;
import org.jspecify.annotations.Nullable;

/**
 * {@link MaterialInfo} for tests: MockBukkit has no default attribute modifiers, so materials give none here.
 */
public final class MaterialInfos {
    private MaterialInfos() {}

    /**
     * @return what the editor needs to know about the material, without its attribute modifiers
     */
    public static @Nullable MaterialInfo of(Material material) {
        return MaterialInfo.of(material, ignored -> ImmutableMultimap.of());
    }
}
