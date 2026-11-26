package com.farsight.registry;

import com.farsight.FarSightMod;
import com.farsight.item.ScoutBinocularsItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item SCOUT_BINOCULARS = Registry.register(
            Registries.ITEM,
            Identifier.of(FarSightMod.MOD_ID, "scout_binoculars"),
            new ScoutBinocularsItem(new Item.Settings().maxCount(1))
    );

    public static final ItemGroup FARSIGHT_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of(FarSightMod.MOD_ID, "farsight_group"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModBlocks.TOWER_TERMINAL))
                    .displayName(Text.translatable("itemGroup.farsight"))
                    .entries((context, entries) -> {
                        entries.add(SCOUT_BINOCULARS);
                        entries.add(ModBlocks.TOWER_TERMINAL);
                        entries.add(ModBlocks.SIGNAL_RELAY);
                        entries.add(ModBlocks.TOWER_BARRIER);
                    })
                    .build()
    );

    public static void init() {
        // Static initialization triggers registration
    }
}
