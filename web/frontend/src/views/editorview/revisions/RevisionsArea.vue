/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import PageHeader from '@/components/typography/PageHeader.vue'
import MutedText from '@/components/typography/MutedText.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import ButtonRow from '@/components/button/ButtonRow.vue'
import Spinner from '@/components/feedback/Spinner.vue'
import RevisionDialog from '~/views/editorview/revisions/RevisionDialog.vue'
import type {RevisionInfo} from '~/api/generated/schema'
import {failure} from '~/api/editor'
import {showToast} from '~/util/toast'

/**
 * Every revision of the session with who saved it and when; any of them can be looked at and restored, restoring
 * adding a revision so nothing is lost. Restoring replaces everything, so it needs every section free.
 */
const session = useEditorSession()
const {t, locale} = useI18n()
const revisions = ref<RevisionInfo[] | null>(null)
const viewing = ref<number | null>(null)

async function load(): Promise<void> {
  try {
    revisions.value = [...await session.api().revisions()].reverse()
  } catch (error) {
    showToast(failure(error).message, 'error')
  }
}

watch(() => session.data.value.revision, () => void load(), {immediate: true})

function when(revision: RevisionInfo): string {
  return new Date(revision.created).toLocaleString(locale.value)
}

async function restore(number: number): Promise<void> {
  try {
    await session.api().restore(number)
    showToast(t('editor.revisions.restored', {number}), 'success')
  } catch (error) {
    const refused = failure(error)
    showToast(refused.status === 423 ? t('editor.changes.locked', {reason: refused.message}) : refused.message, 'error')
  }
}
</script>

<template>
  <main class="min-h-0 flex-1 overflow-y-auto px-6 py-5">
    <div class="max-w-4xl">
      <PageHeader>{{ t('editor.areas.revisions') }}</PageHeader>
      <MutedText size="sm" tag="p">{{ t('editor.revisions.hint') }}</MutedText>
      <Spinner v-if="revisions === null" class="mt-6"/>
      <ol v-else class="mt-4 flex flex-col">
        <li v-for="revision in revisions" :key="revision.revision" class="flex flex-wrap items-center gap-3 border-b border-line-soft py-3 text-sm">
          <span class="w-16 font-mono">{{ t('editor.revisions.number', {number: revision.revision}) }}</span>
          <span class="w-28 text-held">{{ revision.author ?? t('editor.revisions.server') }}</span>
          <span class="flex-1 text-ink-muted">{{ revision.revision === 0 ? t('editor.revisions.opened') : t('editor.revisions.changes', {count: revision.operations}) }}</span>
          <span class="text-ink-faint">{{ when(revision) }}</span>
          <ButtonRow>
            <SecondaryButton compact @click="viewing = revision.revision">{{ t('editor.revisions.view') }}</SecondaryButton>
            <SecondaryButton :disabled="revision.revision === session.data.value.revision" compact @click="restore(revision.revision)">{{ t('editor.revisions.restore') }}</SecondaryButton>
          </ButtonRow>
        </li>
      </ol>
    </div>
    <RevisionDialog :number="viewing" @close="viewing = null"/>
  </main>
</template>
