/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import InfoBadge from '@/components/badge/InfoBadge.vue'
import MutedText from '@/components/typography/MutedText.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import type {Offer} from '~/api/generated/payload'
import {localized, offerState} from '~/util/session/storeOffers'
import {section} from '~/util/session/sections'

/**
 * One mob of the store: name, id and what it does, where it stands for this session, and installing or updating it
 * when the participant may change mobs.
 */
const props = defineProps<{
  offer: Offer
}>()

const emit = defineEmits<{
  install: []
  update: []
}>()

const session = useEditorSession()
const locks = useEditorLocks()
const {t, locale} = useI18n()
const state = computed(() => session.payload.value ? offerState(props.offer, session.payload.value) : 'available')
const allowed = computed(() => locks.mayChange(section.mob(props.offer.id)))
const action = computed(() => {
  if (!allowed.value) return null
  if (state.value === 'available') return {label: 'editor.store.install', icon: 'download', event: 'install'} as const
  if (state.value === 'update' || state.value === 'changed') return {label: 'editor.store.update', icon: 'arrows-rotate', event: 'update'} as const
  return null
})

function run(event: 'install' | 'update'): void {
  if (event === 'install') emit('install')
  else emit('update')
}
</script>

<template>
  <div class="flex flex-wrap items-center gap-3 rounded-control border border-line-soft px-3 py-2">
    <div class="min-w-0 flex-1">
      <p class="font-medium">{{ localized(offer.names, locale, offer.id) }} <span class="font-mono text-xs text-ink-faint">{{ offer.id }}</span></p>
      <MutedText size="sm" tag="p">{{ localized(offer.descriptions, locale, '') }}</MutedText>
    </div>
    <InfoBadge v-if="state !== 'available'">{{ t(`editor.store.state.${state}`) }}</InfoBadge>
    <SecondaryButton v-if="action" :icon="action.icon" compact @click="run(action.event)">{{ t(action.label) }}</SecondaryButton>
  </div>
</template>
