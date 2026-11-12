package com.oidioomstudio.torchmanator;

import com.oidioomstudio.torchmanator.block.TorchmanatorBlock;
import com.oidioomstudio.torchmanator.block.entity.TorchmanatorBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TorchmanatorMod implements ModInitializer {
    public static final String MOD_ID = "torchmanator";
    public static final String MOD_VERSION = "1.1.2";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ResourceKey<Block> TORCHMANATOR_BLOCK_KEY = ResourceKey.create(
        Registries.BLOCK,
        Identifier.fromNamespaceAndPath(MOD_ID, "torchmanator")
    );

    public static final ResourceKey<Item> TORCHMANATOR_ITEM_KEY = ResourceKey.create(
        Registries.ITEM,
        Identifier.fromNamespaceAndPath(MOD_ID, "torchmanator")
    );

    public static final ResourceKey<CreativeModeTab> TORCHMANATOR_TAB_KEY = ResourceKey.create(
        Registries.CREATIVE_MODE_TAB,
        Identifier.fromNamespaceAndPath(MOD_ID, "torchmanator_tab")
    );

    public static final TorchmanatorBlock TORCHMANATOR_BLOCK = new TorchmanatorBlock(
        BlockBehaviour.Properties.of()
            .setId(TORCHMANATOR_BLOCK_KEY)
            .sound(SoundType.VAULT)
            .strength(3.5f, 6.0f)
            .requiresCorrectToolForDrops()
            .lightLevel(state -> state.getValue(TorchmanatorBlock.LIT) ? 15 : 0)
            .noOcclusion()
    );

    public static final BlockItem TORCHMANATOR_ITEM = new BlockItem(
        TORCHMANATOR_BLOCK,
        new Item.Properties()
            .setId(TORCHMANATOR_ITEM_KEY)
            .useBlockDescriptionPrefix()
    );

    public static final BlockEntityType<TorchmanatorBlockEntity> TORCHMANATOR_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(
        TorchmanatorBlockEntity::new,
        TORCHMANATOR_BLOCK
    ).build();

    public static final CreativeModeTab TORCHMANATOR_TAB = FabricCreativeModeTab.builder()
        .icon(() -> new ItemStack(TORCHMANATOR_ITEM))
        .title(Component.translatable("itemGroup.torchmanator"))
        .displayItems((itemDisplayParameters, output) -> {
            output.accept(TORCHMANATOR_ITEM);
        })
        .build();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Torchmanator Mod by Oidioom (Developer: Tarek Sharabaty)...");
        com.oidioomstudio.torchmanator.config.TorchmanatorConfig.load();

        Registry.register(BuiltInRegistries.BLOCK, TORCHMANATOR_BLOCK_KEY, TORCHMANATOR_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, TORCHMANATOR_ITEM_KEY, TORCHMANATOR_ITEM);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "torchmanator_block_entity"), TORCHMANATOR_BLOCK_ENTITY);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TORCHMANATOR_TAB_KEY, TORCHMANATOR_TAB);
        com.oidioomstudio.olib.api.OCreativeTabs.accept(TORCHMANATOR_ITEM);

        LOGGER.info("Torchmanator Mod successfully initialized with dedicated Creative Tab!");
    }
}
