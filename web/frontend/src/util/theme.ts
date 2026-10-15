/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/** The editor's colours: dark, its own, or light. */
export type Theme = 'dark' | 'light'

/**
 * Sets the theme picked before the page is first drawn, so a light page never flashes dark. Runs as an inline script
 * in the page's head, before anything else, which is why it is plain text. The browser keeps the theme under `bloodnight.theme`.
 */
export const THEME_SCRIPT = `try{if(localStorage.getItem('bloodnight.theme')==='light')document.documentElement.dataset.theme='light'}catch(e){}`
