/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import BareButton from '@/components/button/BareButton.vue'
import {SOUND_GROUPS} from '~/util/sounds/soundChoices'

/** Narrows a sound list to one kind of sound, or shows all of them. */
const model = defineModel<string | undefined>({required: true})

const {t} = useI18n()

function classes(active: boolean): string {
  return active ? 'bg-selected text-ink' : 'text-ink-muted hover:bg-raised'
}
</script>

<template>
  <div :aria-label="t('editor.soundPicker.groups')" class="flex flex-wrap gap-1" role="group">
    <BareButton :aria-pressed="model === undefined" :class="classes(model === undefined)" class="rounded-control px-2 py-0.5 text-xs" @click="model = undefined">
      {{ t('editor.soundPicker.all') }}
    </BareButton>
    <BareButton
        v-for="kind in SOUND_GROUPS"
        :key="kind"
        :aria-pressed="model === kind"
        :class="classes(model === kind)"
        class="rounded-control px-2 py-0.5 text-xs"
        @click="model = kind"
    >
      {{ t(`editor.soundPicker.group.${kind}`) }}
    </BareButton>
  </div>
</template>
