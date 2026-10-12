/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import SectionHeader from '@/components/typography/SectionHeader.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import StyleSwatch from '@/views/styleview/StyleSwatch.vue'

/**
 * The colours of the palette: the surfaces, the text, the states, the node kinds and the pin types.
 * Each class is written out in full, because Tailwind only generates the classes it finds spelled
 * in a source file.
 */
interface SwatchGroup {
  label: string
  round: boolean
  swatches: [name: string, colorClass: string][]
}

const GROUPS: SwatchGroup[] = [
  {label: 'Surfaces', round: false, swatches: [
    ['ground', 'bg-ground'], ['surface', 'bg-surface'], ['raised', 'bg-raised'], ['hover', 'bg-hover'],
    ['selected', 'bg-selected'], ['line', 'bg-line'], ['line-soft', 'bg-line-soft'],
  ]},
  {label: 'Text', round: false, swatches: [['ink', 'bg-ink'], ['ink-muted', 'bg-ink-muted'], ['ink-faint', 'bg-ink-faint']]},
  {label: 'States', round: false, swatches: [
    ['accent', 'bg-accent'], ['accent-hover', 'bg-accent-hover'], ['accent-text', 'bg-accent-text'], ['focus', 'bg-focus'],
    ['held', 'bg-held'], ['held-bg', 'bg-held-bg'], ['problem', 'bg-problem'], ['problem-bg', 'bg-problem-bg'],
  ]},
  {label: 'Node kinds', round: false, swatches: [
    ['event', 'bg-node-event'], ['action', 'bg-node-action'], ['value', 'bg-node-value'], ['flow', 'bg-node-flow'],
    ['macro', 'bg-node-macro'],
  ]},
  {label: 'Pin types', round: true, swatches: [
    ['exec', 'bg-pin-exec'], ['entity', 'bg-pin-entity'], ['number', 'bg-pin-number'], ['boolean', 'bg-pin-boolean'],
    ['text', 'bg-pin-text'], ['location', 'bg-pin-location'], ['item', 'bg-pin-item'], ['other', 'bg-pin-other'],
  ]},
]
</script>

<template>
  <section class="space-y-4">
    <SectionHeader>Palette</SectionHeader>
    <div v-for="group in GROUPS" :key="group.label" class="space-y-2">
      <SectionLabel>{{ group.label }}</SectionLabel>
      <div class="flex flex-wrap gap-x-5 gap-y-2">
        <StyleSwatch
            v-for="[name, colorClass] in group.swatches"
            :key="name"
            :name="name"
            :color-class="colorClass"
            :round="group.round"
        />
      </div>
    </div>
  </section>
</template>
