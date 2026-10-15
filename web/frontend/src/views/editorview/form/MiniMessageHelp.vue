/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import BareButton from '@/components/button/BareButton.vue'
import AppIcon from '@/components/display/AppIcon.vue'
import MiniMessageText from '@/components/display/MiniMessageText.vue'
import {useEventListener} from '@vueuse/core'
import {useFloatingPanel} from '@/composables/useFloatingPanel'
import {COLORS} from '~/util/minimessage/preview'

/**
 * A question mark beside a MiniMessage field with a quick reference: the colours, styles, gradients and placeholders,
 * each drawn as players see it. Hovering shows it; a click keeps it open while typing until the question mark is
 * clicked again or Escape pressed. Pressing it or an entry leaves the focus in the field, so typing goes on. The
 * pointer leaving both closes it after a moment, so it can cross from the question mark into the reference. Picking
 * an entry hands its tag to the field with `insert`.
 */
const props = defineProps<{
  /** Placeholders the field may use and the sample shown for them. */
  samples?: Readonly<Record<string, string>>
}>()

const emit = defineEmits<{
  insert: [tag: string]
}>()

const {t} = useI18n()
const hovered = ref(false)
const pinned = ref(false)
const open = computed(() => hovered.value || pinned.value)
const trigger = ref<HTMLElement | null>(null)
const {panel, style} = useFloatingPanel(trigger, open, 'end')
const panelId = useId()

const colors = Object.keys(COLORS).filter(name => !name.includes('grey'))
const styles = ['bold', 'italic', 'underlined', 'strikethrough']
const more = computed(() => [
  {tag: '<color:#ff8800>', example: t('editor.miniMessage.help.hex')},
  {tag: '<gradient:red:gold>', example: t('editor.miniMessage.help.gradient')},
  {tag: '<rainbow>', example: t('editor.miniMessage.help.rainbow')},
  {tag: '<reset>', example: t('editor.miniMessage.help.reset')},
])
const placeholders = computed(() => Object.entries(props.samples ?? {}))

let leaving: ReturnType<typeof setTimeout> | undefined

function enter(): void {
  clearTimeout(leaving)
  hovered.value = true
}

function leave(): void {
  clearTimeout(leaving)
  leaving = setTimeout(() => {
    hovered.value = false
  }, 200)
}

onBeforeUnmount(() => clearTimeout(leaving))

useEventListener(document, 'pointermove', event => {
  if (!hovered.value) return
  const target = event.target as Node | null
  if (panel.value?.contains(target) || trigger.value?.contains(target)) enter()
  else leave()
})

useEventListener(document, 'focusin', event => {
  if (panel.value?.contains(event.target as Node | null)) enter()
})

useEventListener(window, 'keydown', event => {
  if (event.key === 'Escape' && pinned.value) pinned.value = false
})
</script>

<template>
  <span ref="trigger" class="inline-flex">
    <BareButton
        :aria-controls="panelId"
        :aria-expanded="open"
        :aria-label="t('editor.miniMessage.help.open')"
        :aria-pressed="pinned"
        :class="pinned ? 'text-accent-text' : 'text-ink-faint hover:text-ink'"
        class="flex size-8 items-center justify-center rounded-control"
        @blur="leave"
        @click="pinned = !pinned"
        @mousedown.prevent
        @focus="enter"
        @mouseenter="enter"
        @mouseleave="leave"
    >
      <AppIcon icon="circle-question"/>
    </BareButton>
    <Teleport to="body">
      <div
          v-if="open"
          :id="panelId"
          ref="panel"
          :aria-label="t('editor.miniMessage.help.title')"
          :style="style"
          class="z-50 flex max-h-[70vh] w-80 flex-col gap-3 overflow-y-auto rounded-panel border border-line bg-surface p-3 text-sm shadow-lg"
          role="dialog"
      >
        <p class="font-semibold">{{ t('editor.miniMessage.help.title') }}</p>
        <section class="flex flex-col gap-1">
          <p class="text-xs text-ink-muted">{{ t('editor.miniMessage.help.colors') }}</p>
          <div class="grid grid-cols-2 gap-x-2">
            <BareButton v-for="color in colors" :key="color" class="flex items-center gap-2 rounded-control px-1 text-left font-mono text-xs hover:bg-raised" @mousedown.prevent @click="emit('insert', `<${color}>`)">
              <span :style="{background: COLORS[color]}" class="size-3 shrink-0 rounded-sm border border-line"/>
              &lt;{{ color }}&gt;
            </BareButton>
          </div>
        </section>
        <section class="flex flex-col gap-1">
          <p class="text-xs text-ink-muted">{{ t('editor.miniMessage.help.styles') }}</p>
          <BareButton v-for="name in styles" :key="name" class="flex items-center justify-between gap-2 rounded-control px-1 text-left hover:bg-raised" @mousedown.prevent @click="emit('insert', `<${name}>`)">
            <span class="font-mono text-xs">&lt;{{ name }}&gt;</span>
            <MiniMessageText :text="`<${name}>${t('editor.miniMessage.help.sample')}`" base="var(--color-ink)"/>
          </BareButton>
          <BareButton v-for="entry in more" :key="entry.tag" class="flex items-center justify-between gap-2 rounded-control px-1 text-left hover:bg-raised" @mousedown.prevent @click="emit('insert', entry.tag)">
            <span class="font-mono text-xs">{{ entry.tag }}</span>
            <span class="text-xs text-ink-muted">{{ entry.example }}</span>
          </BareButton>
          <p class="px-1 text-xs text-ink-muted">{{ t('editor.miniMessage.help.closing', {example: '</red>'}) }}</p>
        </section>
        <section v-if="placeholders.length" class="flex flex-col gap-1">
          <p class="text-xs text-ink-muted">{{ t('editor.miniMessage.help.placeholders') }}</p>
          <BareButton v-for="[name, sample] in placeholders" :key="name" class="flex items-center justify-between gap-2 rounded-control px-1 text-left hover:bg-raised" @mousedown.prevent @click="emit('insert', `<${name}>`)">
            <span class="font-mono text-xs">&lt;{{ name }}&gt;</span>
            <span class="text-xs text-ink-muted">{{ sample }}</span>
          </BareButton>
        </section>
        <p class="text-xs text-ink-muted">{{ t('editor.miniMessage.help.tab') }}</p>
      </div>
    </Teleport>
  </span>
</template>
