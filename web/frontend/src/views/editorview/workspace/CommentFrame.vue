/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {Handle, Position} from '@vue-flow/core'
import AppIcon from '@/components/display/AppIcon.vue'
import BareButton from '@/components/button/BareButton.vue'
import CountBadge from '@/components/badge/CountBadge.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import TextAreaInput from '@/components/input/text/TextAreaInput.vue'
import type {Comment} from '~/api/generated/payload'
import {COMMENT_COLORS, type FramePins} from '~/util/graph/comments'

/**
 * A comment frame on the canvas. Open, it is a coloured frame behind the nodes with a title bar to drag it by, a
 * note under the title and a corner to resize it; while selected, its bar offers the colours and the note. Closed, it
 * is one box with the title, how many nodes it hides, a problem count, and a pin for every wire between a hidden node
 * and the rest. A double click on the title renames it; the button at its start opens or closes it.
 */
const props = defineProps<{
  comment: Comment
  /** How many nodes it holds. */
  members: number
  /** The pins of the closed box, one per wire crossing it. */
  pins?: FramePins
  /** How many problems its hidden nodes have. */
  problems: number
  selected: boolean
  readOnly: boolean
  /** The canvas zoom, so a resize follows the pointer. */
  zoom: number
  /** The grid sizes snap to. */
  grid: number
  /** Whether it starts with its title being typed, as a new frame does. */
  naming: boolean
}>()

/** A frame's size while it is resized. */
interface FrameSize {
  width: number
  height: number
}

const emit = defineEmits<{
  edit: [changes: Partial<Comment>]
  toggle: []
  named: []
}>()

const {t} = useI18n()
const editingTitle = ref(false)
const editingNote = ref(false)
const title = ref('')
const note = ref('')
const size = ref<FrameSize | null>(null)
const titleField = ref<HTMLElement | null>(null)
const noteField = ref<HTMLElement | null>(null)

const width = computed(() => size.value?.width ?? props.comment.width)
const height = computed(() => size.value?.height ?? props.comment.height)
const colour = computed(() => COMMENT_COLORS.includes(props.comment.color as typeof COMMENT_COLORS[number]) ? props.comment.color : 'grey')
const rows = computed(() => Math.max(props.pins?.inputs.length ?? 0, props.pins?.outputs.length ?? 0))

watch(() => props.naming, naming => {
  if (naming) startTitle()
}, {immediate: true})

function startTitle(): void {
  if (props.readOnly) return
  title.value = props.comment.title
  editingTitle.value = true
  void nextTick(() => titleField.value?.querySelector('input')?.select())
}

function saveTitle(): void {
  if (!editingTitle.value) return
  editingTitle.value = false
  emit('named')
  const text = title.value.trim()
  if (text !== props.comment.title) emit('edit', {title: text})
}

function startNote(): void {
  note.value = props.comment.note
  editingNote.value = true
  void nextTick(() => noteField.value?.querySelector('textarea')?.focus())
}

function saveNote(): void {
  editingNote.value = false
  const text = note.value.trim()
  if (text !== props.comment.note) emit('edit', {note: text})
}

/** Resizes from the bottom right corner while the pointer is held, and saves the size on the grid when let go. */
function startResize(event: PointerEvent): void {
  const start = {x: event.clientX, y: event.clientY, width: props.comment.width, height: props.comment.height}
  const move = (moved: PointerEvent) => {
    size.value = {
      width: Math.max(props.grid * 6, start.width + (moved.clientX - start.x) / props.zoom),
      height: Math.max(props.grid * 4, start.height + (moved.clientY - start.y) / props.zoom),
    }
  }
  const stop = () => {
    window.removeEventListener('pointermove', move)
    window.removeEventListener('pointerup', stop)
    if (!size.value) return
    const snapped = {width: Math.round(size.value.width / props.grid) * props.grid, height: Math.round(size.value.height / props.grid) * props.grid}
    size.value = null
    if (snapped.width !== props.comment.width || snapped.height !== props.comment.height) emit('edit', snapped)
  }
  window.addEventListener('pointermove', move)
  window.addEventListener('pointerup', stop)
}

function pinTop(index: number, count: number): string {
  return `${44 + (index + 0.5) * ((rows.value * 22) / count)}px`
}
</script>

