package com.farsight.block;

import com.farsight.data.DiscoveryState;
import com.farsight.data.PoiData;
import com.farsight.data.PoiType;
import com.farsight.network.ModNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class CheckpointFlagBlock extends Block {
    public static final BooleanProperty CLAIMED = BooleanProperty.of("claimed");

    public CheckpointFlagBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(CLAIMED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CLAIMED);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (!(world instanceof ServerWorld serverWorld) || !(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }

        if (state.get(CLAIMED)) {
            serverPlayer.sendMessage(Text.translatable("message.farsight.checkpoint.already_claimed").formatted(Formatting.AQUA), true);
            return ActionResult.SUCCESS;
        }

        Box box = new Box(
                pos.getX() - 20, pos.getY() - 10, pos.getZ() - 20,
                pos.getX() + 20, pos.getY() + 14, pos.getZ() + 20
        );
        List<IllagerEntity> guards = serverWorld.getEntitiesByClass(IllagerEntity.class, box, IllagerEntity::isAlive);
        if (!guards.isEmpty()) {
            serverPlayer.sendMessage(
                    Text.translatable("message.farsight.checkpoint.locked", guards.size()).formatted(Formatting.RED),
                    true
            );
            world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.BLOCKS, 0.8f, 0.7f);
            return ActionResult.SUCCESS;
        }

        world.setBlockState(pos, state.with(CLAIMED, true), Block.NOTIFY_ALL);
        world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0f, 1.15f);
        serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                25, 0.6, 0.8, 0.6, 0.05);

        DiscoveryState discovery = DiscoveryState.get(serverWorld);
        discovery.removePoisIf(p -> p.type() == PoiType.CHECKPOINT_LOCKED &&
                Math.hypot(p.x() - pos.getX(), p.z() - pos.getZ()) < 24.0);
        String id = "checkpoint_claimed_" + pos.getX() + "_" + pos.getZ();
        discovery.addPoi(new PoiData(
                id,
                "Liberated Checkpoint",
                PoiType.CHECKPOINT_CLAIMED,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                "checkpoint",
                serverWorld.getBiome(pos).getIdAsString()
        ));
        ModNetworking.syncAllPlayers(serverWorld);

        serverPlayer.sendMessage(Text.translatable("message.farsight.checkpoint.liberated").formatted(Formatting.GREEN), true);
        return ActionResult.SUCCESS;
    }
}
