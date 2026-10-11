/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.payload;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A data input or output.
 *
 * @param id          stable port id
 * @param name        editor name
 * @param description help text
 * @param type        the port's type
 * @param optional    inputs only: may stay empty
 * @param literal     inputs only: a value can be typed in instead of connecting a node
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record PortInfo(String id, String name, String description, TypeRef type, boolean optional, boolean literal) {}
