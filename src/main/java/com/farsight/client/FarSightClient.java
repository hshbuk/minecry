package com.farsight.client;

import com.farsight.block.TowerBarrierBlock;
import com.farsight.client.hud.CompassHudRenderer;
import com.farsight.client.render.WorldMarkerRenderer;
import com.farsight.client.screen.TowerPuzzleScreen;
import com.farsight.client.screen.WorldMapScreen;
import com.farsight.network.ModNetworking;
import com.farsight.registry.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class FarSightClient implements ClientModInitializer {
    private static KeyBinding openMapKey;
    private static KeyBinding openUpgradesKey;

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TOWER_BARRIER, RenderLayer.getTranslucent());

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                com.farsight.registry.ModEntities.SCOUT_PLANE,
                ctx -> new com.farsight.client.render.VehicleEntityRenderer<>(ctx, false)
        );
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                com.farsight.registry.ModEntities.ATTACK_HELI,
                ctx -> new com.farsight.client.render.VehicleEntityRenderer<>(ctx, true)
        );
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                com.farsight.registry.ModEntities.FARSIGHT_MERCHANT,
                net.minecraft.client.render.entity.WanderingTraderEntityRenderer::new
        );

        openMapKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.farsight.world_map",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.farsight"
        ));
        openUpgradesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.farsight.upgrades",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                "category.farsight"
        ));

        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.SyncDiscoveryPayload.ID, (payload, context) -> {
            context.client().execute(() -> ClientDiscoveryCache.update(payload.regions(), payload.pois(), payload.roads()));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.OpenPuzzlePayload.ID, (payload, context) -> {
            context.client().execute(() -> context.client().setScreen(
                    new TowerPuzzleScreen(payload.pos(), payload.biomeTheme(), payload.regionName(), payload.seed())
            ));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.RegionUnlockedPayload.ID, (payload, context) -> {
            context.client().execute(() -> ClientDiscoveryCache.triggerBanner(payload.regionName(), payload.poisRevealed()));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.CutsceneHudPayload.ID, (payload, context) -> {
            context.client().execute(() -> ClientDiscoveryCache.setCutscene(payload.active(), payload.title(), payload.subtitle()));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.SyncProgressPayload.ID, (payload, context) -> {
            context.client().execute(() -> ClientDiscoveryCache.setProgress(payload.progress()));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientDiscoveryCache.tick();
            if (client.player != null && client.player.getVehicle() != null &&
                    (client.player.getVehicle() instanceof com.farsight.entity.ScoutPlaneEntity ||
                            client.player.getVehicle() instanceof com.farsight.entity.AttackHeliEntity)) {
                float fwd = (client.options.forwardKey.isPressed() ? 1.0f : 0.0f) - (client.options.backKey.isPressed() ? 1.0f : 0.0f);
                float str = (client.options.rightKey.isPressed() ? 1.0f : 0.0f) - (client.options.leftKey.isPressed() ? 1.0f : 0.0f);
                boolean up = client.options.jumpKey.isPressed();
                boolean down = client.options.sprintKey.isPressed();
                ClientPlayNetworking.send(new ModNetworking.VehicleInputPayload(fwd, str, up, down));
            }
            if (ClientDiscoveryCache.isCutsceneActive() && client.options.jumpKey.wasPressed()) {
                ClientPlayNetworking.send(new ModNetworking.SkipCutscenePayload());
            }
            while (openMapKey.wasPressed()) {
                if (client.currentScreen == null && !ClientDiscoveryCache.isCutsceneActive()) {
                    client.setScreen(new WorldMapScreen());
                }
            }
            while (openUpgradesKey.wasPressed()) {
                if (client.currentScreen == null && !ClientDiscoveryCache.isCutsceneActive()) {
                    client.setScreen(new com.farsight.client.screen.UpgradesScreen());
                }
            }
        });

        HudRenderCallback.EVENT.register(CompassHudRenderer::render);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(WorldMarkerRenderer::render);
    }
}
