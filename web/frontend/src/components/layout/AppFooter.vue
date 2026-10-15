/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import LanguagePicker from '@/components/layout/LanguagePicker.vue'
import ThemeToggle from '@/components/layout/ThemeToggle.vue'
import {DOCS_URL} from '~/util/docsLinks'

/**
 * The line across the bottom of every page: where to read on and ask, the licence, and the language and colours the
 * editor is drawn in.
 */
const {t} = useI18n()
const links = computed(() => [
  {label: t('footer.docs'), href: DOCS_URL},
  {label: 'GitHub', href: 'https://github.com/eldoriarpg/blood-night'},
  {label: 'Discord', href: 'https://bn.discord.eldoria.de'},
])
</script>

<template>
  <footer class="flex shrink-0 flex-wrap items-center gap-x-4 gap-y-1 border-t border-line-soft bg-surface px-4 py-1 text-xs text-ink-faint">
    <span class="font-heading font-bold text-ink-muted">{{ t('app.name') }}</span>
    <a v-for="link in links" :key="link.href" :href="link.href" class="hover:text-ink" rel="noopener" target="_blank">{{ link.label }}</a>
    <span>{{ t('footer.license') }}</span>
    <span class="ml-auto flex items-center gap-2">
      <LanguagePicker/>
      <ThemeToggle/>
    </span>
  </footer>
</template>
