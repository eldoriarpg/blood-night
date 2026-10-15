/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import TabBar from '@/components/navigation/TabBar.vue'
import DocsLink from '@/components/navigation/DocsLink.vue'
import {sectionDocsLink} from '~/util/docsLinks'
import PageHeader from '@/components/typography/PageHeader.vue'
import Alert from '@/components/feedback/Alert.vue'
import SettingsForm from '~/views/editorview/settings/SettingsForm.vue'
import {section} from '~/util/session/sections'
import {pointer} from '~/util/patch/jsonPointer'
import {SERVER_SECTIONS} from '~/util/settings/settingsSections'

/** The server's settings: messages, the editor's address, loot and more, one tab each. */
const session = useEditorSession()
const locks = useEditorLocks()
const {t} = useI18n()
const {place} = useEditorNavigation()
const tab = computed({get: () => place.value.serverTab || (SERVER_SECTIONS[0]?.key ?? 'messages'), set: key => place.value.serverTab = key})

const tabs = computed(() => SERVER_SECTIONS.map(entry => ({key: entry.key, label: t(`editor.settings.serverSections.${entry.key}`)})))
const current = computed(() => SERVER_SECTIONS.find(entry => entry.key === tab.value) ?? SERVER_SECTIONS[0])
const heldBy = computed(() => locks.heldByOther(section.main))
const denied = computed(() => !locks.mayChange(section.main))
const value = computed(() => session.payload.value?.settings?.main as Record<string, unknown> | undefined)

onMounted(() => void locks.take(section.main))
onBeforeUnmount(() => void locks.release(section.main))
</script>

<template>
  <main v-if="current" class="min-h-0 flex-1 overflow-y-auto px-6 py-5">
    <PageHeader>{{ t('editor.settings.server') }}</PageHeader>
    <Alert v-if="heldBy" class="mt-3" variant="held">{{ t('editor.settings.heldBy', {name: heldBy}) }}</Alert>
    <Alert v-else-if="denied" class="mt-3" variant="held">{{ t('editor.permission.readOnly') }}</Alert>
    <TabBar v-model="tab" :tabs="tabs" class="mt-3"/>
    <div v-if="sectionDocsLink(current.key, true)" class="pt-2">
      <DocsLink :href="sectionDocsLink(current.key, true)!">{{ t('editor.docs.section') }}</DocsLink>
    </div>
    <SettingsForm
        definition="ConfigFile"
        :path="pointer('settings', 'main')"
        :properties="current.properties"
        :read-only="heldBy !== null || denied"
        :value="value"
    />
  </main>
</template>
