package com.oidioomstudio.torchmanator.config;

import com.oidioomstudio.olib.config.OConfigHolder;

public class TorchmanatorConfig {
    public static class ConfigData {
        public boolean enabled = true;
        public int radius = 512;
        public int maxBlockLight = 4;
        public int tickInterval = 10;
    }

    private static final OConfigHolder<ConfigData> HOLDER = new OConfigHolder<>(
        "torchmanator",
        ConfigData.class,
        ConfigData::new
    );

    public static boolean enabled = true;
    public static int radius = 512;
    public static int maxBlockLight = 4;
    public static int tickInterval = 10;

    public static void load() {
        ConfigData data = HOLDER.load();
        enabled = data.enabled;
        radius = Math.clamp(data.radius, 16, 512);
        maxBlockLight = Math.clamp(data.maxBlockLight, 0, 12);
        tickInterval = Math.clamp(data.tickInterval, 2, 40);
    }

    public static void save() {
        ConfigData data = HOLDER.get();
        data.enabled = enabled;
        data.radius = radius;
        data.maxBlockLight = maxBlockLight;
        data.tickInterval = tickInterval;
        HOLDER.save();
    }

    public static void resetDefaults() {
        enabled = true;
        radius = 512;
        maxBlockLight = 4;
        tickInterval = 10;
        save();
    }
}
