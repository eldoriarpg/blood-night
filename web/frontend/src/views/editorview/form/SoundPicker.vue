/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import BareButton from '@/components/button/BareButton.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import Popover from '@/components/feedback/Popover.vue'
import SearchInput from '@/components/input/text/SearchInput.vue'
import MutedText from '@/components/typography/MutedText.vue'
import {BORDERED_INPUT_CLASSES} from '@/components/input/inputClasses'
import SoundGroups from '~/views/editorview/form/SoundGroups.vue'
import SoundList from '~/views/editorview/form/SoundList.vue'
import {searchSounds, type SoundChoice} from '~/util/sounds/soundChoices'

/**
 * Picks one of Minecraft's sounds by what it sounds like: a field showing the chosen sound's name in the editor's
 * language with a button that plays it, opening a searchable list that can be narrowed to one kind of sound. Every
 * sound in the list can be played before it is picked; arrow keys and enter pick.
 */
const model = defineModel<string>({required: true})

defineProps<{
  label: string
  readOnly?: boolean
}>()

const {t} = useI18n()
const sounds = useSoundAssets()
const open = ref(false)
const group = ref<string | undefined>(undefined)
const LIMIT = 150

const {query, active, matches, shown, step} = usePickerSearch(
  open, query => searchSounds(sounds.choices.value, query, group.value), LIMIT, choice => choice.key === model.value, [group])

onMounted(() => {
  void sounds.load()
})

function pick(choice: SoundChoice | undefined): void {
  if (!choice) return
  model.value = choice.key
  open.value = false
}
</script>

<template>
  <div class="flex items-center gap-1">
    <Popover v-model:open="open" :label="label" align="start" class="min-w-0 flex-1" panel-class="flex w-[28rem] max-w-[90vw] flex-col gap-2 p-2" role="dialog">
      <template #trigger="{toggle, triggerAttrs}">
        <BareButton
            :aria-label="label"
            :class="BORDERED_INPUT_CLASSES"
            :disabled="readOnly"
            class="flex w-full items-center gap-2 text-left"
            v-bind="triggerAttrs"
            @click="toggle"
        >
          <span class="min-w-0 flex-1 truncate">{{ sounds.name(model) }}</span>
          <span class="truncate font-mono text-xs text-ink-faint">{{ model }}</span>
        </BareButton>
      </template>
      <SearchInput
          v-model="query"
          :aria-label="t('editor.soundPicker.search')"
          :placeholder="t('editor.soundPicker.search')"
          @keydown.down.prevent="step(1)"
          @keydown.enter.prevent="pick(shown[active])"
          @keydown.up.prevent="step(-1)"
      />
      <SoundGroups v-model="group"/>
      <MutedText v-if="sounds.status.value === 'loading'" class="px-2" size="sm" tag="p">{{ t('editor.soundPicker.loading') }}</MutedText>
      <MutedText v-else-if="sounds.status.value === 'failed'" class="px-2" size="sm" tag="p">{{ t('editor.soundPicker.failed') }}</MutedText>
      <SoundList
          v-model:active="active"
          :found="matches.length"
          :label="label"
          :ready="sounds.status.value === 'ready'"
          :selected="model"
          :shown="shown"
          @pick="pick"
          @play="choice => sounds.play(choice.key)"
      />
    </Popover>
    <MutedIconButton :disabled="!sounds.playable(model)" :label="t('editor.soundPicker.play', {name: sounds.name(model)})" icon="play" @click="sounds.play(model)"/>
  </div>
</template>
