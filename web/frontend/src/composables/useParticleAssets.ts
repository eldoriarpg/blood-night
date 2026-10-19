/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {particleIndex, textureUrl} from '~/api/assets'

/** The particle sprites of the session's Minecraft version, loaded once per version. */
interface ParticleAssetsData {
    version: string
    status: 'idle' | 'loading' | 'ready' | 'failed'
    particles: Record<string, string[]>
}

/**
 * Minecraft's particle sprites for the session's Minecraft version, shared by every particle picker and preview.
 * While they load or when the web backend cannot fetch them, particles are drawn as plain dots.
 */
export function useParticleAssets() {
    const data = useState<ParticleAssetsData>('useParticleAssets', () => ({version: '', status: 'idle', particles: {}}))
    const session = useEditorSession()
    const version = computed(() => session.payload.value?.choices?.minecraftVersion ?? '')

    async function load(): Promise<void> {
        const wanted = version.value
        if (wanted === '' || (data.value.version === wanted && data.value.status !== 'failed')) return
        data.value = {version: wanted, status: 'loading', particles: {}}
        try {
            const index = await particleIndex(wanted)
            if (data.value.version === wanted) data.value = {version: wanted, status: 'ready', particles: index.particles ?? {}}
        } catch {
            if (data.value.version === wanted) data.value = {version: wanted, status: 'failed', particles: {}}
        }
    }

    /**
     * @param particle a particle's key, e.g. `minecraft:flame`
     * @returns the URL of its first sprite, none for particles without one or while not loaded
     */
    function sprite(particle: string): string | undefined {
        const first = data.value.particles[particle]?.[0]
        return first && version.value ? textureUrl(version.value, first) : undefined
    }

    return {status: computed(() => data.value.status), load, sprite}
}
