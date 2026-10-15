/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/**
 * Sets the light or dark theme before the page is drawn: the one picked on the documentation site, which shares this
 * site's storage, else the system's.
 */
(function () {
    var picked = null;
    try {
        picked = window.localStorage.getItem('theme');
    } catch (ignored) {
        picked = null;
    }
    var dark = picked ? picked === 'dark' : !window.matchMedia || window.matchMedia('(prefers-color-scheme: dark)').matches;
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
})();
