package com.farsight.item;

import com.farsight.data.DiscoveryState;
import com.farsight.data.PoiData;
import com.farsight.data.PoiType;
import com.farsight.network.ModNetworking;
import com.farsight.world.TowerLocator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ScoutBinocularsItem extends Item {
    public ScoutBinocularsItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) {
            return TypedActionResult.success(stack);
        }

        if (world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.getItemCooldownManager().set(this, 40);
            world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_SPYGLASS_USE, SoundCategory.PLAYERS, 1.0f, 1.1f);

            BlockPos nearestTower = TowerLocator.findNearestUnclaimedTower(serverWorld, user.getBlockPos(), 1000);
            if (nearestTower != null) {
                Vec3d toTower = Vec3d.ofCenter(nearestTower).subtract(user.getEyePos()).normalize();
                double dot = user.getRotationVec(1.0f).dotProduct(toTower);
                int dist = (int) Math.sqrt(user.getBlockPos().getSquaredDistance(nearestTower));
                DiscoveryState state = DiscoveryState.get(serverWorld);
                String id = "tower_" + nearestTower.getX() + "_" + nearestTower.getZ();

                if (dot > 0.7 || dist < 120) {
                    if (state.getPois().stream().noneMatch(p -> p.id().equals(id))) {
                        state.addPoi(new PoiData(
                                id,
                                "Pillager Signal Tower",
                                PoiType.TOWER_UNCLAIMED,
                                nearestTower.getX(),
                                nearestTower.getY(),
                                nearestTower.getZ(),
                                "unclaimed",
                                serverWorld.getBiome(nearestTower).getIdAsString()
                        ));
                        ModNetworking.syncAllPlayers(serverWorld);
                    }
                    serverPlayer.sendMessage(
                            Text.translatable("message.farsight.binoculars.spotted", dist, nearestTower.getX(), nearestTower.getZ())
                                    .formatted(Formatting.GOLD),
                            true
                    );
                } else {
                    serverPlayer.sendMessage(
                            Text.translatable("message.farsight.binoculars.ping", dist)
                                    .formatted(Formatting.YELLOW),
                            true
                    );
                }
            } else {
                serverPlayer.sendMessage(
                        Text.translatable("message.farsight.binoculars.none").formatted(Formatting.GRAY),
                        true
                );
            }
        }

        return TypedActionResult.success(stack);
    }
}
