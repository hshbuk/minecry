package com.farsight.world;

import com.farsight.FarSightMod;
import com.farsight.data.DiscoveryState;
import com.farsight.data.PoiData;
import com.farsight.data.PoiType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

import java.util.ArrayList;
import java.util.List;

public class TowerLocator {
    public static final RegistryKey<Structure> PILLAGER_TOWER_KEY = RegistryKey.of(
            RegistryKeys.STRUCTURE,
            Identifier.of(FarSightMod.MOD_ID, "pillager_tower")
    );

    public static BlockPos findNearestUnclaimedTower(ServerWorld world, BlockPos origin, int searchRadiusChunks) {
        var registry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
        var entryOpt = registry.getEntry(PILLAGER_TOWER_KEY);
        if (entryOpt.isEmpty()) return null;

        var result = world.getChunkManager().getChunkGenerator().locateStructure(
                world,
                RegistryEntryList.of(entryOpt.get()),
                origin,
                searchRadiusChunks,
                false
        );
        return result != null ? result.getFirst() : null;
    }

    /**
     * Ensures at least the nearest unclaimed Pillager Towers around the player
     * are marked on the map/compass so players always know where to explore next.
     */
    public static void ensureNearbyTowersMarked(ServerWorld world, BlockPos playerPos) {
        DiscoveryState state = DiscoveryState.get(world);
        List<BlockPos> offsets = List.of(
                playerPos,
                playerPos.add(450, 0, 450),
                playerPos.add(-450, 0, 450),
                playerPos.add(450, 0, -450),
                playerPos.add(-450, 0, -450)
        );

        List<BlockPos> found = new ArrayList<>();
        for (BlockPos origin : offsets) {
            BlockPos tower = findNearestUnclaimedTower(world, origin, 50);
            if (tower == null) continue;
            boolean duplicate = found.stream().anyMatch(p -> p.getSquaredDistance(tower) < 100 * 100);
            if (!duplicate) {
                found.add(tower);
            }
        }

        for (BlockPos tower : found) {
            String claimedId = "claimed_" + tower.getX() + "_" + tower.getZ();
            String unclaimedId = "tower_" + tower.getX() + "_" + tower.getZ();
            boolean alreadyMarked = state.getPois().stream().anyMatch(p ->
                    p.id().equals(unclaimedId) ||
                            (p.type() == PoiType.TOWER_CLAIMED && Math.hypot(p.x() - tower.getX(), p.z() - tower.getZ()) < 48)
            );
            if (!alreadyMarked && state.getPois().stream().noneMatch(p -> p.id().equals(claimedId))) {
                String biomeId = world.getBiome(tower).getIdAsString();
                TowerTheme theme = TowerTheme.fromBiome(world.getBiome(tower));
                state.addPoi(new PoiData(
                        unclaimedId,
                        theme.defaultRegionName + " Tower",
                        PoiType.TOWER_UNCLAIMED,
                        tower.getX(),
                        Math.max(tower.getY(), 64) + 20,
                        tower.getZ(),
                        "unclaimed",
                        biomeId
                ));
            }
        }
    }
}
