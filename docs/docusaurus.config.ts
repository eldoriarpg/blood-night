/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {themes as prismThemes} from 'prism-react-renderer'
import type {Config} from '@docusaurus/types'
import type * as Preset from '@docusaurus/preset-classic'

const config: Config = {
  title: 'BloodNight',
  tagline: 'Some nights turn red. Special mobs come out, and you decide how they behave.',
  favicon: 'img/favicon.svg',
  future: {
    v4: true,
    faster: true,
  },
  url: 'https://eldoriarpg.github.io',
  baseUrl: '/blood-night/',
  organizationName: 'eldoriarpg',
  projectName: 'blood-night',
  trailingSlash: false,
  onBrokenLinks: 'throw',
  markdown: {
    format: 'detect',
    hooks: {
      onBrokenMarkdownLinks: 'throw',
    },
  },
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },
  presets: [
    [
      'classic',
      {
        docs: {
          routeBasePath: '/',
          sidebarPath: './sidebars.ts',
          editUrl: 'https://github.com/eldoriarpg/blood-night/tree/main/docs/',
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],
  themeConfig: {
    colorMode: {
      defaultMode: 'dark',
      respectPrefersColorScheme: true,
    },
    navbar: {
      title: 'BloodNight',
      logo: {
        alt: 'BloodNight',
        src: 'img/favicon.svg',
      },
      items: [
        {type: 'docSidebar', sidebarId: 'guide', position: 'left', label: 'Guide'},
        {type: 'docSidebar', sidebarId: 'reference', position: 'left', label: 'Reference'},
        {href: 'https://github.com/eldoriarpg/blood-night', label: 'GitHub', position: 'right'},
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Help',
          items: [
            {label: 'Discord', href: 'https://bn.discord.eldoria.de'},
            {label: 'Issues', href: 'https://github.com/eldoriarpg/blood-night/issues'},
          ],
        },
      ],
      copyright: `BloodNight is free software under the AGPL-3.0. Made by RainbowDashLabs and contributors.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['java', 'yaml', 'bash'],
    },
  } satisfies Preset.ThemeConfig,
}

export default config
