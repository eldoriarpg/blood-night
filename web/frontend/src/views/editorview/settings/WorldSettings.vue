/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import InfoBadge from '@/components/badge/InfoBadge.vue'
import BareButton from '@/components/button/BareButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import TabBar from '@/components/navigation/TabBar.vue'
import DocsLink from '@/components/navigation/DocsLink.vue'
import {sectionDocsLink} from '~/util/docsLinks'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import PageHeader from '@/components/typography/PageHeader.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'
import Alert from '@/components/feedback/Alert.vue'
import SettingsForm from '~/views/editorview/settings/SettingsForm.vue'
import CopySettingsDialog from '~/views/editorview/settings/CopySettingsDialog.vue'
import {section} from '~/util/session/sections'
import {pointer} from '~/util/patch/jsonPointer'
import {WORLD_SECTIONS} from '~/util/settings/settingsSections'
import {WORLD_KIND, worldSectionShows, type WorldKind} from '~/util/settings/worldKind'

/**
 * A world's settings: the worlds in a sidebar, the picked world's sections as tabs, and copying its settings to other
 * worlds. A world without blood nights shows only its general section, since nothing else of it is used. Opening a
 * world takes its section; a world someone else holds is shown read only.
 */
const session = useEditorSession()
const locks = useEditorLocks()
const {t} = useI18n()
const picked = ref('')
const tab = ref(WORLD_SECTIONS[0]?.key ?? 'general')
const copying = ref(false)
const copySections = ref<string[]>([])

const worlds = computed(() => Object.entries(session.payload.value?.settings?.worlds ?? {})
  .map(([uid, world]) => ({
    uid,
    name: world.name,
    enabled: (world.settings as {enabled?: boolean} | undefined)?.enabled !== false,
    kind: {dayCycle: world.dayCycle !== false, environment: world.environment ?? 'NORMAL'},
  }))
  .sort((a, b) => a.name.localeCompare(b.name)))
const world = computed(() => worlds.value.find(candidate => candidate.uid === picked.value) ?? worlds.value[0])
const heldBy = computed(() => world.value ? locks.heldByOther(section.world(world.value.uid)) : null)
const denied = computed(() => !locks.mayChange(world.value ? section.world(world.value.uid) : ''))
const kind = computed<WorldKind | undefined>(() => world.value?.kind)
provide(WORLD_KIND, kind)
const sections = computed(() => world.value?.enabled === false
  ? WORLD_SECTIONS.slice(0, 1)
  : WORLD_SECTIONS.filter(entry => worldSectionShows(entry.key, kind.value)))
const tabs = computed(() => sections.value.map(entry => ({key: entry.key, label: t(`editor.settings.sections.${entry.key}`)})))
const current = computed(() => sections.value.find(entry => entry.key === tab.value) ?? sections.value[0])
const value = computed(() => world.value ? session.payload.value?.settings?.worlds?.[world.value.uid]?.settings as Record<string, unknown> | undefined : undefined)

watch(() => world.value?.uid, (now, before) => {
  if (before) void locks.release(section.world(before))
  if (now) void locks.take(section.world(now))
}, {immediate: true})

onBeforeUnmount(() => {
  if (world.value) void locks.release(section.world(world.value.uid))
})

function copy(sections: string[]): void {
  copySections.value = sections
  copying.value = true
}
</script>

<template>
  <div class="flex min-h-0 flex-1">
    <nav :aria-label="t('editor.settings.worlds')" class="flex w-60 shrink-0 flex-col gap-1 overflow-y-auto border-r border-line-soft bg-surface p-3">
      <SectionLabel class="px-2 pb-1">{{ t('editor.settings.worlds') }}</SectionLabel>
      <BareButton
          v-for="entry in worlds"
          :key="entry.uid"
          :class="entry.uid === world?.uid ? 'bg-selected' : 'hover:bg-raised'"
          class="flex min-h-10 items-center gap-2 rounded-control px-2 text-left"
          @click="picked = entry.uid"
      >
        <span class="min-w-0 flex-1">
          <span class="block truncate font-mono text-sm">{{ entry.name }}</span>
          <span class="text-xs text-ink-faint">{{ entry.enabled ? t('editor.settings.on') : t('editor.settings.off') }}</span>
        </span>
        <InfoBadge v-if="locks.heldByOther(`settings/worlds/${entry.uid}`)">{{ locks.heldByOther(`settings/worlds/${entry.uid}`) }}</InfoBadge>
      </BareButton>
    </nav>
    <main v-if="world && current" class="min-w-0 flex-1 overflow-y-auto px-6 py-5">
      <div class="flex flex-wrap items-center gap-3">
        <PageHeader class="flex-1 font-mono">{{ world.name }}</PageHeader>
        <SecondaryButton icon="copy" @click="copy(WORLD_SECTIONS.map(entry => entry.key))">{{ t('editor.copy.open') }}</SecondaryButton>
      </div>
      <Alert v-if="heldBy" class="mt-3" variant="held">{{ t('editor.settings.heldBy', {name: heldBy}) }}</Alert>
      <Alert v-else-if="denied" class="mt-3" variant="held">{{ t('editor.permission.readOnly') }}</Alert>
      <TabBar v-model="tab" :tabs="tabs" class="mt-3"/>
      <div class="flex items-center justify-between gap-3 pt-2">
        <DocsLink v-if="sectionDocsLink(current.key, false)" :href="sectionDocsLink(current.key, false)!">{{ t('editor.docs.section') }}</DocsLink>
        <SecondaryButton class="ml-auto" compact @click="copy([tab])">{{ t('editor.copy.section') }}</SecondaryButton>
      </div>
      <SettingsForm
          :definition="'WorldSettings'"
          :path="pointer('settings', 'worlds', world.uid, 'settings')"
          :properties="current.properties"
          :read-only="heldBy !== null || denied"
          :value="value"
      />
    </main>
    <EmptyState v-else :message="t('editor.settings.noWorlds')" class="flex-1"/>
    <CopySettingsDialog v-if="world" v-model="copying" :from="world.uid" :sections="copySections"/>
  </div>
</template>
