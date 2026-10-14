/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import type {SetupChoices} from '~/util/session/setupWizard'

/** Which worlds have blood nights, one switch per world. */
const choices = defineModel<SetupChoices>({required: true})

defineProps<{
  /** The worlds' names by UID. */
  names: Readonly<Record<string, string>>
}>()
</script>

<template>
  <div class="flex flex-col gap-3">
    <ToggleSetting
        v-for="(name, uid) in names"
        :key="uid"
        :model-value="choices.worlds[uid] ?? false"
        :label="name"
        @update:model-value="choices.worlds[uid] = $event"
    />
  </div>
</template>
