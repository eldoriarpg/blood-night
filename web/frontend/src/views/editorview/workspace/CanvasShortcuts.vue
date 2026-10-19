/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'

/**
 * A small help in the canvas's bottom corner listing what the keys and mouse do there. Folded to one button by
 * default, so it never covers the graph unasked.
 */
const {t} = useI18n()
const open = ref(false)

const SHORTCUTS = ['addNode', 'wire', 'details', 'reroute', 'select', 'addToSelection', 'comment', 'pan', 'zoom', 'delete', 'free'] as const
</script>

<template>
  <div class="absolute bottom-4 left-4">
    <SecondaryButton v-if="!open" :aria-expanded="false" compact icon="keyboard" @click="open = true">{{ t('editor.shortcuts.open') }}</SecondaryButton>
    <section v-else :aria-label="t('editor.shortcuts.title')" class="flex w-80 flex-col gap-2 rounded-panel border border-line bg-surface p-3 text-xs">
      <div class="flex items-center justify-between">
        <SectionLabel>{{ t('editor.shortcuts.title') }}</SectionLabel>
        <MutedIconButton :aria-expanded="true" :label="t('editor.shortcuts.close')" icon="xmark" @click="open = false"/>
      </div>
      <dl class="grid grid-cols-[auto_minmax(0,1fr)] gap-x-3 gap-y-1.5">
        <template v-for="shortcut in SHORTCUTS" :key="shortcut">
          <dt class="whitespace-nowrap font-mono text-ink">{{ t(`editor.shortcuts.keys.${shortcut}`) }}</dt>
          <dd class="text-ink-muted">{{ t(`editor.shortcuts.does.${shortcut}`) }}</dd>
        </template>
      </dl>
    </section>
  </div>
</template>
