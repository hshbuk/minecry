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

    public static final Item TACTICAL_MACHETE = Registry.register(
            Registries.ITEM,
            Identifier.of(FarSightMod.MOD_ID, "tactical_machete"),
            new com.farsight.item.TacticalMacheteItem(new Item.Settings().maxCount(1).maxDamage(1200))
    );

    public static final Item HUNTER_BOW = Registry.register(
            Registries.ITEM,
            Identifier.of(FarSightMod.MOD_ID, "hunter_bow"),
            new com.farsight.item.HunterBowItem(new Item.Settings().maxCount(1).maxDamage(650))
    );

    public static final Item FLARE_LAUNCHER = Registry.register(
            Registries.ITEM,
            Identifier.of(FarSightMod.MOD_ID, "flare_launcher"),
            new com.farsight.item.FlareLauncherItem(new Item.Settings().maxCount(1).maxDamage(180))
    );

    public static final Item HORSE_WHISTLE = Registry.register(
            Registries.ITEM,
            Identifier.of(FarSightMod.MOD_ID, "horse_whistle"),
            new com.farsight.item.HorseWhistleItem(new Item.Settings().maxCount(1))
    );

    public static final Item SCOUT_PLANE_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of(FarSightMod.MOD_ID, "scout_plane_item"),
            new com.farsight.item.VehicleSpawnItem(new Item.Settings().maxCount(1), () -> ModEntities.SCOUT_PLANE)
    );

    public static final Item ATTACK_HELI_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of(FarSightMod.MOD_ID, "attack_heli_item"),
            new com.farsight.item.VehicleSpawnItem(new Item.Settings().maxCount(1), () -> ModEntities.ATTACK_HELI)
    );

    public static final ItemGroup FARSIGHT_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of(FarSightMod.MOD_ID, "farsight_group"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModBlocks.TOWER_TERMINAL))
                    .displayName(Text.translatable("itemGroup.farsight"))
                    .entries((context, entries) -> {
                        entries.add(SCOUT_BINOCULARS);
                        entries.add(HORSE_WHISTLE);
                        entries.add(SCOUT_PLANE_ITEM);
                        entries.add(ATTACK_HELI_ITEM);
                        entries.add(TACTICAL_MACHETE);
                        entries.add(HUNTER_BOW);
                        entries.add(FLARE_LAUNCHER);
                        entries.add(ModBlocks.TOWER_TERMINAL);
                        entries.add(ModBlocks.SIGNAL_RELAY);
                        entries.add(ModBlocks.TOWER_BARRIER);
                        entries.add(ModBlocks.CHECKPOINT_FLAG);
                    })
                    .build()
    );

    public static void init() {
        // Static initialization triggers registration
    }
}
