/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {RouterConfig} from '@nuxt/schema'

/**
 * Scrolling between pages. A URL fragment is never an anchor here: on a session link it carries the participant's
 * token, so it must not be looked up as an element. A page opens at the top, and going back returns to where it was.
 */
export default {
    scrollBehavior: (to, from, savedPosition) => {
        if (savedPosition) return savedPosition
        if (to.path === from.path) return false
        return {left: 0, top: 0}
    },
} satisfies RouterConfig
