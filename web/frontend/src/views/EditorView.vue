/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import EditorHeader from '~/views/editorview/EditorHeader.vue'
import WorkspaceArea from '~/views/editorview/workspace/WorkspaceArea.vue'
import MobsArea from '~/views/editorview/mobs/MobsArea.vue'
import ItemsArea from '~/views/editorview/items/ItemsArea.vue'
import SettingsArea from '~/views/editorview/settings/SettingsArea.vue'
import RevisionsArea from '~/views/editorview/revisions/RevisionsArea.vue'
import EditorStatus from '~/views/editorview/EditorStatus.vue'
import SetupWizard from '~/views/editorview/setup/SetupWizard.vue'
import ToastContainer from '@/components/feedback/ToastContainer.vue'
import type {EditorArea} from '~/views/editorview/editorArea'

/**
 * The editor of one session: the header with the areas, and the area picked. The participant's token comes from the
 * personal link's fragment, which browsers never send to a server.
 */
const route = useRoute()
const session = useEditorSession()
const {t} = useI18n()
const area = ref<EditorArea>('mobs')
const leftSetup = ref(false)
const setup = computed(() => !leftSetup.value && session.payload.value?.setup === true ? session.payload.value : null)

useHead({title: () => t('editor.title')})

onMounted(() => {
  const token = decodeURIComponent(window.location.hash.slice(1))
  void session.open(String(route.params.id ?? ''), token)
})

onBeforeUnmount(() => session.close())
</script>

<template>
  <div class="flex h-screen flex-col bg-ground text-ink">
    <EditorHeader v-model="area"/>
    <EditorStatus v-if="session.data.value.status !== 'ready'" :status="session.data.value.status"/>
    <SetupWizard v-else-if="setup" :payload="setup" @leave="leftSetup = true"/>
    <template v-else>
      <MobsArea v-if="area === 'mobs'"/>
      <WorkspaceArea v-else-if="area === 'macros'" kind="macros"/>
      <ItemsArea v-else-if="area === 'items'"/>
      <SettingsArea v-else-if="area === 'settings'"/>
      <RevisionsArea v-else/>
    </template>
    <ToastContainer/>
  </div>
</template>
