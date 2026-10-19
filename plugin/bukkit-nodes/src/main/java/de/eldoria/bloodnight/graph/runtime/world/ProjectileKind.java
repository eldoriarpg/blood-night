/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime.world;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Egg;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.Trident;
import org.bukkit.entity.WindCharge;
import org.bukkit.entity.WitherSkull;

/**
 * The projectiles a mob can launch, as the editor offers them.
 */
public enum ProjectileKind {
    ARROW(Arrow.class),
    SPECTRAL_ARROW(SpectralArrow.class),
    TRIDENT(Trident.class),
    SNOWBALL(Snowball.class),
    EGG(Egg.class),
    SMALL_FIREBALL(SmallFireball.class),
    LARGE_FIREBALL(LargeFireball.class),
    DRAGON_FIREBALL(DragonFireball.class),
    WITHER_SKULL(WitherSkull.class),
    SHULKER_BULLET(ShulkerBullet.class),
    WIND_CHARGE(WindCharge.class);

    private final Class<? extends Projectile> type;

    ProjectileKind(Class<? extends Projectile> type) {
        this.type = type;
    }

    /**
     * @return the projectile class
     */
    public Class<? extends Projectile> type() {
        return type;
    }
}
