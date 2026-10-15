/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/**
 * In development, reloads the page once a locale file changes, so new texts show up beside the components that
 * already use them. The dev server tells when a locale file last changed (see `nuxt.config.ts`), asked every two
 * seconds while the page is visible.
 */
export default defineNuxtPlugin(() => {
    if (!import.meta.dev) return
    const url = `${useRuntimeConfig().app.buildAssetsDir.replace(/\/$/, '')}/__locale-version`
    let known: string | undefined
    setInterval(async () => {
        if (document.hidden) return
        const version = await fetch(url, {cache: 'no-store'}).then(response => response.ok ? response.text() : undefined, () => undefined)
        if (version === undefined) return
        if (known !== undefined && version !== known) location.reload()
        known = version
    }, 2000)
})
