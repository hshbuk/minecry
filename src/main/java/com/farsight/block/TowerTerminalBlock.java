package com.farsight.block;

import com.farsight.block.entity.TowerTerminalBlockEntity;
import com.farsight.network.ModNetworking;
import com.farsight.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TowerTerminalBlock extends BlockWithEntity {
    public static final MapCodec<TowerTerminalBlock> CODEC = createCodec(TowerTerminalBlock::new);
    public static final BooleanProperty ACTIVATED = BooleanProperty.of("activated");
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public TowerTerminalBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(ACTIVATED, false)
                .with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED, FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TowerTerminalBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (!(world instanceof ServerWorld serverWorld) || !(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof TowerTerminalBlockEntity terminal)) {
            return ActionResult.PASS;
        }

        if (state.get(ACTIVATED) || terminal.isActivated()) {
            serverPlayer.sendMessage(
                    Text.translatable("message.farsight.tower.already_active", terminal.getRegionName())
                            .formatted(Formatting.AQUA),
                    true
            );
            world.playSound(null, pos, SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS, 0.7f, 1.3f);
            return ActionResult.SUCCESS;
        }

        int guardsRemaining = countNearbyGuards(serverWorld, pos);
        int relaysRemaining = countUntunedRelays(serverWorld, pos);

        if (guardsRemaining > 0 || relaysRemaining > 0) {
            serverPlayer.sendMessage(
                    Text.translatable("message.farsight.tower.locked", guardsRemaining, relaysRemaining)
                            .formatted(Formatting.RED),
                    true
            );
            world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.BLOCKS, 0.8f, 0.7f);
            return ActionResult.SUCCESS;
        }

        // Open the Far Cry Signal Alignment Puzzle Screen
        ServerPlayNetworking.send(serverPlayer, new ModNetworking.OpenPuzzlePayload(
                pos,
                terminal.getBiomeTheme(),
                terminal.getRegionName(),
                terminal.getPuzzleSeed()
        ));
        return ActionResult.SUCCESS;
    }

    public static int countNearbyGuards(ServerWorld world, BlockPos terminalPos) {
        Box box = new Box(
                terminalPos.getX() - 18, terminalPos.getY() - 40, terminalPos.getZ() - 18,
                terminalPos.getX() + 18, terminalPos.getY() + 10, terminalPos.getZ() + 18
        );
        List<IllagerEntity> illagers = world.getEntitiesByClass(IllagerEntity.class, box, IllagerEntity::isAlive);
        return illagers.size();
    }

    public static int countUntunedRelays(ServerWorld world, BlockPos terminalPos) {
        int untuned = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                for (int dy = -34; dy <= 4; dy++) {
                    mutable.set(terminalPos.getX() + dx, terminalPos.getY() + dy, terminalPos.getZ() + dz);
                    BlockState s = world.getBlockState(mutable);
                    if (s.isOf(ModBlocks.SIGNAL_RELAY) && !s.get(SignalRelayBlock.TUNED)) {
                        untuned++;
                    }
                }
            }
        }
        return untuned;
    }
}
