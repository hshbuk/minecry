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

public class ScoutPlaneEntity extends Entity {
    public boolean inputUp = false;
    public boolean inputDown = false;
    public float inputForward = 0.0f;

    public ScoutPlaneEntity(EntityType<?> type, World world) {
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
                setPitch(player.getPitch() * 0.5f);
                player.fallDistance = 0.0f;

                Vec3d vel = getVelocity();
                double speed = vel.horizontalLength();

                if (inputForward > 0.01f) {
                    Vec3d look = Vec3d.fromPolar(0, getYaw()).normalize();
                    double targetSpeed = Math.min(1.15, speed + 0.06);
                    double vy = vel.y;
                    if (inputUp) vy = Math.min(0.45, vy + 0.06);
                    else if (inputDown) vy = Math.max(-0.45, vy - 0.06);
                    else vy = Math.max(-0.08, -player.getPitch() * 0.01);

                    setVelocity(look.x * targetSpeed, vy, look.z * targetSpeed);
                } else {
                    double vy = inputUp ? 0.25 : (inputDown ? -0.3 : Math.max(-0.22, vel.y - 0.03));
                    setVelocity(vel.x * 0.92, vy, vel.z * 0.92);
                }
            } else {
                Vec3d vel = getVelocity();
                setVelocity(vel.x * 0.85, Math.max(-0.4, vel.y - 0.04), vel.z * 0.85);
            }

            move(MovementType.SELF, getVelocity());
            velocityDirty = true;
        } else if (pilot != null && getVelocity().horizontalLengthSquared() > 0.05) {
            getWorld().addParticle(ParticleTypes.CLOUD, getX(), getY() + 0.4, getZ(), 0, 0, 0);
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
            dropStack(new ItemStack(ModItems.SCOUT_PLANE_ITEM));
            discard();
            return true;
        }
        return false;
    }
}
