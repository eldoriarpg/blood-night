/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import ButtonRow from '@/components/button/ButtonRow.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import MutedText from '@/components/typography/MutedText.vue'
import PreviewTarget from '~/views/editorview/particles/PreviewTarget.vue'
import type {ParticleEffect} from '~/api/generated/payload'
import {aimed} from '~/util/particles/particleEffects'
import {Simulation, type Body} from '~/util/particles/preview'
import {draw, imageCache, type Camera} from '~/util/particles/previewCanvas'
import {vec} from '~/util/particles/shapes'

/**
 * Plays an effect in the browser on a mob figure, and on a player figure for effects with a target, the player
 * movable and turnable. The view turns slowly around them. Particle motion is only roughly like the game's.
 */
const props = defineProps<{
  effect: ParticleEffect
  /** The small variant beside a mob's effects: no controls, no target. */
  compact?: boolean
}>()

const {t} = useI18n()
const session = useEditorSession()
const particles = useParticleAssets()
const items = useItemAssets()
const canvas = ref<HTMLCanvasElement | null>(null)
const playing = ref(true)
const loop = ref(true)
const slow = ref(false)
const target = ref({distance: 6, lift: 0, side: 0, turn: 90})
const withTarget = computed(() => !props.compact && aimed(props.effect))
const images = imageCache()
const source: Body = {feet: vec(0, 0, 0), height: 1.95, yaw: 0}
let simulation: Simulation | null = null
let frame = 0
let last = 0
let elapsed = 0
let angle = 0.6

function targetBody(): Body | null {
  if (!withTarget.value) return null
  const {distance, lift, side, turn} = target.value
  return {feet: vec(side, lift, distance), height: 1.8, yaw: turn}
}

function restart(): void {
  simulation = new Simulation(props.effect, source, targetBody(), session.payload.value?.choices?.particles ?? {})
}

function icon(item: string | undefined): string | undefined {
  if (!item) return undefined
  const material = session.payload.value?.items?.[item]?.material ?? item
  return items.icon(items.items.value[material]?.texture)
}

function render(now: number): void {
  frame = requestAnimationFrame(render)
  const element = canvas.value
  const context = element?.getContext('2d')
  if (!element || !context || !simulation) return
  const step = slow.value ? 200 : 50
  if (playing.value) {
    elapsed += now - last
    angle += (now - last) / 9000
  }
  last = now
  while (playing.value && elapsed >= step) {
    elapsed -= step
    simulation.step()
    if (simulation.done && loop.value) restart()
  }
  const body = targetBody()
  const centre = body ? vec(body.feet.x / 2, 1 + body.feet.y / 2, body.feet.z / 2) : vec(0, 1, 0)
  const span = body ? Math.hypot(body.feet.x, body.feet.y, body.feet.z) : 0
  const camera: Camera = {centre, angle, scale: Math.min(element.width, element.height) / (5 + span), width: element.width, height: element.height}
  draw(context, camera, {
    bodies: body ? [source, body] : [source],
    dots: simulation.dots,
    pieces: simulation.pieces,
    sprite: particles.sprite,
    icon,
  }, images, getComputedStyle(element).color)
}

watch([() => props.effect, target, withTarget], restart, {deep: true})

onMounted(() => {
  void particles.load()
  void items.load()
  restart()
  last = performance.now()
  frame = requestAnimationFrame(render)
})

onBeforeUnmount(() => cancelAnimationFrame(frame))
</script>

<template>
  <section :aria-label="t('editor.particles.preview.title')" class="flex flex-col gap-2">
    <canvas ref="canvas" :class="compact ? 'h-40 w-56' : 'h-80 w-full max-w-2xl'" :height="compact ? 160 : 320" :width="compact ? 224 : 640" class="rounded-panel border border-line-soft bg-surface text-ink"/>
    <template v-if="!compact">
      <ButtonRow>
        <SecondaryButton :icon="playing ? 'pause' : 'play'" compact @click="playing = !playing">{{ t(playing ? 'editor.particles.preview.pause' : 'editor.particles.preview.play') }}</SecondaryButton>
        <SecondaryButton compact icon="arrows-rotate" @click="restart">{{ t('editor.particles.preview.again') }}</SecondaryButton>
      </ButtonRow>
      <div class="flex max-w-md flex-col gap-2">
        <ToggleSetting v-model="loop" :label="t('editor.particles.preview.loop')"/>
        <ToggleSetting v-model="slow" :label="t('editor.particles.preview.slow')"/>
      </div>
      <PreviewTarget v-if="withTarget" v-model="target"/>
      <MutedText size="sm" tag="p">{{ t('editor.particles.preview.approximate', {command: '/bloodnight editor preview <id>'}) }}</MutedText>
    </template>
  </section>
</template>
