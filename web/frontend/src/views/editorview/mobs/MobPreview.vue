/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import PrimaryButton from '@/components/button/PrimaryButton.vue'
import GraphCanvas from '~/views/editorview/workspace/GraphCanvas.vue'
import type {WorkspaceEntry} from '~/views/editorview/workspace/workspaceEntries'
import {mobBindings} from '~/util/graph/graphBindings'
import {withRoutines} from '~/util/graph/routineCalls'

/**
 * The open mob's behaviour as a read-only graph, with an Edit button over it that opens the full graph editor.
 */
const props = defineProps<{
  entry: WorkspaceEntry
}>()

const emit = defineEmits<{
  edit: []
}>()

const session = useEditorSession()
const {t} = useI18n()
const bindings = computed(() => mobBindings(session.payload.value, props.entry.id))
const catalog = computed(() => session.catalog.value ? withRoutines(session.catalog.value, props.entry.graph) : undefined)
</script>

<template>
  <section :aria-label="t('editor.mobs.preview')" class="relative min-w-0 flex-1 overflow-hidden">
    <GraphCanvas
        v-if="catalog"
        :key="entry.id"
        :base="entry.base"
        :bindings="bindings"
        :catalog="catalog"
        :graph="entry.graph"
        :held-by="null"
        :in-macro="false"
        :macros="session.payload.value?.macros ?? []"
        :problem-count="0"
        read-only
    />
    <div class="absolute right-4 top-4"><PrimaryButton icon="pen" @click="emit('edit')">{{ t('editor.mobs.edit') }}</PrimaryButton></div>
  </section>
</template>
