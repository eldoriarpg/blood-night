/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.configuration.elements.world.NightSelection;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

/**
 * World settings for tests, read from JSON the way the configuration reads them: through fields.
 */
public final class Settings {
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .changeDefaultVisibility(checker -> checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE))
            .build();

    private Settings() {}

    /**
     * @param json the settings that differ from the defaults
     * @return the settings of a world with blood nights turned on
     */
    public static WorldSettings world(String json) {
        WorldSettings settings = MAPPER.readValue(json, WorldSettings.class);
        return MAPPER.updateValue(settings, Map.of("enabled", true));
    }

    /**
     * @return the default settings of a world with blood nights turned on
     */
    public static WorldSettings enabled() {
        return world("{}");
    }

    static NightSelection everyNight() {
        return MAPPER.readValue(
                "{\"type\":\"INTERVAL\",\"interval\":{\"interval\":1,\"probability\":100}}", NightSelection.class);
    }
}