<template>
  <div
      v-if="comment.collapsed"
      :class="[`bn-frame-${colour}`, selected && 'bn-frame-selected']"
      :style="{minHeight: `${52 + rows * 22}px`}"
      class="bn-frame-closed relative w-56 rounded-panel border-2 bg-surface"
      data-testid="comment-closed"
  >
    <Handle
        v-for="(pin, index) in pins?.inputs ?? []"
        :id="pin.handle"
        :key="pin.handle"
        :class="pin.pin === 'exec' ? 'pin-exec pin-filled' : ['pin-data', `pin-${pin.pin}`, 'pin-filled']"
        :connectable="false"
        :position="Position.Left"
        :style="{top: pinTop(index, pins?.inputs.length ?? 1)}"
        :title="t('editor.comments.pin', {node: pin.node, port: pin.port.split(':').at(-1)})"
        type="target"
    />
    <div class="bn-frame-bar flex items-center gap-2 px-2 py-2">
      <BareButton :aria-label="t('editor.comments.expand')" :disabled="readOnly" :title="t('editor.comments.expand')" class="nodrag text-ink-muted hover:text-ink" data-testid="comment-toggle" @click="emit('toggle')">
        <AppIcon icon="chevron-right"/>
      </BareButton>
      <span class="min-w-0 truncate font-semibold">{{ comment.title || t('editor.comments.untitled') }}</span>
    </div>
    <div class="flex items-center gap-2 px-3 pb-2 text-sm text-ink-muted">
      <span>{{ t('editor.comments.nodes', {count: members}) }}</span>
      <CountBadge v-if="problems > 0" :count="problems" :title="t('editor.comments.problems', {count: problems})" small/>
    </div>
    <Handle
        v-for="(pin, index) in pins?.outputs ?? []"
        :id="pin.handle"
        :key="pin.handle"
        :class="pin.pin === 'exec' ? 'pin-exec pin-filled' : ['pin-data', `pin-${pin.pin}`, 'pin-filled']"
        :connectable="false"
        :position="Position.Right"
        :style="{top: pinTop(index, pins?.outputs.length ?? 1)}"
        :title="t('editor.comments.pin', {node: pin.node, port: pin.port.split(':').at(-1)})"
        type="source"
    />
  </div>
  <div
      v-else
      :class="[`bn-frame-${colour}`, selected && 'bn-frame-selected']"
      :style="{width: `${width}px`, height: `${height}px`}"
      class="bn-frame relative rounded-panel border-2"
      data-testid="comment-frame"
  >
    <div class="bn-frame-bar flex h-11 items-center gap-2 px-2">
      <BareButton :aria-label="t('editor.comments.collapse')" :disabled="readOnly" :title="t('editor.comments.collapse')" class="nodrag text-ink-muted hover:text-ink" data-testid="comment-toggle" @click="emit('toggle')">
        <AppIcon icon="chevron-down"/>
      </BareButton>
      <div v-if="editingTitle" ref="titleField" class="nodrag min-w-0 flex-1">
        <TextInput
            v-model="title"
            :aria-label="t('editor.comments.title')"
            :placeholder="t('editor.comments.untitled')"
            compact
            @blur="saveTitle"
            @keydown.enter.prevent="saveTitle"
            @keydown.esc.prevent="editingTitle = false"
        />
      </div>
      <BareButton v-else :disabled="readOnly" :title="t('editor.comments.rename')" class="min-w-0 truncate text-left font-semibold" data-testid="comment-title" @dblclick.stop="startTitle">
        {{ comment.title || t('editor.comments.untitled') }}
      </BareButton>
      <span v-if="selected && !readOnly" class="nodrag ml-auto flex shrink-0 items-center gap-1">
        <BareButton
            v-for="option in COMMENT_COLORS"
            :key="option"
            :aria-label="t(`editor.comments.colours.${option}`)"
            :aria-pressed="option === colour"
            :class="[`bn-swatch-${option}`, option === colour && 'ring-2 ring-ink']"
            :title="t(`editor.comments.colours.${option}`)"
            class="size-4 rounded-full"
            @click="emit('edit', {color: option})"
        />
        <BareButton :aria-label="t('editor.comments.note')" :title="t('editor.comments.note')" class="ml-1 text-ink-muted hover:text-ink" @click="startNote">
          <AppIcon icon="pen"/>
        </BareButton>
      </span>
    </div>
    <div v-if="editingNote" ref="noteField" class="nodrag bn-frame-note px-2">
      <TextAreaInput v-model="note" :label="t('editor.comments.note')" @blur="saveNote" @keydown.esc.prevent="saveNote"/>
    </div>
    <p v-else-if="comment.note" class="whitespace-pre-line px-3 text-sm text-ink-muted">{{ comment.note }}</p>
    <div
        v-if="!readOnly"
        :aria-label="t('editor.comments.resize')"
        class="bn-frame-resize nodrag absolute right-0 bottom-0 size-4 cursor-nwse-resize"
        role="separator"
        @pointerdown.stop.prevent="startResize"
    />
  </div>
</template>

<style scoped>
.bn-frame {
  pointer-events: none;
  background: color-mix(in srgb, var(--frame) 10%, transparent);
  border-color: color-mix(in srgb, var(--frame) 55%, transparent);
}

.bn-frame-bar,
.bn-frame-note,
.bn-frame-resize {
  pointer-events: auto;
}

.bn-frame .bn-frame-bar {
  cursor: grab;
  border-bottom: 1px solid color-mix(in srgb, var(--frame) 35%, transparent);
  background: color-mix(in srgb, var(--frame) 22%, transparent);
}

.bn-frame-closed {
  border-color: var(--frame);
}

.bn-frame-selected {
  border-color: var(--frame);
  box-shadow: 0 0 0 1px var(--color-focus);
}

.bn-frame-resize {
  background: linear-gradient(135deg, transparent 50%, color-mix(in srgb, var(--frame) 70%, transparent) 50%);
}

.bn-frame-grey, .bn-swatch-grey { --frame: var(--color-ink-faint); }
.bn-frame-red, .bn-swatch-red { --frame: var(--color-accent-text); }
.bn-frame-orange, .bn-swatch-orange { --frame: var(--color-held); }
.bn-frame-yellow, .bn-swatch-yellow { --frame: var(--color-pin-location); }
.bn-frame-green, .bn-swatch-green { --frame: var(--color-pin-number); }
.bn-frame-blue, .bn-swatch-blue { --frame: var(--color-pin-entity); }
.bn-frame-purple, .bn-swatch-purple { --frame: var(--color-pin-text); }

[class*='bn-swatch-'] {
  background: var(--frame);
}
</style>
