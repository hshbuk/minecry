package com.farsight.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;

public class TacticalMacheteItem extends SwordItem {
    public TacticalMacheteItem(Settings settings) {
        super(ToolMaterials.DIAMOND, settings.attributeModifiers(
                SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 5, -2.0f)
        ));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.isAlive() || target.getHealth() <= 0) {
            attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 80, 1));
        }
        return super.postHit(stack, target, attacker);
    }
}
