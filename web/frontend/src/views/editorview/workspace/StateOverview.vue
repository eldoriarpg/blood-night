/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'
import type {Graph} from '~/api/generated/payload'
import {ALWAYS, stateLayout, type StateArrow, type StateBox, type StateLayout} from '~/util/graph/stateLayout'

/**
 * The states of a mob's graph at a glance: every state a box, the states inside one drawn inside its box, and an arrow
 * for every Go To and every choice of Go To One Of, from the state the node lies in. Moves made from the part that
 * always runs start at a box of their own. Picking a box opens that state.
 */
const state = defineModel<string | undefined>('state', {required: true})

const props = defineProps<{
  graph: Graph
}>()

const emit = defineEmits<{
  close: []
}>()

const {t} = useI18n()
const layout = ref<StateLayout | null>(null)

watch(() => props.graph, async graph => {
  layout.value = await stateLayout(graph)
}, {immediate: true})

function label(box: StateBox): string {
  if (box.name === ALWAYS) return t('editor.states.general')
  return box.initial ? `★ ${box.name}` : box.name
}

function path(arrow: StateArrow): string {
  return arrow.points.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`).join(' ')
}

function open(box: StateBox): void {
  state.value = box.name === ALWAYS ? undefined : box.name
}
</script>

<template>
  <section :aria-label="t('editor.states.overview')" class="absolute bottom-20 left-1/2 flex max-h-[60%] w-[44rem] max-w-[calc(100%-2rem)] -translate-x-1/2 flex-col gap-2 rounded-panel border border-line bg-surface p-3">
    <div class="flex items-center justify-between">
      <SectionLabel>{{ t('editor.states.overview') }}</SectionLabel>
      <MutedIconButton :label="t('editor.palette.close')" icon="xmark" @click="emit('close')"/>
    </div>
    <EmptyState v-if="!layout || layout.boxes.length === 0" compact :message="t('editor.states.none')"/>
    <div v-else class="overflow-auto">
      <svg :height="layout.height" :viewBox="`0 0 ${layout.width} ${layout.height}`" :width="layout.width" class="text-ink" role="img">
        <defs>
          <marker id="state-arrow" markerHeight="8" markerWidth="8" orient="auto-start-reverse" refX="7" refY="4">
            <path class="fill-ink-muted" d="M0,0 L8,4 L0,8 z"/>
          </marker>
        </defs>
        <g v-for="box in layout.boxes" :key="box.name" :aria-label="label(box)" class="cursor-pointer" role="button" tabindex="0" @click="open(box)" @keydown.enter="open(box)" @keydown.space.prevent="open(box)">
          <rect
              :class="[(box.name === ALWAYS ? undefined : box.name) === state ? 'stroke-focus' : 'stroke-line', box.depth % 2 === 0 ? 'fill-raised' : 'fill-surface']"
              :height="box.height"
              :width="box.width"
              :x="box.x"
              :y="box.y"
              rx="8"
              stroke-width="1.5"
          />
          <text :x="box.x + 12" :y="box.y + (box.parent ? 18 : box.height / 2 + 4)" class="fill-ink font-mono text-xs">{{ label(box) }}</text>
        </g>
        <path v-for="arrow in layout.arrows" :key="`${arrow.from}>${arrow.to}`" :d="path(arrow)" class="stroke-ink-muted" fill="none" marker-end="url(#state-arrow)" stroke-width="1.5"/>
      </svg>
    </div>
  </section>
</template>
