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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates smoothed, walkable Roads with bridges, embankments, and lantern posts,
 * plus fortified Pillager Road Checkpoints connected in a loop (no dead ends).
 */
public class RoadAndCheckpointGenerator {

    public static List<PoiData> generateRoadsAndCheckpoints(ServerWorld world, DiscoveryState state, RegionData region) {
        List<PoiData> createdCheckpoints = new ArrayList<>();
        TowerTheme theme = TowerTheme.fromBiome(world.getBiome(new BlockPos(region.centerX(), 64, region.centerZ())));

        int[][] dirs = {{210, 80}, {-190, -110}};
        List<BlockPos> cpPositions = new ArrayList<>();

        for (int i = 0; i < dirs.length; i++) {
            int cx = region.centerX() + dirs[i][0];
            int cz = region.centerZ() + dirs[i][1];
            if (!region.contains(cx, cz)) continue;

            world.getChunk(cx >> 4, cz >> 4);
            int cy = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, cx, cz);
            if (cy <= world.getSeaLevel()) continue;

            BlockPos cpPos = new BlockPos(cx, cy, cz);
            buildCheckpoint(world, cpPos, theme, region.tier());

            String cpId = "cp_" + cx + "_" + cz;
            PoiData cpPoi = new PoiData(
                    cpId,
                    "Road Checkpoint [★" + region.tier() + "]",
                    PoiType.CHECKPOINT_LOCKED,
                    cx, cy + 1, cz,
                    region.id(),
                    world.getBiome(cpPos).getIdAsString()
            );
            state.addPoi(cpPoi);
            createdCheckpoints.add(cpPoi);
            cpPositions.add(cpPos);

            // Road: Tower -> Checkpoint
            buildRoad(world, region.centerX(), region.centerZ(), cx, cz, theme);
            state.addRoad(new RoadData(
                    "road_tower_cp_" + i + "_" + cx + "_" + cz,
                    region.centerX(), region.centerZ(),
                    cx, cz,
                    region.id()
            ));
        }

        // Connect Checkpoints to Villages (or to each other) so roads form a real network with no dead ends
        List<PoiData> villages = new ArrayList<>();
        for (PoiData poi : state.getPois()) {
            if (poi.type() == PoiType.VILLAGE && region.contains(poi.x(), poi.z())) {
                villages.add(poi);
                buildRoad(world, region.centerX(), region.centerZ(), poi.x(), poi.z(), theme);
                state.addRoad(new RoadData(
                        "road_tower_vil_" + poi.x() + "_" + poi.z(),
                        region.centerX(), region.centerZ(),
                        poi.x(), poi.z(),
                        region.id()
                ));
            }
        }

        if (cpPositions.size() >= 2) {
            BlockPos a = cpPositions.get(0);
            BlockPos b = cpPositions.get(1);
            buildRoad(world, a.getX(), a.getZ(), b.getX(), b.getZ(), theme);
            state.addRoad(new RoadData(
                    "road_cp_loop_" + a.getX() + "_" + b.getX(),
                    a.getX(), a.getZ(),
                    b.getX(), b.getZ(),
                    region.id()
            ));
        }
        if (!cpPositions.isEmpty() && !villages.isEmpty()) {
            BlockPos cp = cpPositions.get(0);
            PoiData v = villages.get(0);
            buildRoad(world, cp.getX(), cp.getZ(), v.x(), v.z(), theme);
            state.addRoad(new RoadData(
                    "road_cp_vil_" + cp.getX() + "_" + v.x(),
                    cp.getX(), cp.getZ(),
                    v.x(), v.z(),
                    region.id()
            ));
        }

