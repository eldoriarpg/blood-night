/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Theme} from '~/util/theme'

/**
 * The theme the editor is drawn in, as the browser remembers it, and switching it. The page's head sets it before the
 * first paint (see `THEME_SCRIPT`); this reads it back from there and keeps the page and the browser's memory current.
 */
export function useTheme() {
    const theme = useState<Theme>('useTheme', () => 'dark')

    if (import.meta.client) {
        theme.value = document.documentElement.dataset.theme === 'light' ? 'light' : 'dark'
    }

    function toggle(): void {
        theme.value = theme.value === 'light' ? 'dark' : 'light'
        if (theme.value === 'light') document.documentElement.dataset.theme = 'light'
        else delete document.documentElement.dataset.theme
        try {
            localStorage.setItem('bloodnight.theme', theme.value)
        } catch {
            return
        }
    }

    return {theme, toggle}
}
