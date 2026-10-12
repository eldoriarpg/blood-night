/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import TabBar from '@/components/navigation/TabBar.vue'
import PageHeader from '@/components/typography/PageHeader.vue'
import Alert from '@/components/feedback/Alert.vue'
import SettingsForm from '~/views/editorview/settings/SettingsForm.vue'
import {section} from '~/util/session/sections'
import {pointer} from '~/util/patch/jsonPointer'
import {SERVER_SECTIONS} from '~/util/settings/settingsSections'

/** The server's settings: messages, the editor's address and the global drops, one tab each. */
const session = useEditorSession()
const locks = useEditorLocks()
const {t} = useI18n()
const tab = ref(SERVER_SECTIONS[0]?.key ?? 'messages')

const tabs = computed(() => SERVER_SECTIONS.map(entry => ({key: entry.key, label: t(`editor.settings.serverSections.${entry.key}`)})))
const current = computed(() => SERVER_SECTIONS.find(entry => entry.key === tab.value) ?? SERVER_SECTIONS[0])
const sectionName = computed(() => current.value?.root === 'main' ? section.main : section.globalDrops)
const heldBy = computed(() => locks.heldByOther(sectionName.value))
const value = computed(() => session.payload.value?.settings?.[current.value?.root ?? 'main'] as Record<string, unknown> | undefined)

watch(sectionName, (now, before) => {
  if (before) void locks.release(before)
  void locks.take(now)
}, {immediate: true})

onBeforeUnmount(() => void locks.release(sectionName.value))
</script>

<template>
  <main v-if="current" class="min-h-0 flex-1 overflow-y-auto px-6 py-5">
    <PageHeader>{{ t('editor.settings.server') }}</PageHeader>
    <Alert v-if="heldBy" class="mt-3" variant="held">{{ t('editor.settings.heldBy', {name: heldBy}) }}</Alert>
    <TabBar v-model="tab" :tabs="tabs" class="mt-3"/>
    <SettingsForm
        :definition="current.root === 'main' ? 'ConfigFile' : 'GlobalDrops'"
        :path="pointer('settings', current.root)"
        :properties="current.properties"
        :read-only="heldBy !== null"
        :value="value"
    />
  </main>
</template>
