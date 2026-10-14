/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts" generic="T extends string">
import SetupOption from '~/views/editorview/setup/SetupOption.vue'

/** A step with one answer out of a few presets, each named and explained under `editor.setup.<step>.<preset>`. */
const model = defineModel<T>({required: true})

defineProps<{
  step: 'frequency' | 'difficulty'
  presets: readonly T[]
}>()

const {t} = useI18n()
</script>

<template>
  <div class="flex flex-col gap-2">
    <SetupOption
        v-for="preset in presets"
        :key="preset"
        :hint="t(`editor.setup.${step}.${preset}.hint`)"
        :label="t(`editor.setup.${step}.${preset}.label`)"
        :picked="model === preset"
        @click="model = preset"
    />
  </div>
</template>
