/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.eldoria.bloodnight.configuration.world.NightSelection;
import tools.jackson.databind.json.JsonMapper;

/**
 * Night selections for tests, read from JSON the way the configuration reads them: through fields.
 */
public final class Selections {
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .changeDefaultVisibility(checker -> checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE))
            .build();

    private Selections() {}

    static NightSelection of(String json) {
        return MAPPER.readValue(json, NightSelection.class);
    }

    public static <T> T read(String json, Class<T> type) {
        return MAPPER.readValue(json, type);
    }

    static String write(NightSelection selection) {
        return MAPPER.writeValueAsString(selection);
    }
}
