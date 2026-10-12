/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import IconButton from '@/components/button/IconButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import CountBadge from '@/components/badge/CountBadge.vue'

/** The canvas's floating toolbar: zoom, fit, and the problems of the session. */
defineProps<{
  problemCount: number
  readOnly: boolean
}>()

const emit = defineEmits<{
  zoomIn: []
  zoomOut: []
  fit: []
  arrange: []
  problems: []
}>()

const {t} = useI18n()
</script>

<template>
  <div :aria-label="t('editor.canvas.toolbar')" class="absolute bottom-4 left-1/2 flex -translate-x-1/2 items-center gap-1 rounded-panel border border-line bg-surface p-1" role="toolbar">
    <IconButton :label="t('editor.canvas.zoomOut')" icon="minus" @click="emit('zoomOut')"/>
    <IconButton :label="t('editor.canvas.zoomIn')" icon="plus" @click="emit('zoomIn')"/>
    <IconButton :label="t('editor.canvas.fit')" icon="expand" @click="emit('fit')"/>
    <IconButton :disabled="readOnly" :label="t('editor.canvas.arrange')" icon="diagram-project" @click="emit('arrange')"/>
    <span aria-hidden="true" class="mx-1 h-6 w-px bg-line"/>
    <SecondaryButton compact @click="emit('problems')">
      {{ t('editor.canvas.problems') }}
      <CountBadge v-if="problemCount > 0" :count="problemCount"/>
    </SecondaryButton>
  </div>
</template>
