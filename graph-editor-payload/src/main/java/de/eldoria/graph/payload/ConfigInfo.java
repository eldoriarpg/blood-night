/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.JsonNode;

/**
 * A value set on the node in the editor.
 *
 * @param id           stable field id
 * @param name         editor name
 * @param type         the field's type; always has a literal form
 * @param defaultValue value used when none is set, absent for required fields
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConfigInfo(String id, String name, TypeRef type, JsonNode defaultValue) {}
