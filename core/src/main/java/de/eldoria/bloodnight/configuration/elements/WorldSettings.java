package de.eldoria.bloodnight.configuration.elements;

import de.eldoria.bloodnight.configuration.elements.world.SoundSettings;
import de.eldoria.bloodnight.configuration.elements.world.MobSettings;
import de.eldoria.bloodnight.configuration.elements.world.NightSelection;
import de.eldoria.bloodnight.configuration.elements.world.NightSettings;
import de.eldoria.bloodnight.configuration.elements.world.WorldDrops;

public class WorldSettings {
    private final NightSelection nightSelection;
    private final NightSettings nightSettings;
    private final MobSettings mobSettings;
    private final SoundSettings soundSettings;
    private final WorldDrops worldDrops;

    public WorldSettings(NightSelection nightSelection, NightSettings nightSettings, MobSettings mobSettings, SoundSettings soundSettings, WorldDrops worldDrops) {
        this.nightSelection = nightSelection;
        this.nightSettings = nightSettings;
        this.mobSettings = mobSettings;
        this.soundSettings = soundSettings;
        this.worldDrops = worldDrops;
    }

    public WorldSettings() {
        nightSelection = new NightSelection();
        nightSettings = new NightSettings();
        mobSettings = new MobSettings();
        soundSettings = new SoundSettings();
        worldDrops = WorldDrops.defaultDrops();
    }

    public NightSelection nightSelection() {
        return nightSelection;
    }

    public NightSettings nightSettings() {
        return nightSettings;
    }

    public MobSettings mobSettings() {
        return mobSettings;
    }

    public SoundSettings soundSettings() {
        return soundSettings;
    }

    public WorldDrops worldDrops() {
        return worldDrops;
    }
}

