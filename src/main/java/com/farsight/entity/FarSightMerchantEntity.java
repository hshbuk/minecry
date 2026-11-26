package com.farsight.entity;

import com.farsight.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.world.World;

public class FarSightMerchantEntity extends WanderingTraderEntity {
    public FarSightMerchantEntity(EntityType<? extends WanderingTraderEntity> entityType, World world) {
        super(entityType, world);
        setCustomName(Text.literal("FarSight Caravan Merchant").formatted(Formatting.GOLD));
        setCustomNameVisible(true);
        setDespawnDelay(48000);
    }

    @Override
    protected void fillRecipes() {
        TradeOfferList offers = getOffers();
        offers.clear();

        offers.add(new TradeOffer(new TradedItem(Items.EMERALD, 2), new ItemStack(Items.ARROW, 16), 12, 2, 0.05f));
        offers.add(new TradeOffer(new TradedItem(Items.EMERALD, 4), new ItemStack(Items.GOLDEN_APPLE, 2), 8, 4, 0.05f));
        offers.add(new TradeOffer(new TradedItem(Items.EMERALD, 6), new ItemStack(ModItems.SCOUT_BINOCULARS, 1), 4, 5, 0.05f));
        offers.add(new TradeOffer(new TradedItem(Items.EMERALD, 8), new ItemStack(ModItems.HORSE_WHISTLE, 1), 4, 6, 0.05f));
        offers.add(new TradeOffer(new TradedItem(Items.EMERALD, 14), new ItemStack(ModItems.TACTICAL_MACHETE, 1), 3, 10, 0.05f));
        offers.add(new TradeOffer(new TradedItem(Items.EMERALD, 16), new ItemStack(ModItems.HUNTER_BOW, 1), 3, 10, 0.05f));
        offers.add(new TradeOffer(new TradedItem(Items.EMERALD, 20), new ItemStack(ModItems.FLARE_LAUNCHER, 1), 3, 12, 0.05f));
        offers.add(new TradeOffer(new TradedItem(Items.EMERALD, 32), new ItemStack(ModItems.SCOUT_PLANE_ITEM, 1), 2, 20, 0.05f));
        offers.add(new TradeOffer(new TradedItem(Items.EMERALD, 48), new ItemStack(ModItems.ATTACK_HELI_ITEM, 1), 2, 25, 0.05f));
    }
}
