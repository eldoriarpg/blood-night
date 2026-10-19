/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'

/**
 * Picks a particle by its key, searchable, with the particle's sprite beside the field once Minecraft's assets are
 * loaded. Particles whose data effects cannot draw are left out.
 */
const model = defineModel<string>({required: true})

defineProps<{
  label: string
  readOnly: boolean
}>()

const session = useEditorSession()
const assets = useParticleAssets()
const kinds = computed(() => session.payload.value?.choices?.particles ?? {})
const options = computed(() => Object.keys(kinds.value).filter(key => kinds.value[key] !== 'UNSUPPORTED').sort())
const labels = computed(() => Object.fromEntries(options.value.map(key => [key, key.replace(/^minecraft:/, '').replaceAll('_', ' ')])))
const sprite = computed(() => assets.sprite(model.value))

onMounted(() => {
  void assets.load()
})
</script>

<template>
  <div class="flex items-center gap-2">
    <img v-if="sprite" :src="sprite" alt="" class="h-8 w-8 shrink-0 rounded-control bg-raised p-0.5 [image-rendering:pixelated]"/>
    <ChoicePicker :label="label" :labels="labels" :model-value="model" :options="options" :read-only="readOnly" class="min-w-0 flex-1" @update:model-value="value => { if (value) model = value }"/>
  </div>
</template>
