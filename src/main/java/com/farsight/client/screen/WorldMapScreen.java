package com.farsight.client.screen;

import com.farsight.client.ClientDiscoveryCache;
import com.farsight.data.PoiData;
import com.farsight.data.RegionData;
import net.minecraft.block.MapColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Interactive Far Cry-style World Map with Fog of War, unlocked region borders,
 * live terrain coloring, POI tooltips, and click-to-set Waypoint tracking.
 */
public class WorldMapScreen extends Screen {
    private double camX;
    private double camZ;
    private double blocksPerPixel = 4.0;

    private int mapLeft, mapTop, mapWidth, mapHeight;
    private boolean dragging = false;
    private double lastMouseX, lastMouseY;

    public WorldMapScreen() {
        super(Text.translatable("screen.farsight.map.title"));
    }

    @Override
    protected void init() {
        super.init();
        if (client != null && client.player != null) {
            camX = client.player.getX();
            camZ = client.player.getZ();
        }
        mapLeft = 24;
        mapTop = 36;
        mapWidth = width - 48;
        mapHeight = height - 64;

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.farsight.map.center"), btn -> {
            if (client != null && client.player != null) {
                camX = client.player.getX();
                camZ = client.player.getZ();
            }
        }).dimensions(mapLeft, height - 24, 110, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.farsight.map.clear_wp"), btn -> {
            ClientDiscoveryCache.setActiveWaypoint(null);
        }).dimensions(mapLeft + 116, height - 24, 120, 18).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        // Header bar
        context.drawTextWithShadow(textRenderer,
                Text.translatable("screen.farsight.map.header",
                        ClientDiscoveryCache.getRegions().size(),
                        ClientDiscoveryCache.getPois().size()),
                mapLeft, 14, 0xFF00E5FF);

        // Map frame
        context.fill(mapLeft - 2, mapTop - 2, mapLeft + mapWidth + 2, mapTop + mapHeight + 2, 0xFF00E5FF);
        context.fill(mapLeft, mapTop, mapLeft + mapWidth, mapTop + mapHeight, 0xFF0B1016);

        context.enableScissor(mapLeft, mapTop, mapLeft + mapWidth, mapTop + mapHeight);

        // 1. Render Terrain + Fog of War in 6x6 pixel tiles for crisp smooth performance
        int tile = 6;
        World world = client != null ? client.world : null;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int sx = 0; sx < mapWidth; sx += tile) {
            for (int sy = 0; sy < mapHeight; sy += tile) {
                double wx = screenToWorldX(mapLeft + sx + tile * 0.5);
                double wz = screenToWorldZ(mapTop + sy + tile * 0.5);

                boolean discovered = ClientDiscoveryCache.isDiscovered(wx, wz);
                int color;
                if (!discovered) {
                    // Stylized Fog of War with subtle tactical grid
                    boolean grid = ((int) Math.floor(wx / 64.0) + (int) Math.floor(wz / 64.0)) % 2 == 0;
                    color = grid ? 0xFF0D131A : 0xFF101820;
                } else {
                    color = sampleWorldColor(world, (int) wx, (int) wz, mutable);
                }
                context.fill(mapLeft + sx, mapTop + sy,
                        Math.min(mapLeft + sx + tile, mapLeft + mapWidth),
                        Math.min(mapTop + sy + tile, mapTop + mapHeight),
                        color);
            }
        }

        // 2. Draw Region Borders (glowing cyan rings)
        for (RegionData region : ClientDiscoveryCache.getRegions()) {
            drawRegionCircle(context, region.centerX(), region.centerZ(), region.radius(), 0xAA00E5FF);
        }

        // 3. Draw Discovered POIs
        PoiData hoveredPoi = null;
        PoiData activeWp = ClientDiscoveryCache.getActiveWaypoint();

        for (PoiData poi : ClientDiscoveryCache.getPois()) {
            int px = worldToScreenX(poi.x());
            int py = worldToScreenZ(poi.z());
            if (px < mapLeft - 12 || px > mapLeft + mapWidth + 12 || py < mapTop - 12 || py > mapTop + mapHeight + 12) {
                continue;
            }

            boolean isWp = activeWp != null && activeWp.id().equals(poi.id());
            int size = isWp ? 7 : 5;
            int bgColor = poi.type().getColor();

            if (isWp) {
                context.fill(px - size - 2, py - size - 2, px + size + 2, py + size + 2, 0xFFFFFFFF);
            }
            context.fill(px - size, py - size, px + size, py + size, 0xFF000000);
            context.fill(px - size + 1, py - size + 1, px + size - 1, py + size - 1, bgColor);
            context.drawCenteredTextWithShadow(textRenderer, poi.type().getSymbol(), px, py - 4, 0xFFFFFFFF);

            if (Math.abs(mouseX - px) <= 8 && Math.abs(mouseY - py) <= 8) {
                hoveredPoi = poi;
            }
        }

        // 4. Draw Player Arrow
        if (client != null && client.player != null) {
            int plx = worldToScreenX(client.player.getX());
            int ply = worldToScreenZ(client.player.getZ());
            context.fill(plx - 4, ply - 4, plx + 4, ply + 4, 0xFFFFFFFF);
            context.fill(plx - 2, ply - 2, plx + 2, ply + 2, 0xFF34C759);
        }

        context.disableScissor();

        // 5. Tooltip for hovered POI
        if (hoveredPoi != null) {
            final PoiData tipPoi = hoveredPoi;
            List<Text> tooltip = new ArrayList<>();
            tooltip.add(Text.literal(tipPoi.name()).styled(s -> s.withColor(tipPoi.type().getColor()).withBold(true)));
            tooltip.add(Text.literal(tipPoi.type().getDisplayName()).styled(s -> s.withColor(0xFFB0BEC5)));
            tooltip.add(Text.literal("XYZ: " + tipPoi.x() + ", " + tipPoi.y() + ", " + tipPoi.z()));
            if (client != null && client.player != null) {
                int dist = (int) Math.hypot(tipPoi.x() - client.player.getX(), tipPoi.z() - client.player.getZ());
                tooltip.add(Text.literal("Distance: " + dist + "m"));
            }
            tooltip.add(Text.translatable("screen.farsight.map.click_wp").styled(s -> s.withColor(0xFF00E5FF)));
            context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
        }

        // Footer hint
        context.drawTextWithShadow(textRenderer,
                Text.translatable("screen.farsight.map.controls"),
                mapLeft + 244, height - 19, 0xFF90A4AE);

        super.render(context, mouseX, mouseY, delta);
    }

    private int sampleWorldColor(World world, int wx, int wz, BlockPos.Mutable mutable) {
        if (world != null && world.isChunkLoaded(wx >> 4, wz >> 4)) {
            int topY = world.getTopY(Heightmap.Type.WORLD_SURFACE, wx, wz) - 1;
            mutable.set(wx, Math.max(world.getBottomY(), topY), wz);
            MapColor mapColor = world.getBlockState(mutable).getMapColor(world, mutable);
            if (mapColor != null && mapColor.color != 0) {
                return 0xFF000000 | mapColor.color;
            }
        }
        // Unloaded chunk inside an unlocked region: stylized topographic tint
        return 0xFF1C2D27;
    }

    private void drawRegionCircle(DrawContext context, int cx, int cz, int radius, int color) {
        int steps = 72;
        for (int i = 0; i < steps; i++) {
            double angle = (2.0 * Math.PI * i) / steps;
            double wx = cx + Math.cos(angle) * radius;
            double wz = cz + Math.sin(angle) * radius;
            int sx = worldToScreenX(wx);
            int sy = worldToScreenZ(wz);
            context.fill(sx - 1, sy - 1, sx + 2, sy + 2, color);
        }
    }

    private int worldToScreenX(double wx) {
        return (int) (mapLeft + mapWidth / 2.0 + (wx - camX) / blocksPerPixel);
    }

    private int worldToScreenZ(double wz) {
        return (int) (mapTop + mapHeight / 2.0 + (wz - camZ) / blocksPerPixel);
    }

    private double screenToWorldX(double sx) {
        return camX + (sx - (mapLeft + mapWidth / 2.0)) * blocksPerPixel;
    }

    private double screenToWorldZ(double sy) {
        return camZ + (sy - (mapTop + mapHeight / 2.0)) * blocksPerPixel;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && mouseX >= mapLeft && mouseX <= mapLeft + mapWidth && mouseY >= mapTop && mouseY <= mapTop + mapHeight) {
            // Check if player clicked a POI to toggle Waypoint
            for (PoiData poi : ClientDiscoveryCache.getPois()) {
                int px = worldToScreenX(poi.x());
                int py = worldToScreenZ(poi.z());
                if (Math.abs(mouseX - px) <= 8 && Math.abs(mouseY - py) <= 8) {
                    PoiData current = ClientDiscoveryCache.getActiveWaypoint();
                    if (current != null && current.id().equals(poi.id())) {
                        ClientDiscoveryCache.setActiveWaypoint(null);
                    } else {
                        ClientDiscoveryCache.setActiveWaypoint(poi);
                    }
                    if (client != null) {
                        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.2f));
                    }
                    return true;
                }
            }
            dragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            camX -= (mouseX - lastMouseX) * blocksPerPixel;
            camZ -= (mouseY - lastMouseY) * blocksPerPixel;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0) {
            blocksPerPixel = Math.max(1.0, blocksPerPixel * 0.8);
        } else if (verticalAmount < 0) {
            blocksPerPixel = Math.min(16.0, blocksPerPixel * 1.25);
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
