/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/**
 * English is the language fallen back to; German, Spanish, French, Malay, Turkish and Chinese are translated, the
 * languages the plugin ships.
 *
 * <p>The messages are not part of this configuration. `@nuxtjs/i18n` reads the locale files named
 * in `nuxt.config.ts` and loads a language only once it is shown, and every server request gets an
 * instance of its own, so a language one request switches to never reaches the next.
 */
export default defineI18nConfig(() => ({
    legacy: false,
    fallbackLocale: 'en',
}))
