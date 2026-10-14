/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import type {SetupChoices} from '~/util/session/setupWizard'

/** What finishing the setup will change, one line per step. */
const props = defineProps<{
  choices: SetupChoices
  names: Readonly<Record<string, string>>
  mobs: number
}>()

const {t} = useI18n()
const worlds = computed(() => Object.entries(props.choices.worlds).filter(([, on]) => on).map(([uid]) => props.names[uid] ?? uid))
const lines = computed(() => [
  ...(props.choices.takeOver ? [t('editor.setup.summary.takeOver')] : []),
  worlds.value.length > 0 ? t('editor.setup.summary.worlds', {worlds: worlds.value.join(', ')}) : t('editor.setup.summary.noWorlds'),
  t('editor.setup.summary.frequency', {choice: t(`editor.setup.frequency.${props.choices.frequency}.label`)}),
  t('editor.setup.summary.difficulty', {choice: t(`editor.setup.difficulty.${props.choices.difficulty}.label`)}),
  t('editor.setup.summary.mobs', {count: props.mobs}),
])
</script>

<template>
  <ul class="flex list-disc flex-col gap-1 pl-5">
    <li v-for="line in lines" :key="line">{{ line }}</li>
  </ul>
</template>
