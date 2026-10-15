/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import PrimaryButton from '@/components/button/PrimaryButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import CountBadge from '@/components/badge/CountBadge.vue'
import BareButton from '@/components/button/BareButton.vue'
import PresenceList from '~/views/editorview/PresenceList.vue'
import {failure} from '~/api/editor'
import {showToast} from '~/util/toast'
import {EDITOR_AREAS, type EditorArea} from '~/views/editorview/editorArea'
import {docsLink} from '~/util/docsLinks'
import {changesSinceApply} from '~/util/session/lastApplied'

/**
 * The editor's header: the brand, the areas as tabs (mobs with their problem count), a link to the docs about the
 * open area, who is online, whether
 * everything is saved, when the server last applied the session and what changed since, and marking the session as
 * done so the server can apply it, again after each apply.
 */
const area = defineModel<EditorArea>({required: true})

const session = useEditorSession()
const {t, locale} = useI18n()
const submitting = ref(false)

const data = session.data
const ready = computed(() => data.value.status === 'ready')
const saving = computed(() => data.value.pending.length > 0)
const submitted = computed(() => data.value.state === 'SUBMITTED')
const lastApplied = computed(() => data.value.lastApplied)
const sinceApply = computed(() => changesSinceApply(data.value.revision, lastApplied.value))
const appliedAt = computed(() => lastApplied.value ? new Date(lastApplied.value.at).toLocaleString(locale.value) : '')
const mobProblems = computed(() => data.value.problems.filter(problem => problem.path.startsWith('/mobs/')).length)

async function submit(): Promise<void> {
  submitting.value = true
  try {
    await session.api().submit()
    showToast(t('editor.header.submitted'), 'success')
  } catch (error) {
    showToast(failure(error).message, 'error')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <header class="flex flex-wrap items-center gap-4 border-b border-line-soft bg-surface px-5">
    <div class="flex items-center gap-2 py-3">
      <img alt="" class="size-6" src="/logo.svg"/>
      <span class="font-heading text-lg font-bold">{{ t('app.name') }}</span>
    </div>
    <nav v-if="ready" :aria-label="t('editor.header.areas')" class="flex self-stretch gap-5" role="tablist">
      <BareButton
          v-for="key in EDITOR_AREAS"
          :key="key"
          :aria-selected="area === key"
          :class="area === key ? 'border-focus text-ink' : 'border-transparent text-ink-muted hover:text-ink'"
          class="flex items-center gap-2 border-b-2 px-1 text-sm"
          role="tab"
          @click="area = key"
      >
        {{ t(`editor.areas.${key}`) }}
        <CountBadge v-if="key === 'mobs' && mobProblems > 0" :count="mobProblems"/>
      </BareButton>
    </nav>
    <div class="flex-1"/>
    <a
        :href="docsLink(area)"
        class="text-sm text-ink-muted underline-offset-4 hover:text-ink hover:underline"
        data-testid="docs-link"
        rel="noopener"
        target="_blank"
    >{{ t('editor.header.docs') }}</a>
    <template v-if="ready">
      <PresenceList :names="data.online" :you="data.you"/>
      <span class="text-sm text-ink-faint">
        {{ saving ? t('editor.header.saving') : t('editor.header.saved', {revision: data.revision}) }}
      </span>
      <span v-if="lastApplied" class="text-sm text-ink-faint" data-testid="last-applied">
        {{ t('editor.header.applied', {when: appliedAt, revision: lastApplied.revision}) }}
        <template v-if="sinceApply === 1">· {{ t('editor.header.changeSince') }}</template>
        <template v-else-if="sinceApply > 1">· {{ t('editor.header.changesSince', {count: sinceApply}) }}</template>
      </span>
      <SecondaryButton v-if="submitted" :disabled="submitting" @click="submit">{{ t('editor.header.submitAgain') }}</SecondaryButton>
      <PrimaryButton v-else :disabled="submitting || saving" @click="submit">{{ t('editor.header.submit') }}</PrimaryButton>
    </template>
  </header>
</template>
