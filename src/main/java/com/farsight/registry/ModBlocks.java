package com.farsight.registry;

import com.farsight.FarSightMod;
import com.farsight.block.SignalRelayBlock;
import com.farsight.block.TowerBarrierBlock;
import com.farsight.block.TowerTerminalBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block TOWER_TERMINAL = register("tower_terminal",
            new TowerTerminalBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.IRON_GRAY)
                    .strength(4.0f, 1200.0f)
                    .sounds(BlockSoundGroup.METAL)
                    .luminance(state -> state.get(TowerTerminalBlock.ACTIVATED) ? 14 : 6)
                    .requiresTool()));

    public static final Block SIGNAL_RELAY = register("signal_relay",
            new SignalRelayBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.ORANGE)
                    .strength(2.5f, 6.0f)
                    .sounds(BlockSoundGroup.COPPER)
                    .luminance(state -> state.get(SignalRelayBlock.TUNED) ? 12 : 4)));

    public static final Block TOWER_BARRIER = register("tower_barrier",
            new TowerBarrierBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.RED)
                    .strength(-1.0f, 3600000.0f)
                    .sounds(BlockSoundGroup.GLASS)
                    .nonOpaque()
                    .luminance(state -> state.get(TowerBarrierBlock.OPEN) ? 2 : 10)));

    public static final Block CHECKPOINT_FLAG = register("checkpoint_flag",
            new com.farsight.block.CheckpointFlagBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.BRIGHT_RED)
                    .strength(2.0f, 6.0f)
                    .sounds(BlockSoundGroup.METAL)
                    .luminance(state -> state.get(com.farsight.block.CheckpointFlagBlock.CLAIMED) ? 12 : 6)));

    private static Block register(String name, Block block) {
        Identifier id = Identifier.of(FarSightMod.MOD_ID, name);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void init() {
        // Static initialization triggers registration
    }
}
