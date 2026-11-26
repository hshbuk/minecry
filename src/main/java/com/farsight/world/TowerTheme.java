package com.farsight.world;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public enum TowerTheme {
    PLAINS("plains", "Frontier Highlands",
            Blocks.COBBLESTONE, Blocks.STONE_BRICKS, Blocks.DARK_OAK_LOG,
            Blocks.OAK_PLANKS, Blocks.OAK_FENCE, Blocks.LANTERN),
    DESERT("desert", "Sunscourge Dunes",
            Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.TERRACOTTA,
            Blocks.CUT_SANDSTONE, Blocks.ACACIA_FENCE, Blocks.LANTERN),
    SNOWY("snowy", "Frostpeak Reach",
            Blocks.DEEPSLATE_BRICKS, Blocks.POLISHED_DEEPSLATE, Blocks.SPRUCE_LOG,
            Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_FENCE, Blocks.SOUL_LANTERN),
    JUNGLE("jungle", "Emerald Canopy",
            Blocks.MOSSY_COBBLESTONE, Blocks.MOSSY_STONE_BRICKS, Blocks.JUNGLE_LOG,
            Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_FENCE, Blocks.LANTERN),
    BADLANDS("badlands", "Crimson Mesa",
            Blocks.RED_SANDSTONE, Blocks.BLACKSTONE, Blocks.ORANGE_TERRACOTTA,
            Blocks.CUT_RED_SANDSTONE, Blocks.ACACIA_FENCE, Blocks.LANTERN),
    SWAMP("swamp", "Mirewatch Fen",
            Blocks.MUD_BRICKS, Blocks.CUT_COPPER, Blocks.MANGROVE_LOG,
            Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_FENCE, Blocks.SOUL_LANTERN);

    public final String id;
    public final String defaultRegionName;
    public final Block primaryStone;
    public final Block accentStone;
    public final Block pillar;
    public final Block planks;
    public final Block fence;
    public final Block lantern;

    TowerTheme(String id, String defaultRegionName, Block primaryStone, Block accentStone,
               Block pillar, Block planks, Block fence, Block lantern) {
        this.id = id;
        this.defaultRegionName = defaultRegionName;
        this.primaryStone = primaryStone;
        this.accentStone = accentStone;
        this.pillar = pillar;
        this.planks = planks;
        this.fence = fence;
        this.lantern = lantern;
    }

    public static TowerTheme fromBiome(RegistryEntry<Biome> biome) {
        if (biome.matchesKey(BiomeKeys.DESERT) || biome.isIn(BiomeTags.IS_BEACH)) {
            return DESERT;
        }
        if (biome.isIn(BiomeTags.IS_BADLANDS) || biome.isIn(BiomeTags.IS_SAVANNA)) {
            return BADLANDS;
        }
        if (biome.isIn(BiomeTags.IS_JUNGLE)) {
            return JUNGLE;
        }
        if (biome.matchesKey(BiomeKeys.SWAMP) || biome.matchesKey(BiomeKeys.MANGROVE_SWAMP)) {
            return SWAMP;
        }
        if (biome.value().getTemperature() < 0.2f || biome.isIn(BiomeTags.IS_TAIGA)) {
            return SNOWY;
        }
        return PLAINS;
    }
}
