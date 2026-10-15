/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {config} from '@vue/test-utils'
import {createI18n} from 'vue-i18n'
import en from '@/i18n/en'

/**
 * Everything every mounted component needs and no test should have to repeat.
 *
 * <p>The i18n plugin is real rather than stubbed, in English, the language every other falls back
 * to, so a test asserting a label asserts the label a reader sees. The stubs are the two globals
 * that would otherwise drag half the app into a mount: the icon library and the router behind every
 * link. The icon stub names the icon it was handed, so a test can tell which one is drawn.
 */
const i18n = createI18n({
    legacy: false,
    locale: 'en',
    messages: {en, de: {}, es: {}, fr: {}, ms: {}, tr: {}, zh: {}},
})

config.global.plugins = [i18n]

config.global.stubs = {
    'font-awesome-icon': {
        template: '<span data-testid="icon" :data-icon="String(icon)" />',
        props: ['icon'],
    },
    NuxtLink: {
        template: '<a :href="to"><slot /></a>',
        props: ['to'],
    },
}
