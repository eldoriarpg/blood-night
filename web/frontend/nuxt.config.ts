/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

import {readdirSync, statSync} from 'node:fs'
import {resolve} from 'node:path'
import {fileURLToPath} from 'node:url'
import type {Plugin} from 'vite'
import {THEME_SCRIPT} from './src/util/theme'

/** Where the sources are, which Nuxt's watcher names files relative to. */
const SRC = fileURLToPath(new URL('./src/', import.meta.url))

/** Where the backend answers while developing, unless the environment names another address. */
const BACKEND_URL = process.env.NUXT_BACKEND_URL || 'http://localhost:8080'

/**
 * Whether file changes are found by polling, which a container needs: changes made on the host do not reach its file
 * events. Both watchers are told, Nuxt's (locale files, pages) and Vite's (components), since current chokidar no
 * longer reads `CHOKIDAR_USEPOLLING` on its own.
 */
const POLLING = process.env.CHOKIDAR_USEPOLLING === 'true'

/** Where the dev server tells the latest change to any locale file, see `plugins/localeReload.client.ts`. */
const LOCALE_VERSION = '/__locale-version'

function localeVersion(): number {
  const folder = resolve(SRC, 'i18n')
  return Math.max(0, ...readdirSync(folder).map(name => statSync(resolve(folder, name)).mtimeMs))
}

/**
 * Keeps locale files fresh. A request for a locale file that changed since the last request drops Vite's copy first,
 * and the time of the latest change is served for the page to reload itself by (see `plugins/localeReload.client.ts`).
 * The messages are not swapped in place, and in a container the file watchers report locale changes too unreliably to
 * push a reload.
 */
function freshLocales(): Plugin {
  const seen = new Map<string, number>()
  return {
    name: 'bloodnight:fresh-locales',
    apply: 'serve',
    configureServer(server) {
      server.middlewares.use((request, response, next) => {
        if (request.url?.split('?')[0]?.endsWith(LOCALE_VERSION)) {
          response.setHeader('Content-Type', 'text/plain')
          response.setHeader('Cache-Control', 'no-store')
          response.end(String(localeVersion()))
          return
        }
        const name = /\/i18n\/([a-z-]+\.ts)(\?|$)/.exec(request.url ?? '')?.[1]
        if (name) {
          const file = resolve(SRC, 'i18n', name)
          const changed = statSync(file, {throwIfNoEntry: false})?.mtimeMs
          if (changed !== undefined && seen.get(file) !== changed) {
            seen.set(file, changed)
            for (const module of server.moduleGraph.getModulesByFile(file) ?? []) server.moduleGraph.invalidateModule(module)
          }
        }
        next()
      })
    },
  }
}

/**
 * The web editor's frontend.
 *
 * <p>The landing page renders on the server, an editor session renders in the browser only, because
 * the session is opened from a link with a token the server never sees. `/api` belongs to the
 * backend: in development the dev proxies hand it on, HTTP and WebSocket alike, and the built server
 * forwards it through `server/middleware/backend-proxy.ts` and the editor stream route.
 *
 * <p>`runtimeConfig.backendUrl` is read per request, so one build serves any backend address.
 */
export default defineNuxtConfig({
  devServer: {
    port: 4000,
  },

  compatibilityDate: '2025-06-08',

  srcDir: 'src/',

  app: {
    head: {
      script: [{innerHTML: THEME_SCRIPT}],
      link: [{rel: 'icon', type: 'image/svg+xml', href: '/logo.svg'}],
      meta: [
        {property: 'og:image', content: '/logo.png'},
        {name: 'theme-color', content: '#c8243a'},
      ],
    },
  },

  modules: ['@nuxt/test-utils/module', '@nuxt/eslint', '@nuxtjs/i18n'],

  eslint: {
    config: {
      autoInit: false,
    },
  },

  i18n: {
    restructureDir: 'src',
    langDir: 'i18n',
    vueI18n: 'i18n/config.ts',
    strategy: 'no_prefix',
    defaultLocale: 'en',
    detectBrowserLanguage: {
      useCookie: true,
      cookieKey: 'bloodnight-language',
      redirectOn: 'root',
      fallbackLocale: 'en',
    },
    locales: [
      {code: 'en', language: 'en', file: 'en.ts'},
      {code: 'de', language: 'de', file: 'de.ts'},
    ],
  },

  runtimeConfig: {
    backendUrl: BACKEND_URL,
  },

  css: ['~/style.css'],

  routeRules: {
    '/': {ssr: true},
    '/session/**': {ssr: false},
    '/style': {ssr: false},
  },

  nitro: {
    experimental: {
      websocket: true,
    },
    externals: {
      inline: [
        '@fortawesome/fontawesome-svg-core',
        '@fortawesome/free-solid-svg-icons',
        '@fortawesome/vue-fontawesome',
      ],
    },
    devProxy: {
      '/api': {target: BACKEND_URL, changeOrigin: true, ws: true},
    },
  },

  vite: {
    plugins: [freshLocales()],
    build: {
      sourcemap: false,
    },
    optimizeDeps: {
      include: [
        '@fortawesome/fontawesome-svg-core',
        '@fortawesome/free-solid-svg-icons',
        '@fortawesome/vue-fontawesome',
        '@vue/devtools-core',
        '@vue/devtools-kit',
        'axios',
        'vue-i18n',
      ],
    },
    server: {
      allowedHosts: true,
      watch: POLLING ? {usePolling: true, interval: 300} : undefined,
      proxy: {
        '/api': {target: BACKEND_URL, changeOrigin: true, ws: true},
      },
    },
  },

  watchers: {
    chokidar: POLLING ? {usePolling: true, interval: 300} : {},
  },

  hooks: {
    /** Adds Tailwind's Vite plugin to every Vite config Nuxt builds, client and server alike. */
    'vite:extendConfig'(config: { plugins?: unknown[] }) {
      import('@tailwindcss/vite').then(m => {
        config.plugins ||= []
        config.plugins.push(m.default())
      })
    },
  },

  typescript: {
    strict: true,
  },

  components: {
    dirs: [
      {path: '~/components', pathPrefix: false},
    ],
  },
})
