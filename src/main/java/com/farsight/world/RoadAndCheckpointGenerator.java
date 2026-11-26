package com.farsight.world;

import com.farsight.data.DiscoveryState;
import com.farsight.data.PoiData;
import com.farsight.data.PoiType;
import com.farsight.data.RegionData;
import com.farsight.data.RoadData;
import com.farsight.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates terrain-following Roads and fortified Pillager Road Checkpoints
 * inside an unlocked region, connecting the Tower to Checkpoints and Villages.
 */
public class RoadAndCheckpointGenerator {

    public static List<PoiData> generateRoadsAndCheckpoints(ServerWorld world, DiscoveryState state, RegionData region) {
        List<PoiData> createdCheckpoints = new ArrayList<>();
        TowerTheme theme = TowerTheme.fromBiome(world.getBiome(new BlockPos(region.centerX(), 64, region.centerZ())));

        // Place 2 Road Checkpoints at ~210 blocks along two different directions
        int[][] dirs = {{210, 80}, {-190, -110}};
        List<BlockPos> roadNodes = new ArrayList<>();
        roadNodes.add(new BlockPos(region.centerX(), 64, region.centerZ()));

        for (int i = 0; i < dirs.length; i++) {
            int cx = region.centerX() + dirs[i][0];
            int cz = region.centerZ() + dirs[i][1];
            if (!region.contains(cx, cz)) continue;

            // Force chunk load & find ground height
            int cy = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, cx, cz);
            if (cy <= world.getSeaLevel()) continue;

            BlockPos cpPos = new BlockPos(cx, cy, cz);
            buildCheckpoint(world, cpPos, theme);

            String cpId = "cp_" + cx + "_" + cz;
            PoiData cpPoi = new PoiData(
                    cpId,
                    "Road Checkpoint",
                    PoiType.CHECKPOINT_LOCKED,
                    cx, cy + 1, cz,
                    region.id(),
                    world.getBiome(cpPos).getIdAsString()
            );
            state.addPoi(cpPoi);
            createdCheckpoints.add(cpPoi);
            roadNodes.add(cpPos);

            // Build physical road + record RoadData from Tower -> Checkpoint
            buildRoad(world, region.centerX(), region.centerZ(), cx, cz, theme);
            state.addRoad(new RoadData(
                    "road_tower_cp_" + i + "_" + cx + "_" + cz,
                    region.centerX(), region.centerZ(),
                    cx, cz,
                    region.id()
            ));
        }

        // Also connect the Tower to any Villages inside this region with a road!
        for (PoiData poi : state.getPois()) {
            if (poi.type() == PoiType.VILLAGE && region.contains(poi.x(), poi.z())) {
                buildRoad(world, region.centerX(), region.centerZ(), poi.x(), poi.z(), theme);
                state.addRoad(new RoadData(
                        "road_tower_vil_" + poi.x() + "_" + poi.z(),
                        region.centerX(), region.centerZ(),
                        poi.x(), poi.z(),
                        region.id()
                ));
            }
        }

