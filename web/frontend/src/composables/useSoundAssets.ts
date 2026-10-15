/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {SoundAsset} from '~/api/generated/schema'
import {soundIndex, soundUrl} from '~/api/assets'
import {bareSoundKey, keyName, soundChoices, type SoundChoice} from '~/util/sounds/soundChoices'
import {translated} from '~/util/items/itemChoices'

interface SoundAssetsData {
    version: string
    status: 'idle' | 'loading' | 'ready' | 'failed'
    sounds: Record<string, SoundAsset>
}

/**
 * Minecraft's sounds for the session's Minecraft version: their names in the editor's language, loaded once per
 * version and shared by every sound picker, and playback in the browser. While they load or when the web backend
 * cannot fetch them, sounds are named by their key and cannot be played.
 * <p>
 * Minecraft changes a sound's pitch by playing it faster or slower, so playback here does the same.
 */
export function useSoundAssets() {
    const data = useState<SoundAssetsData>('useSoundAssets', () => ({version: '', status: 'idle', sounds: {}}))
    const session = useEditorSession()
    const {locale} = useI18n()
    const version = computed(() => session.payload.value?.choices?.minecraftVersion ?? '')

    async function load(): Promise<void> {
        const wanted = version.value
        if (wanted === '' || (data.value.version === wanted && data.value.status !== 'failed')) return
        data.value = {version: wanted, status: 'loading', sounds: {}}
        try {
            const index = await soundIndex(wanted)
            if (data.value.version === wanted) data.value = {version: wanted, status: 'ready', sounds: index.sounds ?? {}}
        } catch {
            if (data.value.version === wanted) data.value = {version: wanted, status: 'failed', sounds: {}}
        }
    }

    const choices = computed<SoundChoice[]>(() => soundChoices(data.value.sounds, locale.value))

    /**
     * @param key a sound's key, e.g. `minecraft:entity.zombie.ambient`
     * @returns what it sounds like in the editor's language, else its key made readable
     */
    function name(key: string): string {
        return translated(data.value.sounds[bareSoundKey(key)]?.subtitles, locale.value) ?? keyName(key)
    }

    /**
     * @param key a sound's key
     * @returns whether the browser can play it
     */
    function playable(key: string): boolean {
        return version.value !== '' && data.value.sounds[bareSoundKey(key)]?.playable === true
    }

    /**
     * Plays a sound once.
     *
     * @param key a sound's key
     * @param pitch 0.5 to 2, 1 as recorded
     * @param volume 0 to 1
     */
    function play(key: string, pitch = 1, volume = 1): void {
        if (!playable(key)) return
        const audio = new Audio(soundUrl(version.value, bareSoundKey(key)))
        audio.preservesPitch = false
        audio.playbackRate = Math.min(2, Math.max(0.5, pitch))
        audio.volume = Math.min(1, Math.max(0, volume))
        void audio.play().catch(() => undefined)
    }

    return {load, choices, name, playable, play, status: computed(() => data.value.status)}
}
