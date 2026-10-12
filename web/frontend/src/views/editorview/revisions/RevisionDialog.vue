/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import Modal from '@/components/feedback/Modal.vue'
import SubHeader from '@/components/typography/SubHeader.vue'
import Spinner from '@/components/feedback/Spinner.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import type {EditorPayload} from '~/api/generated/payload'
import {failure} from '~/api/editor'
import {showToast} from '~/util/toast'

/** The mobs, macros and worlds of a revision, to compare it with the session before restoring it. */
const props = defineProps<{
  number: number | null
}>()

const emit = defineEmits<{
  close: []
}>()

const session = useEditorSession()
const {t} = useI18n()
const payload = ref<EditorPayload | null>(null)
const open = computed({get: () => props.number !== null, set: value => value || emit('close')})

watch(() => props.number, async number => {
  payload.value = null
  if (number === null) return
  try {
    payload.value = await session.api().revision(number)
  } catch (error) {
    showToast(failure(error).message, 'error')
  }
})

const summary = computed(() => payload.value ? [
  {label: t('editor.areas.mobs'), entries: Object.keys(payload.value.mobs ?? {}).sort()},
  {label: t('editor.areas.macros'), entries: (payload.value.macros ?? []).map(macro => macro.id)},
  {label: t('editor.settings.worlds'), entries: Object.values(payload.value.settings?.worlds ?? {}).map(world => world.name)},
] : [])
</script>

<template>
  <Modal v-model="open" size="lg">
    <div class="space-y-4">
      <SubHeader>{{ t('editor.revisions.number', {number: number ?? 0}) }}</SubHeader>
      <Spinner v-if="!payload"/>
      <dl v-else class="space-y-3 text-sm">
        <div v-for="part in summary" :key="part.label">
          <dt class="text-ink-faint">{{ part.label }}</dt>
          <dd class="font-mono">{{ part.entries.join(', ') || '-' }}</dd>
        </div>
      </dl>
      <div class="flex justify-end"><SecondaryButton data-cancel @click="emit('close')">{{ t('common.close') }}</SecondaryButton></div>
    </div>
  </Modal>
</template>
