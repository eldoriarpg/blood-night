/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.updates;

import de.eldoria.bloodnight.configuration.ConfigFile;
import de.eldoria.bloodnight.configuration.Configuration;
import de.eldoria.bloodnight.configuration.SettingsPaths;
import de.eldoria.bloodnight.configuration.server.UpdateSettings;
import de.eldoria.eldoutilities.updater.AutoUpdateDecision;
import de.eldoria.eldoutilities.updater.Updater;
import de.eldoria.eldoutilities.updater.lynaupdater.LynaUpdateData;
import de.eldoria.eldoutilities.updater.lynaupdater.LynaUpdateResponse;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

/**
 * Looks for new BloodNight versions on Eldoria's release service Lyna, at start and every six hours after that. A new
 * version is told to the console and, on join, to players with {@value #PERMISSION}. With auto update on, a new stable
 * release is downloaded into the server's update folder and takes effect on the next start. Otherwise admins are
 * offered to download it and to switch auto update on from chat; their choice is written to {@code config.yml}.
 */
public final class Updates {
    /**
     * The permission to hear about new versions on join and to download them.
     */
    public static final String PERMISSION = "bloodnight.updates";

    /**
     * Lyna's product id of BloodNight.
     */
    static final int PRODUCT = 29;

    static final String DOWNLOAD = "https://github.com/eldoriarpg/blood-night/releases/latest";
    static final String AVAILABLE = "bloodnight.update.available";
    private final Plugin plugin;
    private final Configuration configuration;
    private @Nullable Updater<LynaUpdateResponse, LynaUpdateData> updater;

    /**
     * @param plugin        the plugin whose versions are looked up
     * @param configuration where the update settings are read and an admin's choice is written
     */
    public Updates(Plugin plugin, Configuration configuration) {
        this.plugin = plugin;
        this.configuration = configuration;
    }

    /**
     * Applies the current update settings: starts looking for new versions, hands the running updater the new
     * settings, or stops it when the settings neither want to hear about new versions nor download them.
     */
    public void apply() {
        UpdateSettings settings = configuration.main().updates();
        if (!settings.notice() && !settings.autoUpdate()) {
            stop();
            return;
        }
        LynaUpdateData data = data(settings);
        if (updater == null) {
            updater = Updater.lyna(data);
            updater.start();
        } else {
            updater.reconfigure(data);
        }
    }

    /**
     * Stops looking for new versions.
     */
    public void stop() {
        if (updater != null) updater.stop();
        updater = null;
    }

    private LynaUpdateData data(UpdateSettings settings) {
        return LynaUpdateData.builder(plugin, PRODUCT)
                .notifyPermission(PERMISSION)
                .notifyUpdate(settings.notice())
                .autoUpdate(settings.autoUpdate())
                .askForAutoUpdate(settings.askForAutoUpdate())
                .autoUpdateHook(this::decided)
                .updateUrl(DOWNLOAD)
                .updateMessage(AVAILABLE)
                .build();
    }

    private void decided(AutoUpdateDecision decision) {
        configuration.replaceMain(decide(configuration.main(), decision));
    }

    /**
     * @param settings the current settings, left unchanged
     * @param decision what an admin chose in chat
     * @return new settings that keep the choice
     */
    static ConfigFile decide(ConfigFile settings, AutoUpdateDecision decision) {
        return switch (decision) {
            case ENABLED -> SettingsPaths.write(settings, ConfigFile.class, "updates.autoUpdate", "true");
            case NEVER_ASK -> SettingsPaths.write(settings, ConfigFile.class, "updates.askForAutoUpdate", "false");
        };
    }
}