        return createdCheckpoints;
    }

    public static void buildCheckpoint(ServerWorld world, BlockPos center, TowerTheme theme) {
        buildCheckpoint(world, center, theme, TierCalculator.getTierForPos(center));
    }

    public static void buildCheckpoint(ServerWorld world, BlockPos center, TowerTheme theme, int tier) {
        int bx = center.getX();
        int by = center.getY();
        int bz = center.getZ();

        BlockState roadMat = theme.primaryStone.getDefaultState();
        BlockState wallMat = theme.accentStone.getDefaultState();
        BlockState fence = theme.fence.getDefaultState();

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -3; dy <= 0; dy++) {
                    world.setBlockState(new BlockPos(bx + dx, by + dy, bz + dz), roadMat, 2);
                }
                for (int dy = 1; dy <= 4; dy++) {
                    world.setBlockState(new BlockPos(bx + dx, by + dy, bz + dz), Blocks.AIR.getDefaultState(), 2);
                }
            }
        }

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

        for (int dy = 1; dy <= 3; dy++) {
            world.setBlockState(new BlockPos(bx - 2, by + dy, bz - 2), theme.pillar.getDefaultState(), 2);
            world.setBlockState(new BlockPos(bx - 2, by + dy, bz + 2), theme.pillar.getDefaultState(), 2);
        }
        for (int dz = -2; dz <= 2; dz++) {
            world.setBlockState(new BlockPos(bx - 2, by + 4, bz + dz), theme.planks.getDefaultState(), 2);
        }

        BlockPos chestPos = new BlockPos(bx - 2, by + 1, bz);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(LootTables.PILLAGER_OUTPOST_CHEST, world.random.nextLong());
        }

        world.setBlockState(new BlockPos(bx + 2, by + 1, bz), wallMat, 2);
        world.setBlockState(new BlockPos(bx + 2, by + 2, bz), ModBlocks.CHECKPOINT_FLAG.getDefaultState(), 2);

        spawnGuard(world, EntityType.PILLAGER, bx, by + 1, bz + 1, tier);
        spawnGuard(world, EntityType.PILLAGER, bx + 1, by + 1, bz - 1, tier);
        spawnGuard(world, EntityType.VINDICATOR, bx - 1, by + 1, bz, tier);
        if (tier >= 2) {
            spawnGuard(world, EntityType.PILLAGER, bx, by + 1, bz - 1, tier);
        }
        if (tier >= 4) {
            spawnGuard(world, EntityType.EVOKER, bx + 1, by + 1, bz + 1, tier);
        }
    }

    private static void spawnGuard(ServerWorld world, EntityType<? extends IllagerEntity> type, int x, int y, int z, int tier) {
        IllagerEntity mob = type.create(world);
        if (mob != null) {
            mob.setPersistent();
            mob.refreshPositionAndAngles(x + 0.5, y, z + 0.5, 0, 0);
            mob.initialize(world, world.getLocalDifficulty(new BlockPos(x, y, z)), SpawnReason.STRUCTURE, null);
            TierCalculator.scaleHostileMob(mob, tier);
            world.spawnEntityAndPassengers(mob);
        }
    }

    /**
     * Builds a smooth, walkable 3-wide road with slope clamping (max 1 block per step),
     * stone-brick bridges over water, solid embankments over dips, air clearance, and lantern posts.
     */
    private static void buildRoad(ServerWorld world, int x1, int z1, int x2, int z2, TowerTheme theme) {
        double dist = Math.hypot(x2 - x1, z2 - z1);
        int steps = (int) Math.max(10, dist);
        int seaLevel = world.getSeaLevel();

        int[] xs = new int[steps + 1];
        int[] zs = new int[steps + 1];
        int[] rawY = new int[steps + 1];
        boolean[] isWater = new boolean[steps + 1];

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            xs[i] = (int) Math.round(x1 + (x2 - x1) * t);
            zs[i] = (int) Math.round(z1 + (z2 - z1) * t);
            world.getChunk(xs[i] >> 4, zs[i] >> 4);
            int top = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, xs[i], zs[i]) - 1;
            isWater[i] = top <= seaLevel || world.getBlockState(new BlockPos(xs[i], top, zs[i])).isOf(Blocks.WATER);
            rawY[i] = isWater[i] ? seaLevel + 1 : top;
        }

        // Smooth & clamp slope to max +-1 block per step so it's always walkable!
        int[] roadY = new int[steps + 1];
        roadY[0] = rawY[0];
        for (int i = 1; i <= steps; i++) {
            int sum = 0, count = 0;
            for (int k = -2; k <= 2; k++) {
                int idx = Math.max(0, Math.min(steps, i + k));
                sum += rawY[idx];
                count++;
            }
            int avg = Math.round(sum / (float) count);
            if (isWater[i]) avg = Math.max(avg, seaLevel + 1);
            int diff = avg - roadY[i - 1];
            if (diff > 1) roadY[i] = roadY[i - 1] + 1;
            else if (diff < -1) roadY[i] = roadY[i - 1] - 1;
            else roadY[i] = avg;
        }

        BlockState roadPrimary = (theme == TowerTheme.DESERT || theme == TowerTheme.BADLANDS)
                ? Blocks.SMOOTH_SANDSTONE.getDefaultState() : Blocks.GRAVEL.getDefaultState();
        BlockState roadEdge = theme.primaryStone.getDefaultState();
        BlockState bridgeDeck = theme.planks.getDefaultState();
        BlockState bridgeEdge = Blocks.STONE_BRICKS.getDefaultState();
        BlockState support = Blocks.COBBLESTONE.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();

        // Perpendicular direction for road width
        double dx = (x2 - x1) / dist;
        double dz = (z2 - z1) / dist;
        double perpX = -dz;
        double perpZ = dx;

        for (int i = 6; i <= steps - 5; i++) {
            int y = roadY[i];
            boolean bridge = isWater[i] || (y - rawY[i] >= 2);

            for (int w = -1; w <= 1; w++) {
                int px = (int) Math.round(xs[i] + perpX * w);
                int pz = (int) Math.round(zs[i] + perpZ * w);
                BlockState surface = bridge ? (w == 0 ? bridgeDeck : bridgeEdge) : (w == 0 ? roadPrimary : roadEdge);

                world.setBlockState(new BlockPos(px, y, pz), surface, 2);

                // Clear headroom above the road
                for (int dy = 1; dy <= 3; dy++) {
                    world.setBlockState(new BlockPos(px, y + dy, pz), air, 2);
                }

                // Fill solid embankment/pillars underneath so there are zero floating gaps
                int ground = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, px, pz) - 1;
                if (ground < y) {
                    int fillDepth = bridge && (w == 0 || i % 5 != 0) ? 1 : Math.min(12, y - ground + 1);
                    for (int dy = 1; dy <= fillDepth; dy++) {
                        world.setBlockState(new BlockPos(px, y - dy, pz), support, 2);
                    }
                }

                // Bridge railings
                if (bridge && Math.abs(w) == 1) {
                    world.setBlockState(new BlockPos(px, y + 1, pz), theme.fence.getDefaultState(), 2);
                }
            }

            // Roadside lantern post every 26 blocks
            if (!bridge && i % 26 == 0) {
                int lx = (int) Math.round(xs[i] + perpX * 2.0);
                int lz = (int) Math.round(zs[i] + perpZ * 2.0);
                world.setBlockState(new BlockPos(lx, y, lz), roadEdge, 2);
                world.setBlockState(new BlockPos(lx, y + 1, lz), theme.fence.getDefaultState(), 2);
                world.setBlockState(new BlockPos(lx, y + 2, lz), theme.fence.getDefaultState(), 2);
                world.setBlockState(new BlockPos(lx, y + 3, lz), theme.lantern.getDefaultState(), 2);
            }
        }
    }
}
