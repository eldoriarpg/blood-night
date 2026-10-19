/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import DocsLink from '@/components/navigation/DocsLink.vue'
import {DOCS_PAGES, DOCS_URL} from '~/util/docsLinks'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'
import BareButton from '@/components/button/BareButton.vue'
import type {PayloadProblem} from '~/api/generated/schema'

/** What the editor service found wrong with the session, by where it is; picking one opens it. */
defineProps<{
  problems: readonly PayloadProblem[]
}>()

const emit = defineEmits<{
  close: []
  /** A problem was picked, by its path. */
  open: [path: string]
}>()

const {t} = useI18n()
</script>

<template>
  <section :aria-label="t('editor.canvas.problems')" class="absolute bottom-20 left-1/2 flex max-h-72 w-[36rem] max-w-[calc(100%-2rem)] -translate-x-1/2 flex-col gap-2 rounded-panel border border-line bg-surface p-3">
    <div class="flex items-center justify-between">
      <SectionLabel>{{ t('editor.canvas.problems') }}</SectionLabel>
      <DocsLink :href="`${DOCS_URL}${DOCS_PAGES.problems}`" class="ml-auto mr-2">{{ t('editor.docs.problems') }}</DocsLink>
      <MutedIconButton :label="t('editor.palette.close')" icon="xmark" @click="emit('close')"/>
    </div>
    <EmptyState v-if="problems.length === 0" compact :message="t('editor.problems.none')"/>
    <ul v-else class="flex flex-col gap-1 overflow-y-auto text-sm">
      <li v-for="problem in problems" :key="problem.path + problem.message">
        <BareButton class="flex w-full gap-3 rounded-control text-left hover:bg-raised" @click="emit('open', problem.path)">
          <span class="shrink-0 font-mono text-xs text-problem">{{ problem.path }}</span>
          <span>{{ problem.message }}</span>
        </BareButton>
      </li>
    </ul>
  </section>
</template>
