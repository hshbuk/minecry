package com.farsight.world;

import com.farsight.FarSightMod;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

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
}
