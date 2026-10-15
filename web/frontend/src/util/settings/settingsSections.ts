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
    {key: 'general', properties: ['enabled']},
    {key: 'night', properties: ['nightSettings']},
    {key: 'selection', properties: ['nightSelection']},
    {key: 'mobs', properties: ['mobSettings']},
    {key: 'announcements', properties: ['announcement']},
    {key: 'sounds', properties: ['soundSettings']},
    {key: 'deathPlayers', properties: ['deathActions/player']},
    {key: 'deathMobs', properties: ['deathActions/mob']},
    {key: 'drops', properties: ['worldDrops']},
    {key: 'atmosphere', properties: ['atmosphere']},
    {key: 'rules', properties: ['nightRules']},
]

/** A tab of the server settings: a part of the settings payload and some of its properties, or all of it. */
export interface ServerSection {
    key: string
    root: 'main' | 'globalDrops'
    properties: string[] | undefined
}

/** The tabs of the server settings. */
export const SERVER_SECTIONS: readonly ServerSection[] = [
    {key: 'messages', root: 'main', properties: ['announcements']},
    {key: 'editor', root: 'main', properties: ['editor']},
    {key: 'resourcePack', root: 'main', properties: ['resourcePack']},
    {key: 'plugins', root: 'main', properties: ['placeholders', 'foreignMobs']},
    {key: 'updates', root: 'main', properties: ['updates']},
    {key: 'drops', root: 'globalDrops', properties: undefined},
]
