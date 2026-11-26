package com.farsight.registry;

import com.farsight.FarSightMod;
import com.farsight.entity.AttackHeliEntity;
import com.farsight.entity.FarSightMerchantEntity;
import com.farsight.entity.ScoutPlaneEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<ScoutPlaneEntity> SCOUT_PLANE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(FarSightMod.MOD_ID, "scout_plane"),
            EntityType.Builder.create(ScoutPlaneEntity::new, SpawnGroup.MISC)
                    .dimensions(2.2f, 1.4f)
                    .maxTrackingRange(10)
                    .build()
    );

    public static final EntityType<AttackHeliEntity> ATTACK_HELI = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(FarSightMod.MOD_ID, "attack_heli"),
            EntityType.Builder.create(AttackHeliEntity::new, SpawnGroup.MISC)
                    .dimensions(2.4f, 1.8f)
                    .maxTrackingRange(10)
                    .build()
    );

    public static final EntityType<FarSightMerchantEntity> FARSIGHT_MERCHANT = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(FarSightMod.MOD_ID, "farsight_merchant"),
            EntityType.Builder.create(FarSightMerchantEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6f, 1.95f)
                    .maxTrackingRange(10)
                    .build()
    );

    public static void init() {
        FabricDefaultAttributeRegistry.register(FARSIGHT_MERCHANT, WanderingTraderEntity.createMobAttributes());
    }
}
