/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import vueI18n from '@intlify/eslint-plugin-vue-i18n'
import {createTypeScriptImportResolver} from 'eslint-import-resolver-typescript'
import vueA11y from 'eslint-plugin-vuejs-accessibility'
import withNuxt from './.nuxt/eslint.config.mjs'
import * as localeParser from './eslint/i18n/locale-parser.mjs'
import bloodnight from './eslint/index.mjs'
import {LANGUAGES, localeModules, localeSnapshots} from './eslint/locales.mjs'
import {INLINE_STATE_TYPE, restrictSyntax} from './eslint/restrictions.mjs'

/**
 * The messages as the application holds them, for the rules that look keys up. The locale modules
 * themselves are added to this for the locale rules only: the plugin cannot load their messages,
 * which the rules on components would trip over.
 */
const SNAPSHOTS = await localeSnapshots()

/** The locale modules, which only the locale rules read. */
const LOCALE_FILES = LANGUAGES.map(language => `src/i18n/${language}.ts`)

/** The English messages, which every other language falls back to. */
const BASE = [{file: 'src/i18n/en.ts'}]

/** The languages read against English. */
const TRANSLATIONS = LOCALE_FILES.filter(file => file !== 'src/i18n/en.ts')

/**
 * Which layer may not reach which, as `import/no-restricted-paths` zones.
 *
 * <p>The bottom layers do not import from the ones above them, so that lifting a piece out never
 * means dragging half the application along: the API clients and the translations stand on nothing
 * of the interface, and components and composables stand on no view, layout or page.
 */
const LAYERS = [
    {layer: 'api', above: ['views', 'components', 'composables', 'layouts', 'pages']},
    {layer: 'components', above: ['views', 'layouts', 'pages'], stateThroughComposables: true},
    {layer: 'composables', above: ['views', 'layouts', 'pages']},
    {layer: 'i18n', above: ['views', 'components', 'composables', 'layouts', 'pages']},
    {layer: 'views', above: [], stateThroughComposables: true},
    {layer: 'layouts', above: [], stateThroughComposables: true},
    {layer: 'pages', above: [], stateThroughComposables: true},
]

/**
 * The modules holding state that more than one composable builds on.
 *
 * <p>Only composables, other `util` modules, the API clients, plugins and middleware reach them. A
 * view, component, layout or page goes through the composable, which hands out read-only views and
 * the setters that belong to them. A test may still arrange the state it starts from.
 *
 * <p>`browserState` is the exception: it holds no state, it is the helper state is built with.
 */
const SHARED_STATE = {
    from: './src/util/*State.ts',
    except: [`${import.meta.dirname}/src/util/+(browserState).ts`],
}

/**
 * The `import/no-restricted-paths` zones of one layer.
 *
 * @param layer the layer, as listed in {@link LAYERS}
 * @returns the zones its files are held to
 */
function layerZones({layer, above, stateThroughComposables}) {
    const zones = above.map(from => ({
        target: `./src/${layer}`,
        from: `./src/${from}`,
        message: `${layer}/ may not import from ${from}/.`,
    }))
    if (!stateThroughComposables) return zones
    return [...zones, {
        target: `./src/${layer}/**/!(*.test).{ts,vue}`,
        ...SHARED_STATE,
        message: `${layer}/ reaches shared state through its composable, not util/*State.ts.`,
    }]
}

/**
 * Vue's rules as the components here are written.
 *
 * <p>Blocks go script, template, style. An element with nothing inside closes itself, a void one
 * included. Where the first attribute of an element written over several lines stands is
 * formatting, like the rest of Vue's layout rules that Nuxt's config switches off when its
 * stylistic preset is off. `v-html` is refused everywhere, without exceptions.
 */
const VUE_RULES = {
    'vue/block-order': ['error', {order: ['script', 'template', 'style']}],
    'vue/first-attribute-linebreak': 'off',
    'vue/html-self-closing': ['error', {html: {void: 'always', normal: 'always', component: 'always'}, svg: 'always', math: 'always'}],
    'vue/no-v-text-v-html-on-component': 'error',
    'vue/no-v-html': 'error',
}

/**
 * The components whose root is a native form control, or a switch button, which a label names
 * like the element itself.
 *
 * <p>A label is associated with its control by wrapping it or by pointing at its id, either of the
 * two, which is what the accessibility tree reads. The rule's default asks for both at once.
 */
const CONTROL_COMPONENTS = [
    'BaseInput', 'CheckboxInput', 'SelectInput', 'TextInput', 'NumberInput', 'DecimalInput', 'SearchInput',
    'ToggleInput', 'TextAreaInput',
]

