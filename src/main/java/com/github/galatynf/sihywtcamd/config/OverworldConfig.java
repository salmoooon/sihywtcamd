package com.github.galatynf.sihywtcamd.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "overworld")
public class OverworldConfig implements ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    public General general = new General();

    public static class General {
        public boolean mobsLessFear = true;
        public boolean merchantHostility = true;
    }

    @ConfigEntry.Gui.CollapsibleObject
    public Creeper creeper = new Creeper();

    public static class Creeper {
        public boolean explosionFatigue = true;
        public boolean explosionWeakness = true;
        public boolean chainExplosions = true;
    }

    @ConfigEntry.Gui.CollapsibleObject
    public Phantom phantom = new Phantom();

    public static class Phantom {
        public boolean throughBlocks = true;
        public boolean lightFear = true;
        public boolean translucent = true;
    }

    @ConfigEntry.Gui.CollapsibleObject
    public Slime slime = new Slime();

    public static class Slime {
        public boolean biggerSize = true;
        public boolean canMerge = true;
    }

    public boolean guardianNaturalSpawn = true;
}
