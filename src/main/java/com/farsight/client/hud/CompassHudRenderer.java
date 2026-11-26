package com.farsight.client.hud;

import com.farsight.client.ClientDiscoveryCache;
import com.farsight.data.PoiData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class CompassHudRenderer {
    private static final String[] CARDINALS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.options.hudHidden || client.currentScreen != null) return;

        TextRenderer tr = client.textRenderer;
        int screenW = client.getWindow().getScaledWidth();
        int centerX = screenW / 2;
        int barW = 240;
        int barH = 18;
        int barLeft = centerX - barW / 2;
        int barTop = 8;

        // Sleek Far Cry Compass Background
        context.fill(barLeft, barTop, barLeft + barW, barTop + barH, 0x880B1016);
        context.drawBorder(barLeft, barTop, barW, barH, 0x6600E5FF);

        // Player yaw in degrees: 0 = South, 90 = West, 180 = North, 270 = East
        float playerYaw = MathHelper.wrapDegrees(player.getYaw());

        context.enableScissor(barLeft + 2, barTop, barLeft + barW - 2, barTop + barH);

        // 1. Cardinal Directions
        for (int i = 0; i < 8; i++) {
            float targetYaw = MathHelper.wrapDegrees(i * 45.0f);
            float diff = MathHelper.wrapDegrees(targetYaw - playerYaw);
            if (Math.abs(diff) <= 85.0f) {
                int x = (int) (centerX + (diff / 85.0f) * (barW / 2.0f));
                String label = CARDINALS[i];
                int color = label.equals("N") ? 0xFFFF5252 : 0xFFE0E0E0;
                context.drawCenteredTextWithShadow(tr, label, x, barTop + 5, color);
            }
        }

        // 2. Discovered POIs & Active Waypoint on Compass
        PoiData activeWp = ClientDiscoveryCache.getActiveWaypoint();
        for (PoiData poi : ClientDiscoveryCache.getPois()) {
            double dx = poi.x() + 0.5 - player.getX();
            double dz = poi.z() + 0.5 - player.getZ();
            double dist = Math.hypot(dx, dz);
            boolean isWp = activeWp != null && activeWp.id().equals(poi.id());
            if (!isWp && dist > 800.0) continue;

            // Convert (dx, dz) to Minecraft yaw (-atan2(dx, dz))
            float poiYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float diff = MathHelper.wrapDegrees(poiYaw - playerYaw);
            if (Math.abs(diff) <= 85.0f) {
                int x = (int) (centerX + (diff / 85.0f) * (barW / 2.0f));
                int color = poi.type().getColor();
                if (isWp) {
                    context.fill(x - 5, barTop + 2, x + 5, barTop + 12, 0xFFFFFFFF);
                }
                context.fill(x - 4, barTop + 3, x + 4, barTop + 11, color);
                context.drawCenteredTextWithShadow(tr, poi.type().getSymbol(), x, barTop + 3, 0xFF000000);
            }
        }

        context.disableScissor();

        // Center notch
        context.fill(centerX - 1, barTop - 2, centerX + 1, barTop + 2, 0xFF00E5FF);

        // Active Waypoint distance readout under compass
        if (activeWp != null) {
            int dist = (int) Math.hypot(activeWp.x() - player.getX(), activeWp.z() - player.getZ());
            context.drawCenteredTextWithShadow(tr,
                    Text.literal("[" + activeWp.name() + " - " + dist + "m]"),
                    centerX, barTop + barH + 3, activeWp.type().getColor());
        }

        // 3. Dramatic "REGION UNLOCKED" Banner
        if (ClientDiscoveryCache.hasBanner()) {
            int ticks = ClientDiscoveryCache.getBannerTicks();
            int alpha = ticks < 20 ? (int) ((ticks / 20.0f) * 255) : 255;
            int y = 48;
            int w = 260;
            int bx = centerX - w / 2;

            context.fill(bx, y, bx + w, y + 38, (Math.min(alpha, 200) << 24) | 0x0D151E);
            context.drawBorder(bx, y, w, 38, (alpha << 24) | 0x00E5FF);
            context.drawCenteredTextWithShadow(tr,
                    Text.translatable("hud.farsight.region_unlocked", ClientDiscoveryCache.getBannerRegionName()),
                    centerX, y + 7, (alpha << 24) | 0x00E5FF);
            context.drawCenteredTextWithShadow(tr,
                    Text.translatable("hud.farsight.pois_revealed", ClientDiscoveryCache.getBannerPoisCount()),
                    centerX, y + 21, (alpha << 24) | 0x34C759);
        }
    }
}
