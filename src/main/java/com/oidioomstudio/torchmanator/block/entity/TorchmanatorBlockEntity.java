package com.oidioomstudio.torchmanator.block.entity;

import com.oidioomstudio.torchmanator.TorchmanatorMod;
import com.oidioomstudio.torchmanator.block.TorchmanatorBlock;
import com.oidioomstudio.torchmanator.config.TorchmanatorConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TorchmanatorBlockEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);

    public TorchmanatorBlockEntity(BlockPos pos, BlockState state) {
        super(TorchmanatorMod.TORCHMANATOR_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.torchmanator");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return ChestMenu.threeRows(syncId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }

    @Override
    public void stopOpen(ContainerUser user) {
        super.stopOpen(user);
        if (this.level != null && !this.level.isClientSide()) {
            this.level.playSound(null, this.worldPosition, SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS, 0.85f, 1.1f);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TorchmanatorBlockEntity entity) {
        if (level.isClientSide()) return;

        // Find available torches in 27 slots
        int torchSlot = -1;
        ItemStack torchStack = ItemStack.EMPTY;
        for (int i = 0; i < entity.getContainerSize(); i++) {
            ItemStack stack = entity.getItem(i);
            if (!stack.isEmpty() && (stack.is(Items.TORCH) || stack.is(Items.SOUL_TORCH) || stack.is(Items.REDSTONE_TORCH))) {
                torchSlot = i;
                torchStack = stack;
                break;
            }
        }

        boolean hasTorches = (torchSlot != -1);

        // Update visual lit state (lantern appears when torches are inside)
        if (state.getValue(TorchmanatorBlock.LIT) != hasTorches) {
            level.setBlock(pos, state.setValue(TorchmanatorBlock.LIT, hasTorches), 3);
            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);
            if (aboveState.is(state.getBlock())) {
                level.setBlock(above, aboveState.setValue(TorchmanatorBlock.LIT, hasTorches), 3);
            }
        }

        if (!hasTorches || !TorchmanatorConfig.enabled) return;

        // Run every configured tick interval (default: 10 ticks = 0.5s)
        if (level.getGameTime() % Math.max(2, TorchmanatorConfig.tickInterval) != 0) return;

        RandomSource rand = level.getRandom();
        int maxConfiguredRadius = Math.max(16, TorchmanatorConfig.radius);
        int nearMax = Math.min(24, maxConfiguredRadius);
        int midMax = Math.min(128, maxConfiguredRadius);

        BlockState torchState = torchStack.is(Items.SOUL_TORCH) ? Blocks.SOUL_TORCH.defaultBlockState()
                              : torchStack.is(Items.REDSTONE_TORCH) ? Blocks.REDSTONE_TORCH.defaultBlockState()
                              : Blocks.TORCH.defaultBlockState();

        BlockPos.MutableBlockPos candidatePos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos floorPos = new BlockPos.MutableBlockPos();

        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();

        // 48 attempts per cycle across near, mid, and far distance brackets
        for (int attempt = 0; attempt < 48; attempt++) {
            int minR;
            int maxR;
            if (attempt < 24) {
                minR = 2;
                maxR = nearMax;
            } else if (attempt < 36) {
                minR = nearMax;
                maxR = midMax;
            } else {
                minR = midMax;
                maxR = maxConfiguredRadius;
            }

            if (minR >= maxR) {
                minR = 2;
                maxR = maxConfiguredRadius;
            }

            double angle = rand.nextDouble() * 2 * Math.PI;
            double dist = minR + rand.nextDouble() * (maxR - minR);
            int targetX = posX + (int) Math.round(dist * Math.cos(angle));
            int targetZ = posZ + (int) Math.round(dist * Math.sin(angle));

            if (!level.hasChunk(targetX >> 4, targetZ >> 4)) continue;

            int targetY;
            if (attempt % 2 == 0) {
                targetY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, targetX, targetZ);
            } else {
                targetY = posY + rand.nextInt(31) - 15;
            }

            candidatePos.set(targetX, targetY, targetZ);
            floorPos.set(targetX, targetY - 1, targetZ);

            if (!level.isLoaded(candidatePos) || !level.isLoaded(floorPos)) {
                continue;
            }

            // Adjust candidatePos to the air block above the solid ground
            if (!level.getBlockState(candidatePos).isAir()) {
                candidatePos.move(Direction.UP);
                floorPos.move(Direction.UP);
            }

            // If below is air, scan down up to 5 blocks to find a solid floor
            int scanDown = 0;
            while (scanDown < 5 && candidatePos.getY() > level.getMinY() && level.getBlockState(floorPos).isAir()) {
                candidatePos.move(Direction.DOWN);
                floorPos.move(Direction.DOWN);
                scanDown++;
            }

            // Re-verify loaded state after scanning
            if (!level.isLoaded(candidatePos) || !level.isLoaded(floorPos)) {
                continue;
            }

            // Strictly require candidate position to be empty AIR (never break or overwrite existing blocks/crops/chests/redstone)
            BlockState candidateState = level.getBlockState(candidatePos);
            if (!candidateState.isAir() || !candidateState.getFluidState().isEmpty()) {
                continue;
            }

            // Strictly require floor underneath to be solid, sturdy on top (never float in mid-air)
            BlockState floorState = level.getBlockState(floorPos);
            if (floorState.isAir() || !floorState.getFluidState().isEmpty() || !floorState.isFaceSturdy(level, floorPos, Direction.UP)) {
                continue;
            }

            // Check torch survival rule on this exact floor block
            if (!torchState.canSurvive(level, candidatePos)) {
                continue;
            }

            // Check block light level against user config threshold
            int blockLight = level.getBrightness(LightLayer.BLOCK, candidatePos);
            if (blockLight <= TorchmanatorConfig.maxBlockLight) {
                BlockPos placePos = candidatePos.immutable();
                level.setBlock(placePos, torchState, Block.UPDATE_ALL);
                level.playSound(null, placePos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.35f, 1.4f);
                level.playSound(null, placePos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.6f, 1.0f);

                entity.removeItem(torchSlot, 1);
                entity.setChanged();
                break;
            }
        }
    }
}
