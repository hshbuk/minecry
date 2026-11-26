package com.farsight.world;

import com.farsight.data.DiscoveryState;
import com.farsight.data.PoiData;
import com.farsight.data.PoiType;
import com.farsight.data.RegionData;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;

import java.util.List;

public class RegionScanner {

    public static int scanRegion(ServerWorld world, DiscoveryState state, RegionData region) {
        int addedCount = 0;
        BlockPos center = new BlockPos(region.centerX(), 64, region.centerZ());
        int r = region.radius();

        // Sample points: center + 4 quadrants to catch multiple structures in the region
        List<BlockPos> samplePoints = List.of(
                center,
                center.add(r / 2, 0, r / 2),
                center.add(-r / 2, 0, r / 2),
                center.add(r / 2, 0, -r / 2),
                center.add(-r / 2, 0, -r / 2)
        );

        for (BlockPos origin : samplePoints) {
            addedCount += scanTag(world, state, region, origin, StructureTags.VILLAGE, PoiType.VILLAGE, "Village");
            addedCount += scanTag(world, state, region, origin, StructureTags.RUINED_PORTAL, PoiType.RUINED_PORTAL, "Ruined Portal");
            addedCount += scanTag(world, state, region, origin, StructureTags.MINESHAFT, PoiType.MINESHAFT, "Mineshaft");

            addedCount += scanKey(world, state, region, origin, StructureKeys.PILLAGER_OUTPOST, PoiType.PILLAGER_OUTPOST, "Pillager Outpost");
            addedCount += scanKey(world, state, region, origin, StructureKeys.DESERT_PYRAMID, PoiType.TEMPLE, "Desert Pyramid");
            addedCount += scanKey(world, state, region, origin, StructureKeys.JUNGLE_PYRAMID, PoiType.TEMPLE, "Jungle Temple");
            addedCount += scanKey(world, state, region, origin, StructureKeys.SWAMP_HUT, PoiType.TEMPLE, "Witch Hut");
            addedCount += scanKey(world, state, region, origin, StructureKeys.IGLOO, PoiType.TEMPLE, "Igloo");
            addedCount += scanKey(world, state, region, origin, StructureKeys.ANCIENT_CITY, PoiType.ANCIENT_CITY, "Ancient City");
            addedCount += scanKey(world, state, region, origin, StructureKeys.TRIAL_CHAMBERS, PoiType.TRIAL_CHAMBERS, "Trial Chambers");
            addedCount += scanKey(world, state, region, origin, StructureKeys.MONUMENT, PoiType.OCEAN_MONUMENT, "Ocean Monument");
            addedCount += scanKey(world, state, region, origin, StructureKeys.MANSION, PoiType.WOODLAND_MANSION, "Woodland Mansion");
            addedCount += scanKey(world, state, region, origin, StructureKeys.STRONGHOLD, PoiType.STRONGHOLD, "Stronghold");
        }

        // Also reveal the next nearest unclaimed Pillager Tower on the frontier (even slightly outside the radius)
        BlockPos nextTower = TowerLocator.findNearestUnclaimedTower(world, center, 60);
        if (nextTower != null) {
            int dx = nextTower.getX() - region.centerX();
            int dz = nextTower.getZ() - region.centerZ();
            if (dx * dx + dz * dz > 64 * 64) {
                String id = "tower_" + nextTower.getX() + "_" + nextTower.getZ();
                if (state.getPois().stream().noneMatch(p -> p.id().equals(id))) {
                    state.addPoi(new PoiData(
                            id,
                            "Unclaimed Pillager Tower",
                            PoiType.TOWER_UNCLAIMED,
                            nextTower.getX(),
                            nextTower.getY() + 20,
                            nextTower.getZ(),
                            region.id(),
                            world.getBiome(nextTower).getIdAsString()
                    ));
                    addedCount++;
                }
            }
        }

        // Scan for special underground/surface biomes (Lush Caves, Deep Dark, Cherry Grove, Mushroom Fields)
        addedCount += scanSpecialBiomes(world, state, region);

        return addedCount;
    }

