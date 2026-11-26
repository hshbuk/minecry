package com.farsight.block;

import com.farsight.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalRelayBlock extends Block {
    public static final BooleanProperty TUNED = BooleanProperty.of("tuned");
    public static final IntProperty FREQUENCY = IntProperty.of("frequency", 0, 3);

    public SignalRelayBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(TUNED, false)
                .with(FREQUENCY, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TUNED, FREQUENCY);
    }

    public static int targetFrequency(BlockPos pos) {
        return Math.floorMod(pos.getX() * 7 + pos.getY() * 13 + pos.getZ() * 5, 3) + 1;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (state.get(TUNED)) {
            player.sendMessage(Text.translatable("message.farsight.relay.already_tuned").formatted(Formatting.AQUA), true);
            return ActionResult.SUCCESS;
        }

        int nextFreq = (state.get(FREQUENCY) + 1) % 4;
        int target = targetFrequency(pos);
        boolean nowTuned = (nextFreq == target);

        BlockState newState = state.with(FREQUENCY, nextFreq).with(TUNED, nowTuned);
        world.setBlockState(pos, newState, Block.NOTIFY_ALL);

        if (nowTuned) {
            world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.0f, 1.4f);
            if (world instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 14, 0.25, 0.25, 0.25, 0.03);
                unlockNearbyBarriersIfAllRelaysTuned(sw, pos);
            }
            player.sendMessage(Text.translatable("message.farsight.relay.tuned").formatted(Formatting.GREEN), true);
        } else {
            float pitch = 0.7f + nextFreq * 0.2f;
            world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.8f, pitch);
            player.sendMessage(Text.translatable("message.farsight.relay.tuning", nextFreq, target).formatted(Formatting.YELLOW), true);
        }

        return ActionResult.SUCCESS;
    }

    private void unlockNearbyBarriersIfAllRelaysTuned(ServerWorld world, BlockPos origin) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int dx = -12; dx <= 12; dx++) {
            for (int dz = -12; dz <= 12; dz++) {
                for (int dy = -24; dy <= 24; dy++) {
                    mutable.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockState s = world.getBlockState(mutable);
                    if (s.isOf(ModBlocks.SIGNAL_RELAY) && !s.get(TUNED)) {
                        return; // Still untuned relays left
                    }
                }
            }
        }

        // All relays in tower tuned -> remove all TowerBarrierBlocks (restore ladder if one is directly below)
        for (int dx = -12; dx <= 12; dx++) {
            for (int dz = -12; dz <= 12; dz++) {
                for (int dy = -24; dy <= 24; dy++) {
                    mutable.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockState s = world.getBlockState(mutable);
                    if (s.isOf(ModBlocks.TOWER_BARRIER)) {
                        BlockState below = world.getBlockState(mutable.down());
                        if (below.isOf(net.minecraft.block.Blocks.LADDER)) {
                            world.setBlockState(mutable, below, Block.NOTIFY_ALL);
                        } else {
                            world.setBlockState(mutable, net.minecraft.block.Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                        }
                    }
                }
            }
        }
        world.playSound(null, origin, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 0.8f);
    }
}
