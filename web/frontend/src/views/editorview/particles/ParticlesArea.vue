/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import InfoBadge from '@/components/badge/InfoBadge.vue'
import BareButton from '@/components/button/BareButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import SearchInput from '@/components/input/text/SearchInput.vue'
import EffectForm from '~/views/editorview/particles/EffectForm.vue'
import NewEffectDialog from '~/views/editorview/particles/NewEffectDialog.vue'
import type {ParticleEffect} from '~/api/generated/payload'
import {section} from '~/util/session/sections'
import {effectEntries, type EffectEntry} from '~/util/particles/particleEffects'

/**
 * The particle effects graphs play and mobs wear: the server's own and the built-in ones in a sidebar, searchable by
 * name, and the picked one's form. Opening an own effect takes its section; one someone else holds is shown read only.
 */
const session = useEditorSession()
const locks = useEditorLocks()
const {place, openEffect} = useEditorNavigation()
const {t} = useI18n()
const creating = ref(false)
const copying = ref<ParticleEffect | undefined>(undefined)
const query = ref('')

const effects = computed(() => effectEntries(session.payload.value))
const found = computed(() => {
  const words = query.value.toLowerCase().split(/\s+/).filter(word => word !== '')
  return effects.value.filter(candidate => words.every(word => candidate.id.includes(word) || (candidate.effect.name ?? '').toLowerCase().includes(word)))
})
const groups = computed(() => [
  {key: 'own', label: t('editor.particles.own'), entries: found.value.filter(entry => !entry.builtIn)},
  {key: 'builtIn', label: t('editor.particles.builtInGroup'), entries: found.value.filter(entry => entry.builtIn)},
])
const entry = computed<EffectEntry | undefined>(() => effects.value.find(candidate => candidate.id === place.value.particleEffect) ?? effects.value[0])

watch(() => entry.value, (now, before) => {
  if (before && !before.builtIn) void locks.release(section.particleEffect(before.id))
  if (now && !now.builtIn) void locks.take(section.particleEffect(now.id))
}, {immediate: true})

onBeforeUnmount(() => {
  if (entry.value && !entry.value.builtIn) void locks.release(section.particleEffect(entry.value.id))
})

function create(from?: ParticleEffect): void {
  copying.value = from
  creating.value = true
}
</script>

<template>
  <div class="flex min-h-0 flex-1">
    <nav :aria-label="t('editor.particles.list')" class="flex w-64 shrink-0 flex-col gap-1 overflow-y-auto border-r border-line-soft bg-surface p-3">
      <SearchInput v-model="query" :aria-label="t('editor.particles.search')" :placeholder="t('editor.particles.search')" class="mb-1"/>
      <template v-for="group in groups" :key="group.key">
        <SectionLabel class="px-2 pb-1 pt-2">{{ group.label }}</SectionLabel>
        <BareButton
            v-for="candidate in group.entries"
            :key="candidate.id"
            :class="candidate.id === entry?.id ? 'bg-selected' : 'hover:bg-raised'"
            class="flex min-h-10 items-center gap-2 rounded-control px-2 text-left"
            @click="openEffect(candidate.id)"
        >
          <span class="min-w-0 flex-1 truncate text-sm">{{ candidate.effect.name || candidate.id }}</span>
          <InfoBadge v-if="!candidate.builtIn && locks.heldByOther(section.particleEffect(candidate.id))">{{ locks.heldByOther(section.particleEffect(candidate.id)) }}</InfoBadge>
        </BareButton>
      </template>
      <SecondaryButton class="mt-2" compact icon="plus" @click="create()">{{ t('editor.particles.new') }}</SecondaryButton>
    </nav>
    <EffectForm v-if="entry" :key="entry.id" :entry="entry" class="min-w-0 flex-1" @copy="create" @deleted="openEffect('')"/>
    <NewEffectDialog v-model="creating" :from="copying" @created="openEffect"/>
  </div>
</template>
