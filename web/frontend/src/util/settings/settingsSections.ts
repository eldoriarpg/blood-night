/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** A tab of a settings form: some properties of a settings object, each a name or a path such as `deathActions/mob`. */
export interface SettingsSection {
    key: string
    properties: string[]
}

/**
 * The tabs of a world's settings, each a few properties of `WorldSettings`; copying world settings copies whole
 * tabs.
 */
export const WORLD_SECTIONS: readonly SettingsSection[] = [
    {key: 'general', properties: ['enabled', 'displayName']},
    {key: 'night', properties: ['nightSettings']},
    {key: 'selection', properties: ['nightSelection']},
    {key: 'mobs', properties: ['mobSettings']},
    {key: 'announcements', properties: ['announcement']},
    {key: 'sounds', properties: ['soundSettings']},
    {key: 'deathPlayers', properties: ['deathActions/player']},
    {key: 'deathMobs', properties: ['deathActions/mob']},
    {key: 'loot', properties: ['loot']},
    {key: 'atmosphere', properties: ['atmosphere']},
    {key: 'rules', properties: ['nightRules']},
]

/** A tab of the server settings: some properties of `config.yml`. */
export type ServerSection = SettingsSection

/** The tabs of the server settings. */
export const SERVER_SECTIONS: readonly ServerSection[] = [
    {key: 'messages', properties: ['announcements']},
    {key: 'editor', properties: ['editor']},
    {key: 'resourcePack', properties: ['resourcePack']},
    {key: 'plugins', properties: ['placeholders', 'foreignMobs']},
    {key: 'updates', properties: ['updates']},
    {key: 'loot', properties: ['loot']},
]
