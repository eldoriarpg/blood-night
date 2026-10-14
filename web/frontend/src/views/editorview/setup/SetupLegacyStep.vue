/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import MutedText from '@/components/typography/MutedText.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import SetupOption from '~/views/editorview/setup/SetupOption.vue'

/** Whether the old BloodNight's settings, worlds, mobs and items are taken over, and what cannot be. */
const takeOver = defineModel<boolean>({required: true})

defineProps<{
  notes: readonly string[]
}>()

const {t} = useI18n()
</script>

<template>
  <div class="flex flex-col gap-3">
    <MutedText tag="p">{{ t('editor.setup.legacy.found') }}</MutedText>
    <SetupOption :hint="t('editor.setup.legacy.takeOverHint')" :label="t('editor.setup.legacy.takeOver')" :picked="takeOver" @click="takeOver = true"/>
    <SetupOption :hint="t('editor.setup.legacy.freshHint')" :label="t('editor.setup.legacy.fresh')" :picked="!takeOver" @click="takeOver = false"/>
    <template v-if="takeOver && notes.length > 0">
      <SectionLabel>{{ t('editor.setup.legacy.notes') }}</SectionLabel>
      <ul class="list-disc pl-5 text-sm">
        <li v-for="note in notes" :key="note">{{ note }}</li>
      </ul>
    </template>
  </div>
</template>
