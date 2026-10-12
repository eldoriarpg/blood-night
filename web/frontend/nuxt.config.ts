/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/** Where the backend answers while developing, unless the environment names another address. */
const BACKEND_URL = process.env.NUXT_BACKEND_URL || 'http://localhost:8080'

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
      useCookie: false,
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
      proxy: {
        '/api': {target: BACKEND_URL, changeOrigin: true, ws: true},
      },
    },
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
