package com.farsight.world;

import com.farsight.FarSightMod;
import com.mojang.serialization.MapCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class TowerStructure extends Structure {
    public static final MapCodec<TowerStructure> CODEC = createCodec(TowerStructure::new);

    public TowerStructure(Config config) {
        super(config);
    }

    @Override
    protected Optional<StructurePosition> getStructurePosition(Context context) {
        int x = context.chunkPos().getCenterX();
        int z = context.chunkPos().getCenterZ();
        int y = context.chunkGenerator().getHeightInGround(
                x, z, Heightmap.Type.WORLD_SURFACE_WG, context.world(), context.noiseConfig()
        );

        // Avoid underwater / ocean placements
        if (y < context.chunkGenerator().getSeaLevel() + 1) {
            return Optional.empty();
        }

        BlockPos origin = new BlockPos(x, y, z);
        RegistryEntry<Biome> biome = context.biomeSource().getBiome(
                x >> 2, y >> 2, z >> 2, context.noiseConfig().getMultiNoiseSampler()
        );
        TowerTheme theme = TowerTheme.fromBiome(biome);

        return Optional.of(new StructurePosition(origin, collector -> {
            StructurePiece piece = new TowerPiece(theme, origin);
            collector.addPiece(piece);
        }));
    }

    @Override
    public StructureType<?> getType() {
        return FarSightMod.TOWER_STRUCTURE_TYPE;
    }
}
