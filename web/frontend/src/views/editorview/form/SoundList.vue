/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import BareButton from '@/components/button/BareButton.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import MutedText from '@/components/typography/MutedText.vue'
import type {SoundChoice} from '~/util/sounds/soundChoices'

/**
 * The sounds a search found, each picked with a click and played with its own button; tells when there are more than
 * it shows, or none at all.
 */
const active = defineModel<number>('active', {required: true})

defineProps<{
  label: string
  shown: readonly SoundChoice[]
  /** How many sounds the search found, shown or not. */
  found: number
  selected: string
  ready: boolean
}>()

const emit = defineEmits<{
  pick: [choice: SoundChoice]
  play: [choice: SoundChoice]
}>()

const {t} = useI18n()
</script>

<template>
  <div :aria-label="label" class="flex max-h-80 flex-col overflow-y-auto" role="listbox">
    <div
        v-for="(choice, index) in shown"
        :key="choice.key"
        :class="index === active ? 'bg-selected' : ''"
        class="flex min-h-9 items-center gap-1 rounded-control pr-1 hover:bg-raised"
    >
      <BareButton
          :aria-selected="choice.key === selected"
          class="flex min-w-0 flex-1 items-center gap-2 px-2 py-1 text-left text-sm"
          role="option"
          @click="emit('pick', choice)"
          @focus="active = index"
          @mouseenter="active = index"
      >
        <span class="min-w-0 flex-1 truncate">{{ choice.name }}</span>
        <span class="max-w-40 shrink-0 truncate font-mono text-xs text-ink-faint">{{ choice.key.replace('minecraft:', '') }}</span>
      </BareButton>
      <MutedIconButton v-if="choice.playable" :label="t('editor.soundPicker.play', {name: choice.name})" icon="play" @click="emit('play', choice)"/>
    </div>
    <MutedText v-if="found === 0 && ready" class="px-2 py-1" size="sm" tag="p">{{ t('editor.soundPicker.noMatch') }}</MutedText>
    <MutedText v-else-if="found > shown.length" class="px-2 py-1" size="sm" tag="p">{{ t('editor.soundPicker.more', {count: found - shown.length}) }}</MutedText>
  </div>
</template>
