package com.farsight.world;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;

public class TierCalculator {

    public static int getTierForCoords(int x, int z) {
        double dist = Math.hypot(x, z);
        if (dist < 700) return 1;
        if (dist < 1400) return 2;
        if (dist < 2200) return 3;
        if (dist < 3200) return 4;
        return 5;
    }

    public static int getTierForPos(BlockPos pos) {
        return getTierForCoords(pos.getX(), pos.getZ());
    }

    public static String getTierStars(int tier) {
        return "★".repeat(Math.max(1, Math.min(5, tier)));
    }

    public static String getTierLabel(int tier) {
        return switch (tier) {
            case 1 -> "Tier I (Recruit)";
            case 2 -> "Tier II (Veteran)";
            case 3 -> "Tier III (Hardened)";
            case 4 -> "Tier IV (Elite)";
            default -> "Tier V (Apex)";
        };
    }

    public static void scaleHostileMob(HostileEntity mob, int tier) {
        if (tier <= 1) return;
        if (mob.getCommandTags().contains("farsight_scaled")) return;
        mob.addCommandTag("farsight_scaled");

        double hpMult = 1.0 + (tier - 1) * 0.25;
        var maxHpAttr = mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHpAttr != null) {
            maxHpAttr.setBaseValue(maxHpAttr.getBaseValue() * hpMult);
            mob.setHealth((float) maxHpAttr.getValue());
        }

        double dmgMult = 1.0 + (tier - 1) * 0.18;
        var dmgAttr = mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (dmgAttr != null) {
            dmgAttr.setBaseValue(dmgAttr.getBaseValue() * dmgMult);
        }
    }
}
