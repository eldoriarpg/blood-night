/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import TabBar from '@/components/navigation/TabBar.vue'
import WorldSettings from '~/views/editorview/settings/WorldSettings.vue'
import ServerSettings from '~/views/editorview/settings/ServerSettings.vue'

/** The settings: a second header switching between the worlds' settings and the server's. */
const {t} = useI18n()
const {place} = useEditorNavigation()
const scopes = computed(() => [
  {key: 'world', label: t('editor.settings.world')},
  {key: 'server', label: t('editor.settings.server')},
])
</script>

<template>
  <div class="flex min-h-0 flex-1 flex-col">
    <div class="px-5"><TabBar v-model="place.settings" :tabs="scopes"/></div>
    <WorldSettings v-if="place.settings === 'world'"/>
    <ServerSettings v-else/>
  </div>
</template>
