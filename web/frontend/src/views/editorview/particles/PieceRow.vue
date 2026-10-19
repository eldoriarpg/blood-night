/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import LabelledField from '@/components/input/LabelledField.vue'
import ColorInput from '@/components/input/color/ColorInput.vue'
import SelectInput from '@/components/input/select/SelectInput.vue'
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import ItemPicker from '~/views/editorview/form/ItemPicker.vue'
import AnchorFields from '~/views/editorview/particles/AnchorFields.vue'
import EffectNumber from '~/views/editorview/particles/EffectNumber.vue'
import MotionFields from '~/views/editorview/particles/MotionFields.vue'
import VecFields from '~/views/editorview/particles/VecFields.vue'
import type {Facing, Piece, PieceKind} from '~/api/generated/payload'

/**
 * One display piece of a particle effect: an item, a block, or a block stretched from source to target; where it
 * sits, how it is turned and sized, how it travels and stays, and whether it glows. Each change is saved as the whole
 * piece.
 */
const props = defineProps<{
  piece: Piece
  index: number
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [piece: Piece]
  remove: []
}>()

const {t} = useI18n()
const assets = useItemAssets()
const items = assets.choices(true)
const blocks = assets.choices(false)
const KINDS: readonly PieceKind[] = ['ITEM', 'BLOCK', 'SPAN']
const FACINGS: readonly Facing[] = ['PATH', 'BODY', 'LOOK', 'UP']
const rotation = computed(() => props.piece.rotation ?? {pitch: 0, yaw: 0, roll: 0})

function save(change: Partial<Piece>): void {
  emit('save', {...props.piece, ...change})
}
</script>

<template>
  <li :aria-label="t('editor.particles.piece', {number: index + 1})" class="flex flex-col gap-3 rounded-panel border border-line-soft p-3">
    <div class="flex flex-wrap items-end gap-3">
      <SectionLabel class="pb-2">{{ t('editor.particles.piece', {number: index + 1}) }}</SectionLabel>
      <LabelledField v-slot="{id}" :label="t('editor.particles.pieceKind')" class="w-48">
        <SelectInput :id="id" :disabled="readOnly" :model-value="piece.kind" @update:model-value="kind => save({kind: kind as PieceKind})">
          <option v-for="kind in KINDS" :key="kind" :value="kind">{{ t(`editor.particles.pieceKinds.${kind}`) }}</option>
        </SelectInput>
      </LabelledField>
      <LabelledField v-if="piece.kind === 'ITEM'" :label="t('editor.particles.data.item')" class="min-w-56 flex-1">
        <ItemPicker :choices="items" :label="t('editor.particles.data.item')" :model-value="piece.item" :read-only="readOnly" @update:model-value="item => save({item})"/>
      </LabelledField>
      <LabelledField v-else :label="t('editor.particles.data.block')" class="min-w-56 flex-1">
        <ItemPicker :choices="blocks" :label="t('editor.particles.data.block')" :model-value="piece.block" :read-only="readOnly" @update:model-value="block => save({block})"/>
      </LabelledField>
      <MutedIconButton v-if="!readOnly" :label="t('editor.particles.removePiece')" class="mb-1 ml-auto" hover="error" icon="trash" @click="emit('remove')"/>
    </div>
    <AnchorFields :anchor="piece.anchor" :read-only="readOnly" @save="anchor => save({anchor})"/>
    <div class="flex flex-wrap items-end gap-3">
      <LabelledField v-if="piece.kind !== 'SPAN'" v-slot="{id}" :label="t('editor.particles.shape.facing')" class="w-48">
        <SelectInput :id="id" :disabled="readOnly" :model-value="piece.facing ?? 'PATH'" @update:model-value="facing => save({facing: facing as Facing})">
          <option v-for="facing in FACINGS" :key="facing" :value="facing">{{ t(`editor.particles.shape.facings.${facing}`) }}</option>
        </SelectInput>
      </LabelledField>
      <EffectNumber v-if="piece.kind !== 'SPAN'" :label="t('editor.particles.scale')" :max="8" :min="0.05" :read-only="readOnly" :value="piece.scale" class="w-28" @save="scale => save({scale})"/>
      <EffectNumber v-if="piece.kind === 'SPAN'" :label="t('editor.particles.width')" :max="2" :min="0.01" :read-only="readOnly" :value="piece.width" class="w-28" @save="width => save({width})"/>
      <EffectNumber v-if="piece.kind === 'SPAN'" :label="t('editor.particles.reach')" :min="0" :read-only="readOnly" :value="piece.grow" class="w-28" whole @save="grow => save({grow})"/>
      <EffectNumber v-if="piece.motion" :label="t('editor.particles.stick')" :max="200" :min="0" :read-only="readOnly" :value="piece.stick" class="w-28" whole @save="stick => save({stick})"/>
    </div>
    <VecFields
        v-if="piece.kind !== 'SPAN'"
        :axes="[t('editor.particles.rotation.pitch'), t('editor.particles.rotation.yaw'), t('editor.particles.rotation.roll')]"
        :label="t('editor.particles.rotation.label')"
        :read-only="readOnly"
        :value="{x: rotation.pitch, y: rotation.yaw, z: rotation.roll}"
        @save="turn => save({rotation: {pitch: turn.x, yaw: turn.y, roll: turn.z}})"
    />
    <MotionFields v-if="piece.kind !== 'SPAN'" :motion="piece.motion" :read-only="readOnly" @save="motion => save({motion})"/>
    <div class="flex flex-wrap items-end gap-3">
      <ToggleSetting :disabled="readOnly" :label="t('editor.particles.glow')" :model-value="piece.glow ?? false" class="min-w-56 flex-1" @update:model-value="glow => save({glow})"/>
      <LabelledField v-if="piece.glow" v-slot="{id}" :label="t('editor.particles.glowColor')">
        <ColorInput :id="id" :disabled="readOnly" :model-value="piece.color ?? '#ffffff'" @update:model-value="color => save({color})"/>
      </LabelledField>
    </div>
  </li>
</template>
