/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {MobName} from '~/api/generated/payload'

/** The languages BloodNight ships texts in, offered first when a name gets another language. */
export const NAME_LANGUAGES: readonly string[] = ['en', 'de', 'es', 'fr', 'ms', 'tr', 'zh']

/**
 * A mob's name in one language, the way players without that language see it: that language's text, else English,
 * else the first by language code.
 *
 * @param name the mob's name, if it has one
 * @param language the language to show, e.g. the editor's `de`
 * @returns the text, empty for a mob without a name
 */
export function mobLabel(name: MobName | undefined, language: string): string {
    const texts = name?.texts ?? {}
    return texts[language] ?? texts.en ?? Object.keys(texts).sort().map(key => texts[key] ?? '').find(text => text !== '') ?? ''
}
