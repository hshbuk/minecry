package com.farsight.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;

public class HunterBowItem extends BowItem {
    public HunterBowItem(Settings settings) {
        super(settings);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        ItemStack ammo = player.getProjectileType(stack);
        boolean creativeOrInfinite = player.getAbilities().creativeMode;
        if (ammo.isEmpty() && !creativeOrInfinite) return;
        if (ammo.isEmpty()) ammo = new ItemStack(Items.ARROW);

        int usedTicks = this.getMaxUseTime(stack, user) - remainingUseTicks;
        // Charges ~35% faster than vanilla bow
        float pull = getPullProgress((int) (usedTicks * 1.35f));
        if (pull < 0.1f) return;

        if (!world.isClient) {
            ArrowItem arrowItem = (ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
            PersistentProjectileEntity arrow = arrowItem.createArrow(world, ammo, player, stack);
            arrow.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, pull * 3.8f, 0.4f);
            arrow.setDamage(arrow.getDamage() * 1.4);
            if (pull == 1.0f) arrow.setCritical(true);
            world.spawnEntity(arrow);

            stack.damage(1, player, LivingEntity.getSlotForHand(user.getActiveHand()));
            if (!creativeOrInfinite) {
                ammo.decrement(1);
            }
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.25f);
        player.incrementStat(Stats.USED.getOrCreateStat(this));
    }
}
