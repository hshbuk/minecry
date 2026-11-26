package com.farsight.item;

import com.farsight.data.PlayerProgressState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class HorseWhistleItem extends Item {
    public HorseWhistleItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) {
            return TypedActionResult.success(stack);
        }

        if (world instanceof ServerWorld sw && user instanceof ServerPlayerEntity player) {
            player.getItemCooldownManager().set(this, 40);
            String tag = "farsight_horse_" + player.getUuidAsString();

            // Check if player's bonded Scout Horse already exists nearby
            List<HorseEntity> existing = sw.getEntitiesByClass(HorseEntity.class,
                    player.getBoundingBox().expand(120),
                    h -> h.isAlive() && h.getCommandTags().contains(tag));

            double spawnX = player.getX() + 1.5;
            double spawnY = player.getY();
            double spawnZ = player.getZ() + 1.5;

            if (!existing.isEmpty()) {
                HorseEntity horse = existing.get(0);
                horse.teleport(sw, spawnX, spawnY, spawnZ, java.util.Set.of(), player.getYaw(), 0);
                sw.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_HORSE_AMBIENT, SoundCategory.PLAYERS, 1.0f, 1.1f);
                sw.spawnParticles(ParticleTypes.CLOUD, spawnX, spawnY + 0.5, spawnZ, 12, 0.4, 0.3, 0.4, 0.02);
                player.sendMessage(Text.translatable("message.farsight.horse.recalled").formatted(Formatting.AQUA), true);
            } else {
                HorseEntity horse = EntityType.HORSE.create(sw);
                if (horse != null) {
                    horse.refreshPositionAndAngles(spawnX, spawnY, spawnZ, player.getYaw(), 0);
                    horse.setTame(true);
                    horse.setOwnerUuid(player.getUuid());
                    horse.addCommandTag(tag);
                    horse.setCustomName(Text.literal(player.getName().getString() + "'s Scout Horse").formatted(Formatting.GOLD));
                    horse.saddle(new ItemStack(Items.SADDLE), null);

                    int lvl = PlayerProgressState.get(sw).getOrCreate(player.getUuid()).level;
                    horse.equipBodyArmor(new ItemStack(lvl >= 3 ? Items.DIAMOND_HORSE_ARMOR : Items.IRON_HORSE_ARMOR));

                    var speed = horse.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                    if (speed != null) speed.setBaseValue(0.34);
                    var jump = horse.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH);
                    if (jump != null) jump.setBaseValue(0.95);

                    sw.spawnEntity(horse);
                    sw.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_HORSE_ARMOR, SoundCategory.PLAYERS, 1.0f, 1.1f);
                    sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER, spawnX, spawnY + 1.0, spawnZ, 15, 0.5, 0.5, 0.5, 0.05);
                    player.sendMessage(Text.translatable("message.farsight.horse.summoned").formatted(Formatting.GREEN), true);
                }
            }
        }
        return TypedActionResult.success(stack);
    }
}