    private static int scanTag(ServerWorld world, DiscoveryState state, RegionData region,
                               BlockPos origin, TagKey<Structure> tag, PoiType type, String label) {
        var registry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
        var listOpt = registry.getEntryList(tag);
        if (listOpt.isEmpty()) return 0;

        var result = world.getChunkManager().getChunkGenerator().locateStructure(
                world, listOpt.get(), origin, 32, false
        );
        if (result == null) return 0;

        return recordIfInRegion(world, state, region, result.getFirst(), type, label);
    }

    private static int scanKey(ServerWorld world, DiscoveryState state, RegionData region,
                               BlockPos origin, RegistryKey<Structure> key, PoiType type, String label) {
        var registry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
        var entryOpt = registry.getEntry(key);
        if (entryOpt.isEmpty()) return 0;

        var result = world.getChunkManager().getChunkGenerator().locateStructure(
                world, RegistryEntryList.of(entryOpt.get()), origin, 32, false
        );
        if (result == null) return 0;

        return recordIfInRegion(world, state, region, result.getFirst(), type, label);
    }

    private static int recordIfInRegion(ServerWorld world, DiscoveryState state, RegionData region,
                                        BlockPos pos, PoiType type, String label) {
        if (!region.contains(pos.getX(), pos.getZ())) {
            return 0;
        }
        String id = type.name().toLowerCase() + "_" + (pos.getX() >> 4) + "_" + (pos.getZ() >> 4);
        if (state.getPois().stream().anyMatch(p -> p.id().equals(id))) {
            return 0;
        }
        int y = pos.getY() > 0 ? pos.getY() : 64;
        String biomeId = world.getBiome(pos).getIdAsString();
        state.addPoi(new PoiData(id, label, type, pos.getX(), y, pos.getZ(), region.id(), biomeId));
        return 1;
    }

    private static int scanSpecialBiomes(ServerWorld world, DiscoveryState state, RegionData region) {
        int added = 0;
        int step = 128;
        int r = region.radius();
        for (int dx = -r + 64; dx <= r - 64; dx += step) {
            for (int dz = -r + 64; dz <= r - 64; dz += step) {
                int x = region.centerX() + dx;
                int z = region.centerZ() + dz;
                if (!region.contains(x, z)) continue;

                for (int y : new int[]{0, 48, 80}) {
                    BlockPos checkPos = new BlockPos(x, y, z);
                    RegistryEntry<Biome> biome = world.getBiome(checkPos);
                    String label = null;
                    if (biome.matchesKey(BiomeKeys.LUSH_CAVES)) label = "Lush Caves";
                    else if (biome.matchesKey(BiomeKeys.DEEP_DARK)) label = "Deep Dark";
                    else if (biome.matchesKey(BiomeKeys.DRIPSTONE_CAVES)) label = "Dripstone Cavern";
                    else if (biome.matchesKey(BiomeKeys.CHERRY_GROVE)) label = "Cherry Grove";
                    else if (biome.matchesKey(BiomeKeys.MUSHROOM_FIELDS)) label = "Mushroom Island";

                    if (label != null) {
                        final String caveLabel = label;
                        boolean nearbyExists = state.getPois().stream().anyMatch(p ->
                                p.type() == PoiType.SPECIAL_CAVE &&
                                        p.name().equals(caveLabel) &&
                                        Math.hypot(p.x() - x, p.z() - z) < 280.0
                        );
                        if (!nearbyExists) {
                            String id = "biome_" + label.toLowerCase().replace(" ", "_") + "_" + (x >> 6) + "_" + (z >> 6);
                            state.addPoi(new PoiData(id, label, PoiType.SPECIAL_CAVE, x, y, z, region.id(), biome.getIdAsString()));
                            added++;
                        }
                    }
                }
            }
        }
        return added;
    }
}
