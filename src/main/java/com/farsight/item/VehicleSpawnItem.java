package com.farsight.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class VehicleSpawnItem extends Item {
    private final Supplier<EntityType<?>> entityTypeSupplier;

    public VehicleSpawnItem(Settings settings, Supplier<EntityType<?>> entityTypeSupplier) {
        super(settings);
        this.entityTypeSupplier = entityTypeSupplier;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && world instanceof ServerWorld sw) {
            Vec3d look = Vec3d.fromPolar(0, user.getYaw()).normalize();
            Entity vehicle = entityTypeSupplier.get().create(sw);
            if (vehicle != null) {
                vehicle.refreshPositionAndAngles(
                        user.getX() + look.x * 3.5,
                        user.getY() + 0.5,
                        user.getZ() + look.z * 3.5,
                        user.getYaw(),
                        0
                );
                sw.spawnEntity(vehicle);
                sw.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 0.8f, 1.2f);
                if (!user.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
            }
        }
        return TypedActionResult.success(stack, world.isClient);
    }
}
