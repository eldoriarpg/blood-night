/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import BareButton from '@/components/button/BareButton.vue'
import MutedText from '@/components/typography/MutedText.vue'
import SectionHeader from '@/components/typography/SectionHeader.vue'
import type {EffectUse} from '~/util/particles/particleEffects'
import {mobLabel} from '~/util/mobs/mobName'

/**
 * Where an effect is used: the mobs that wear it or play it and the macros that play it, each a link there.
 */
defineProps<{
  uses: readonly EffectUse[]
}>()

const session = useEditorSession()
const {openMob} = useEditorNavigation()
const {t, locale} = useI18n()

function label(use: EffectUse): string {
  if (use.kind === 'macro') return t('editor.particles.usedBy.macro', {name: use.id})
  const name = mobLabel(session.payload.value?.mobs?.[use.id]?.attributes?.name, locale.value) || use.id
  return t(use.path.includes('/particles/') ? 'editor.particles.usedBy.worn' : 'editor.particles.usedBy.played', {name})
}

function open(use: EffectUse): void {
  if (use.kind === 'mob') openMob(use.id)
}
</script>

<template>
  <section :aria-label="t('editor.particles.usedBy.title')" class="flex flex-col gap-2">
    <SectionHeader>{{ t('editor.particles.usedBy.title') }}</SectionHeader>
    <MutedText v-if="uses.length === 0" size="sm" tag="p">{{ t('editor.particles.usedBy.none') }}</MutedText>
    <ul class="flex flex-col gap-1">
      <li v-for="use in uses" :key="use.path">
        <BareButton v-if="use.kind === 'mob'" class="text-sm text-accent hover:underline" @click="open(use)">{{ label(use) }}</BareButton>
        <span v-else class="text-sm">{{ label(use) }}</span>
      </li>
    </ul>
  </section>
</template>
