package com.farsight.world;

import com.farsight.block.TowerTerminalBlock;
import com.farsight.block.entity.TowerTerminalBlockEntity;
import com.farsight.data.DiscoveryState;
import com.farsight.data.PoiData;
import com.farsight.data.PoiType;
import com.farsight.data.RegionData;
import com.farsight.network.ModNetworking;
import com.farsight.registry.ModBlocks;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class TowerActivationHandler {

    public static void tryActivateTower(ServerWorld world, ServerPlayerEntity player, BlockPos pos) {
        if (player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 100.0) {
            return;
        }
        BlockState state = world.getBlockState(pos);
        if (!state.isOf(ModBlocks.TOWER_TERMINAL)) {
            return;
        }
        if (!(world.getBlockEntity(pos) instanceof TowerTerminalBlockEntity terminal)) {
            return;
        }
        if (state.get(TowerTerminalBlock.ACTIVATED) || terminal.isActivated()) {
            return;
        }
        if (TowerTerminalBlock.countNearbyGuards(world, pos) > 0 || TowerTerminalBlock.countUntunedRelays(world, pos) > 0) {
            return;
        }

        // 1. Activate Terminal Block & BlockEntity
        world.setBlockState(pos, state.with(TowerTerminalBlock.ACTIVATED, true), Block.NOTIFY_ALL);
        terminal.setActivated(true);

        // 2. Dramatic Audio + Particle Beacon Burst
        world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1.5f, 1.0f);
        world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        for (int dy = 1; dy <= 28; dy++) {
            world.spawnParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + dy, pos.getZ() + 0.5,
                    4, 0.2, 0.2, 0.2, 0.02);
        }

        // 3. Unlock Region in DiscoveryState
        DiscoveryState discovery = DiscoveryState.get(world);
        String regionId = "region_" + pos.getX() + "_" + pos.getZ();
        RegionData region = new RegionData(
                regionId,
                terminal.getRegionName(),
                terminal.getBiomeTheme(),
                pos.getX(),
                pos.getZ(),
                terminal.getRadius(),
                world.getTime(),
                player.getName().getString()
        );
        discovery.addRegion(region);

        // Replace any unclaimed tower marker nearby with claimed tower marker
        discovery.removePoi("tower_" + pos.getX() + "_" + pos.getZ());
        discovery.addPoi(new PoiData(
                "claimed_" + pos.getX() + "_" + pos.getZ(),
                terminal.getRegionName() + " Tower",
                PoiType.TOWER_CLAIMED,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                regionId,
                world.getBiome(pos).getIdAsString()
        ));

        // 4. Scan all special POIs in the newly unlocked region!
        int revealed = RegionScanner.scanRegion(world, discovery, region);

        // 5. Sync to all players and trigger Region Unlocked banner
        ModNetworking.syncAllPlayers(world);
        for (ServerPlayerEntity p : world.getPlayers()) {
            ServerPlayNetworking.send(p, new ModNetworking.RegionUnlockedPayload(
                    region.name(),
                    region.biomeTheme(),
                    revealed
            ));
        }
    }
}
