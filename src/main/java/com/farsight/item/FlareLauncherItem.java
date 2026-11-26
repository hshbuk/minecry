package com.farsight.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FlareLauncherItem extends Item {
    public FlareLauncherItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            user.getItemCooldownManager().set(this, 35);
            Vec3d look = user.getRotationVec(1.0f);
            SmallFireballEntity flare = new SmallFireballEntity(world, user, look.multiply(1.4));
            flare.setPosition(user.getX() + look.x * 1.2, user.getEyeY() - 0.1 + look.y * 1.2, user.getZ() + look.z * 1.2);
            world.spawnEntity(flare);
            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.3f);
            stack.damage(1, user, PlayerEntity.getSlotForHand(hand));
        }
        return TypedActionResult.success(stack, world.isClient);
    }
}
