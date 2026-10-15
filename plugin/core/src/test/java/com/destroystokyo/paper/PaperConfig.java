/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package com.destroystokyo.paper;

/**
 * Stands in for the Paper server class eldo-util's message sender looks for to tell Paper from Spigot. MockBukkit
 * ships the Paper API but not the server, so without it the tests would get the Spigot sender, which needs the
 * adventure-platform library a Paper plugin leaves out.
 */
public final class PaperConfig {
    private PaperConfig() {}
}
