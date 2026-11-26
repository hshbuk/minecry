package com.farsight.registry;

import com.farsight.FarSightMod;
import com.farsight.block.entity.TowerTerminalBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<TowerTerminalBlockEntity> TOWER_TERMINAL_BE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(FarSightMod.MOD_ID, "tower_terminal"),
            BlockEntityType.Builder.create(TowerTerminalBlockEntity::new, ModBlocks.TOWER_TERMINAL).build()
    );

    public static void init() {
        // Static initialization triggers registration
    }
}
