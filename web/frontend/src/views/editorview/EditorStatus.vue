/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import Spinner from '@/components/feedback/Spinner.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'
import type {EditorStatus} from '~/composables/useEditorSession'

/** What the editor shows while it has no session to edit: loading, gone or failed. */
defineProps<{
  status: EditorStatus
}>()

const {t} = useI18n()
</script>

<template>
  <div class="flex flex-1 items-center justify-center p-8">
    <Spinner v-if="status === 'loading' || status === 'idle'" size="lg"/>
    <EmptyState v-else-if="status === 'gone'" :message="t('editor.status.gone')"/>
    <EmptyState v-else :message="t('editor.status.failed')"/>
  </div>
</template>
