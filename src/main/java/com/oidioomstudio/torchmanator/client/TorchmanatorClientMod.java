package com.oidioomstudio.torchmanator.client;

import com.oidioomstudio.olib.api.OConfigCategory;
import com.oidioomstudio.olib.api.OConfigRegistry;
import com.oidioomstudio.olib.api.OModConfig;
import com.oidioomstudio.olib.api.options.IntegerSliderOption;
import com.oidioomstudio.torchmanator.TorchmanatorMod;
import com.oidioomstudio.torchmanator.config.TorchmanatorConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class TorchmanatorClientMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("torchmanator-client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Torchmanator] Initializing client and registering O Lib configuration with category hierarchy...");
        registerConfig();
    }

    public static void registerConfig() {
        // Genel (General) Category
        OConfigCategory generalCategory = OConfigCategory.builder("general", Component.translatable("torchmanator.config.tab.general"))
            // Subcategory Group: Yerleştirme & Tarama Parametreleri / Placement & Scanning Parameters
            .group("placement", Component.translatable("torchmanator.group.placement"), group -> {
                group.description(Component.translatable("torchmanator.group.placement.desc"));
                group.add(IntegerSliderOption.builder("radius", Component.translatable("torchmanator.config.radius"))
                    .description(Component.translatable("torchmanator.config.radius.desc"))
                    .range(16, 1024, 16)
                    .defaultValue(512)
                    .formatter(val -> Component.translatable("torchmanator.config.radius.val", val))
                    .getter(() -> TorchmanatorConfig.radius)
                    .setter(val -> TorchmanatorConfig.radius = val)
                    .build());
                group.add(IntegerSliderOption.builder("tick_interval", Component.translatable("torchmanator.config.tick_interval"))
                    .description(Component.translatable("torchmanator.config.tick_interval.desc"))
                    .range(2, 60, 2)
                    .defaultValue(10)
                    .formatter(val -> Component.translatable(
                        "torchmanator.config.tick_interval.val",
                        val,
                        String.format(Locale.ROOT, "%.2f", val / 20.0f)
                    ))
                    .getter(() -> TorchmanatorConfig.tickInterval)
                    .setter(val -> TorchmanatorConfig.tickInterval = val)
                    .build());
                group.add(IntegerSliderOption.builder("max_block_light", Component.translatable("torchmanator.config.max_block_light"))
                    .description(Component.translatable("torchmanator.config.max_block_light.desc"))
                    .range(0, 15, 1)
                    .defaultValue(4)
                    .formatter(val -> {
                        Component desc = (val == 0) ? Component.translatable("torchmanator.config.light.pitch_black")
                                    : (val == 4) ? Component.translatable("torchmanator.config.light.recommended")
                                    : (val >= 7) ? Component.translatable("torchmanator.config.light.bright")
                                    : Component.empty();
                        return Component.literal(String.valueOf(val)).append(desc);
                    })
                    .getter(() -> TorchmanatorConfig.maxBlockLight)
                    .setter(val -> TorchmanatorConfig.maxBlockLight = val)
                    .build());
            })
            .build();

        // Register Torchmanator into OConfigRegistry
        OConfigRegistry.register(
            OModConfig.builder(TorchmanatorMod.MOD_ID, Component.translatable("itemGroup.torchmanator"), TorchmanatorMod.MOD_VERSION)
                .category(generalCategory)
                .onSave(TorchmanatorConfig::save)
                .onResetAll(TorchmanatorConfig::resetDefaults)
                .build()
        );
    }
}