export default withNuxt(
    {
        name: 'bloodnight/ignores',
        ignores: ['e2e/report/**', 'e2e/results/**', 'public/**', 'coverage/**', 'eslint/**/fixtures/**', 'src/api/generated/**'],
    },
    {
        name: 'bloodnight/settings',
        settings: {
            'import-x/resolver-next': [createTypeScriptImportResolver({project: './tsconfig.json'})],
            'vue-i18n': {
                localeDir: SNAPSHOTS,
                messageSyntaxVersion: '^11.0.0',
            },
        },
    },
    {
        name: 'bloodnight/plugin',
        plugins: {bloodnight},
    },
    {
        name: 'bloodnight/console',
        files: ['src/**/*.{ts,vue}'],
        ignores: ['src/plugins/**'],
        rules: {
            'no-restricted-properties': ['error',
                {object: 'console', property: 'log', message: 'Leftover console.log: remove it.'},
                {object: 'console', property: 'debug', message: 'Leftover console.debug: remove it.'},
            ],
        },
    },
    ...vueA11y.configs['flat/recommended'],
    {
        name: 'bloodnight/accessibility',
        files: ['**/*.vue'],
        rules: {
            'vuejs-accessibility/label-has-for': ['error', {
                required: {some: ['nesting', 'id']},
                allowChildren: true,
                controlComponents: CONTROL_COMPONENTS,
            }],
        },
    },
    {
        name: 'bloodnight/vue',
        files: ['**/*.vue'],
        rules: VUE_RULES,
    },
    {
        name: 'bloodnight/templates',
        files: ['src/**/*.vue'],
        rules: {
            'bloodnight/button-row': 'error',
            'bloodnight/no-dead-button-size': 'error',
            'bloodnight/no-stacked-inline-text': 'error',
            'bloodnight/styled-elements': 'error',
        },
    },
    {
        name: 'bloodnight/pages',
        files: ['src/pages/**/*.vue'],
        rules: {
            'bloodnight/context-title': 'error',
            'bloodnight/route-view-content': 'error',
        },
    },
    {
        name: 'bloodnight/view-templates',
        files: ['src/**/*.vue'],
        ignores: ['src/components/**'],
        rules: {
            'bloodnight/repeated-class-pattern': 'error',
            'bloodnight/section-density': 'error',
            'bloodnight/template-block-size': 'error',
        },
    },
    {
        name: 'bloodnight/vue-i18n',
        files: ['src/**/*.{ts,vue}'],
        ignores: ['src/i18n/**', 'src/**/*.test.ts'],
        plugins: {'@intlify/vue-i18n': vueI18n},
        rules: {
            '@intlify/vue-i18n/no-missing-keys': 'error',
        },
    },
    {
        name: 'bloodnight/locales',
        files: LOCALE_FILES,
        languageOptions: {parser: localeParser},
        plugins: {'@intlify/vue-i18n': vueI18n},
        settings: {
            'vue-i18n': {
                localeDir: [SNAPSHOTS, localeModules()],
                messageSyntaxVersion: '^11.0.0',
            },
        },
        rules: {
            '@intlify/vue-i18n/valid-message-syntax': 'error',
            'bloodnight/i18n-no-plural-messages': 'error',
            'bloodnight/i18n-unused-keys': ['error', {sources: 'src', base: BASE, translations: TRANSLATIONS}],
        },
    },
    ...LAYERS.map(layer => ({
        name: `bloodnight/layers/${layer.layer}`,
        files: [`src/${layer.layer}/**/*.{ts,vue}`],
        rules: {
            'import/no-restricted-paths': ['error', {zones: layerZones(layer)}],
        },
    })),
    {
        name: 'bloodnight/source-size',
        files: ['src/**/*.{ts,vue}'],
        ignores: ['src/**/*.test.ts', 'src/i18n/**'],
        rules: {
            'bloodnight/max-function-lines': ['error', {max: 79}],
        },
    },
    {
        name: 'bloodnight/module-state',
        files: ['src/**/*.{ts,vue}'],
        ignores: ['src/**/*.test.ts', 'src/test/**'],
        rules: {
            'bloodnight/no-module-state': 'error',
        },
    },
    {
        name: 'bloodnight/enums',
        files: ['src/**/*.{ts,vue}'],
        rules: {
            'bloodnight/no-hand-kept-enum': 'error',
        },
    },
    {
        name: 'bloodnight/state-types',
        files: ['src/**/*.{ts,vue}'],
        rules: restrictSyntax(INLINE_STATE_TYPE),
    },
    {
        name: 'bloodnight/views',
        files: ['src/views/**/*.vue'],
        rules: {
            'max-lines': ['error', {max: 500}],
        },
    },
).onResolved(configs => configs.map(keepOffLocales))

/**
 * Leaves the locale modules to the locale rules.
 *
 * <p>The locale parser hands ESLint a JSON AST, which no rule written for a script can read, so
 * every entry that switches rules on, or runs a processor, ignores those files. The entries that
 * only register plugins or carry settings stay global, since the locale rules need both.
 *
 * @param config one resolved config entry
 * @returns the entry, ignoring the locale modules where it would apply rules to them
 */
function keepOffLocales(config) {
    if (config.name === 'bloodnight/locales') return config
    if (!config.rules && !config.processor) return config
    return {...config, ignores: [...(config.ignores ?? []), ...LOCALE_FILES]}
}
