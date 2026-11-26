package com.farsight.entity;

import com.farsight.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AttackHeliEntity extends Entity {
    public boolean inputUp = false;
    public boolean inputDown = false;
    public float inputForward = 0.0f;
    public float inputStrafe = 0.0f;

    public AttackHeliEntity(EntityType<?> type, World world) {
        super(type, world);
        this.intersectionChecked = true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {}

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {}

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {}

    @Override
    public boolean canHit() {
        return !isRemoved();
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!getWorld().isClient) {
            return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
        }
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity first = getFirstPassenger();
        return first instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity pilot = getControllingPassenger();

        if (!getWorld().isClient) {
            if (pilot instanceof PlayerEntity player) {
                setYaw(player.getYaw());
                player.fallDistance = 0.0f;

                Vec3d forward = Vec3d.fromPolar(0, getYaw()).normalize();
                Vec3d right = Vec3d.fromPolar(0, getYaw() + 90).normalize();

                double vx = (forward.x * inputForward - right.x * inputStrafe) * 0.72;
                double vz = (forward.z * inputForward - right.z * inputStrafe) * 0.72;

                double vy = 0.0;
                if (inputUp) vy = 0.42;
                else if (inputDown) vy = -0.38;

                Vec3d prev = getVelocity();
                setVelocity(prev.x * 0.65 + vx * 0.35, vy, prev.z * 0.65 + vz * 0.35);
            } else {
                Vec3d vel = getVelocity();
                setVelocity(vel.x * 0.8, Math.max(-0.25, vel.y - 0.03), vel.z * 0.8);
            }

            move(MovementType.SELF, getVelocity());
            velocityDirty = true;
        } else if (pilot != null) {
            getWorld().addParticle(ParticleTypes.SMOKE, getX(), getY() + 2.3, getZ(), 0, 0.02, 0);
        }
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (isRemoved() || getWorld().isClient) return true;
        if (source.getAttacker() instanceof PlayerEntity p && p.isSneaking()) {
            dropStack(new ItemStack(ModItems.ATTACK_HELI_ITEM));
            discard();
            return true;
        }
        return false;
    }
}
