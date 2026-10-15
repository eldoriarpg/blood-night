/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

const LANG_OR = /<lang_or:([^:>]+):'((?:[^'\\]|\\.)*)'>/g

/**
 * Whether a MiniMessage text holds a text players see in their own language, e.g.
 * `<lang_or:bloodnight.mob.armored_zombie:'Armored Zombie'>`.
 */
export function isTranslated(text: string): boolean {
    return new RegExp(LANG_OR.source).test(text)
}

/**
 * A MiniMessage text with every text in the player's language replaced by its English fallback, e.g.
 * `<dark_red><lang_or:bloodnight.mob.armored_zombie:'Armored Zombie'>` becomes `<dark_red>Armored Zombie`.
 */
export function withFallbacks(text: string): string {
    return text.replaceAll(LANG_OR, (_, _key: string, fallback: string) => fallback.replaceAll(/\\(.)/g, '$1'))
}