        return createdCheckpoints;
    }

    public static void buildCheckpoint(ServerWorld world, BlockPos center, TowerTheme theme) {
        int bx = center.getX();
        int by = center.getY();
        int bz = center.getZ();

        BlockState roadMat = theme.primaryStone.getDefaultState();
        BlockState wallMat = theme.accentStone.getDefaultState();
        BlockState fence = theme.fence.getDefaultState();

        // Flatten 7x7 base pad
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -2; dy <= 0; dy++) {
                    world.setBlockState(new BlockPos(bx + dx, by + dy, bz + dz), roadMat, 2);
                }
                for (int dy = 1; dy <= 4; dy++) {
                    world.setBlockState(new BlockPos(bx + dx, by + dy, bz + dz), Blocks.AIR.getDefaultState(), 2);
                }
            }
        }

        // Sandbag/stone perimeter walls with open road gates on N/S
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                boolean edge = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                boolean gate = Math.abs(dx) <= 1 && Math.abs(dz) == 3;
                if (edge && !gate) {
                    world.setBlockState(new BlockPos(bx + dx, by + 1, bz + dz), wallMat, 2);
                    if (Math.abs(dx) == 3 && Math.abs(dz) == 3) {
                        world.setBlockState(new BlockPos(bx + dx, by + 2, bz + dz), fence, 2);
                        world.setBlockState(new BlockPos(bx + dx, by + 3, bz + dz), theme.lantern.getDefaultState(), 2);
                    }
                }
            }
        }

        // Guard booth on West side
        for (int dy = 1; dy <= 3; dy++) {
            world.setBlockState(new BlockPos(bx - 2, by + dy, bz - 2), theme.pillar.getDefaultState(), 2);
            world.setBlockState(new BlockPos(bx - 2, by + dy, bz + 2), theme.pillar.getDefaultState(), 2);
        }
        for (int dz = -2; dz <= 2; dz++) {
            world.setBlockState(new BlockPos(bx - 2, by + 4, bz + dz), theme.planks.getDefaultState(), 2);
        }

        // Loot Chest
        BlockPos chestPos = new BlockPos(bx - 2, by + 1, bz);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(LootTables.PILLAGER_OUTPOST_CHEST, world.random.nextLong());
        }

        // Checkpoint Flag Block on East side
        world.setBlockState(new BlockPos(bx + 2, by + 1, bz), wallMat, 2);
        world.setBlockState(new BlockPos(bx + 2, by + 2, bz), ModBlocks.CHECKPOINT_FLAG.getDefaultState(), 2);

        // Spawn 3 Pillager/Vindicator Guards
        spawnGuard(world, EntityType.PILLAGER, bx, by + 1, bz + 1);
        spawnGuard(world, EntityType.PILLAGER, bx + 1, by + 1, bz - 1);
        spawnGuard(world, EntityType.VINDICATOR, bx - 1, by + 1, bz);
    }

    private static void spawnGuard(ServerWorld world, EntityType<? extends IllagerEntity> type, int x, int y, int z) {
        IllagerEntity mob = type.create(world);
        if (mob != null) {
            mob.setPersistent();
            mob.refreshPositionAndAngles(x + 0.5, y, z + 0.5, 0, 0);
            mob.initialize(world, world.getLocalDifficulty(new BlockPos(x, y, z)), SpawnReason.STRUCTURE, null);
            world.spawnEntityAndPassengers(mob);
        }
    }

    private static void buildRoad(ServerWorld world, int x1, int z1, int x2, int z2, TowerTheme theme) {
        double dist = Math.hypot(x2 - x1, z2 - z1);
        int steps = (int) Math.max(1, dist);
        BlockState roadPrimary = (theme == TowerTheme.DESERT || theme == TowerTheme.BADLANDS)
                ? Blocks.SMOOTH_SANDSTONE.getDefaultState() : Blocks.GRAVEL.getDefaultState();
        BlockState roadBorder = Blocks.COBBLESTONE.getDefaultState();

        for (int i = 6; i <= steps - 4; i++) {
            double t = i / (double) steps;
            int rx = (int) Math.round(x1 + (x2 - x1) * t);
            int rz = (int) Math.round(z1 + (z2 - z1) * t);

            for (int ox = -1; ox <= 1; ox++) {
                for (int oz = -1; oz <= 1; oz++) {
                    int px = rx + ox;
                    int pz = rz + oz;
                    int py = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, px, pz) - 1;
                    if (py < world.getSeaLevel()) continue;
                    BlockPos pos = new BlockPos(px, py, pz);
                    BlockState existing = world.getBlockState(pos);
                    if (existing.isAir() || existing.isOf(Blocks.WATER)) continue;
                    boolean isCenter = (ox == 0 || oz == 0);
                    world.setBlockState(pos, isCenter ? roadPrimary : roadBorder, 2);
                }
            }
        }
    }
}
