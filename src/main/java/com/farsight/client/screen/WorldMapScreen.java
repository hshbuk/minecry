package com.farsight.client.screen;

import com.farsight.client.ClientDiscoveryCache;
import com.farsight.data.PoiData;
import com.farsight.data.PoiType;
import com.farsight.data.RegionData;
import net.minecraft.block.MapColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Crisp Far Cry-style World Map with topographic elevation shading, Fog of War,
 * pulsing Tower markers, a sidebar listing nearby Towers, and Waypoint tracking.
 */
public class WorldMapScreen extends Screen {
    private double camX;
    private double camZ;
    private double blocksPerPixel = 3.0;

    private int mapLeft, mapTop, mapWidth, mapHeight;
    private int sideLeft, sideWidth;
    private boolean dragging = false;
    private double lastMouseX, lastMouseY;

    private final List<SidebarEntry> sidebarEntries = new ArrayList<>();

    private record SidebarEntry(PoiData poi, int x, int y, int w, int h) {}

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
        sideWidth = 156;
        mapLeft = 16;
        mapTop = 32;
        mapWidth = Math.max(180, width - sideWidth - 38);
        mapHeight = height - 60;
        sideLeft = mapLeft + mapWidth + 10;

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.farsight.map.center"), btn -> {
            if (client != null && client.player != null) {
                camX = client.player.getX();
                camZ = client.player.getZ();
            }
        }).dimensions(mapLeft, height - 23, 105, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.farsight.map.clear_wp"), btn -> {
            ClientDiscoveryCache.setActiveWaypoint(null);
        }).dimensions(mapLeft + 111, height - 23, 115, 18).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Crisp dark tactical backdrop (avoids blurry world bleed behind UI text)
        context.fill(0, 0, width, height, 0xE6070B10);

        // Header bar
        context.drawTextWithShadow(textRenderer,
                Text.translatable("screen.farsight.map.header",
                        ClientDiscoveryCache.getRegions().size(),
                        ClientDiscoveryCache.getPois().size()),
                mapLeft, 12, 0xFF00E5FF);

        // Map frame
        context.fill(mapLeft - 2, mapTop - 2, mapLeft + mapWidth + 2, mapTop + mapHeight + 2, 0xFF00E5FF);
        context.fill(mapLeft, mapTop, mapLeft + mapWidth, mapTop + mapHeight, 0xFF090E14);

        context.enableScissor(mapLeft, mapTop, mapLeft + mapWidth, mapTop + mapHeight);

        // 1. Crisp 3x3 pixel topographic rendering with elevation shading & Fog of War
        int tile = 3;
        World world = client != null ? client.world : null;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        double playerX = (client != null && client.player != null) ? client.player.getX() : 0;
        double playerZ = (client != null && client.player != null) ? client.player.getZ() : 0;
        boolean hasPlayer = client != null && client.player != null;

        for (int sx = 0; sx < mapWidth; sx += tile) {
            for (int sy = 0; sy < mapHeight; sy += tile) {
                double wx = screenToWorldX(mapLeft + sx + tile * 0.5);
                double wz = screenToWorldZ(mapTop + sy + tile * 0.5);

                boolean nearPlayer = hasPlayer && ((wx - playerX) * (wx - playerX) + (wz - playerZ) * (wz - playerZ)) <= 80.0 * 80.0;
                boolean discovered = nearPlayer || ClientDiscoveryCache.isDiscovered(wx, wz);
                int color;
                if (!discovered) {
                    // Clean dark tactical Fog of War with subtle coordinate grid lines
                    int gx = Math.floorMod((int) Math.floor(wx), 128);
                    int gz = Math.floorMod((int) Math.floor(wz), 128);
                    boolean line = gx < (int) (blocksPerPixel * 2) || gz < (int) (blocksPerPixel * 2);
                    color = line ? 0xFF14202B : 0xFF0B1117;
                } else {
                    color = sampleCrispWorldColor(world, (int) wx, (int) wz, mutable);
                }
                context.fill(mapLeft + sx, mapTop + sy,
                        Math.min(mapLeft + sx + tile, mapLeft + mapWidth),
                        Math.min(mapTop + sy + tile, mapTop + mapHeight),
                        color);
            }
        }

        // 2. Draw Region Borders (glowing cyan rings)
        for (RegionData region : ClientDiscoveryCache.getRegions()) {
            drawRegionCircle(context, region.centerX(), region.centerZ(), region.radius(), 0xCC00E5FF);
        }

        // 3. Draw POIs (non-towers first, then Towers on top so Towers are never hidden!)
        PoiData hoveredPoi = null;
        PoiData activeWp = ClientDiscoveryCache.getActiveWaypoint();
        List<PoiData> sortedPois = new ArrayList<>(ClientDiscoveryCache.getPois());
        sortedPois.sort(Comparator.comparingInt(p ->
                (p.type() == PoiType.TOWER_UNCLAIMED || p.type() == PoiType.TOWER_CLAIMED) ? 1 : 0));

        long pulse = (System.currentTimeMillis() / 350) % 2;

        for (PoiData poi : sortedPois) {
            int px = worldToScreenX(poi.x());
            int py = worldToScreenZ(poi.z());
            if (px < mapLeft - 14 || px > mapLeft + mapWidth + 14 || py < mapTop - 14 || py > mapTop + mapHeight + 14) {
                continue;
            }

            boolean isTower = poi.type() == PoiType.TOWER_UNCLAIMED || poi.type() == PoiType.TOWER_CLAIMED;
            boolean isWp = activeWp != null && activeWp.id().equals(poi.id());
            int size = isTower ? 7 : 6;
            int bgColor = poi.type().getColor();

            if (isWp) {
                context.fill(px - size - 3, py - size - 3, px + size + 3, py + size + 3, 0xFFFFFFFF);
            } else if (poi.type() == PoiType.TOWER_UNCLAIMED && pulse == 0) {
                context.fill(px - size - 2, py - size - 2, px + size + 2, py + size + 2, 0xAAFF3B30);
            }

            context.fill(px - size, py - size, px + size, py + size, 0xFF000000);
            context.fill(px - size + 1, py - size + 1, px + size - 1, py + size - 1, bgColor);
            context.drawCenteredTextWithShadow(textRenderer, poi.type().getSymbol(), px, py - 4, 0xFFFFFFFF);

            // Show clear distance tag directly under Towers and active Waypoint
            if ((isTower || isWp) && client != null && client.player != null) {
                int dist = (int) Math.hypot(poi.x() - client.player.getX(), poi.z() - client.player.getZ());
                String tag = (poi.type() == PoiType.TOWER_UNCLAIMED ? "TOWER " : "") + dist + "m";
                int tw = textRenderer.getWidth(tag);
                context.fill(px - tw / 2 - 2, py + size + 1, px + tw / 2 + 2, py + size + 11, 0xBB000000);
                context.drawCenteredTextWithShadow(textRenderer, tag, px, py + size + 2, bgColor);
            }

            if (Math.abs(mouseX - px) <= 9 && Math.abs(mouseY - py) <= 9) {
                hoveredPoi = poi;
            }
        }

        // 4. Draw Player Marker
        if (client != null && client.player != null) {
            int plx = worldToScreenX(client.player.getX());
            int ply = worldToScreenZ(client.player.getZ());
            context.fill(plx - 5, ply - 5, plx + 5, ply + 5, 0xFF000000);
            context.fill(plx - 4, ply - 4, plx + 4, ply + 4, 0xFFFFFFFF);
            context.fill(plx - 2, ply - 2, plx + 2, ply + 2, 0xFF34C759);
        }

        context.disableScissor();

        // 5. Render Right Sidebar: Nearby Towers & Legend
        renderSidebar(context, mouseX, mouseY, activeWp);

        // 6. Tooltip for hovered POI
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

        // Footer hint (fitted so it never clips on smaller GUI scales)
        int hintX = mapLeft + 234;
        if (hintX + 120 < sideLeft) {
            context.drawTextWithShadow(textRenderer, "Drag: Pan | Scroll: Zoom", hintX, height - 18, 0xFF90A4AE);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderSidebar(DrawContext context, int mouseX, int mouseY, PoiData activeWp) {
        context.fill(sideLeft - 1, mapTop - 1, sideLeft + sideWidth + 1, mapTop + mapHeight + 1, 0xFF00E5FF);
        context.fill(sideLeft, mapTop, sideLeft + sideWidth, mapTop + mapHeight, 0xEE0D141C);

        int y = mapTop + 8;
        context.drawTextWithShadow(textRenderer, "NEARBY TOWERS", sideLeft + 8, y, 0xFF00E5FF);
        y += 14;

        sidebarEntries.clear();
        List<PoiData> towers = new ArrayList<>();
        for (PoiData poi : ClientDiscoveryCache.getPois()) {
            if (poi.type() == PoiType.TOWER_UNCLAIMED || poi.type() == PoiType.TOWER_CLAIMED) {
                towers.add(poi);
            }
        }
        if (client != null && client.player != null) {
            double px = client.player.getX();
            double pz = client.player.getZ();
            towers.sort(Comparator.comparingDouble(t -> Math.hypot(t.x() - px, t.z() - pz)));
        }

        if (towers.isEmpty()) {
            context.drawTextWithShadow(textRenderer, "Use Binoculars or", sideLeft + 8, y, 0xFF90A4AE);
            y += 11;
            context.drawTextWithShadow(textRenderer, "/farsight spawn_tower", sideLeft + 8, y, 0xFF90A4AE);
            y += 16;
        } else {
            int availableForTowers = Math.max(30, mapHeight - 132);
            int maxShow = Math.min(towers.size(), Math.max(1, availableForTowers / 30));
            for (int i = 0; i < maxShow; i++) {
                PoiData t = towers.get(i);
                int rowH = 26;
                boolean hovered = mouseX >= sideLeft + 4 && mouseX <= sideLeft + sideWidth - 4 &&
                        mouseY >= y && mouseY <= y + rowH;
                boolean isWp = activeWp != null && activeWp.id().equals(t.id());

                context.fill(sideLeft + 4, y, sideLeft + sideWidth - 4, y + rowH,
                        isWp ? 0xFF1E3A4C : (hovered ? 0xFF1A2634 : 0xFF121A24));
                context.drawBorder(sideLeft + 4, y, sideWidth - 8, rowH, t.type().getColor());

                int dist = (client != null && client.player != null)
                        ? (int) Math.hypot(t.x() - client.player.getX(), t.z() - client.player.getZ()) : 0;
                String status = t.type() == PoiType.TOWER_CLAIMED ? "[CLAIMED]" : "[LOCKED]";
                context.drawTextWithShadow(textRenderer, t.type().getSymbol() + " " + status + " " + dist + "m",
                        sideLeft + 8, y + 4, t.type().getColor());
                context.drawTextWithShadow(textRenderer, "X:" + t.x() + " Z:" + t.z(),
                        sideLeft + 8, y + 14, 0xFFB0BEC5);

                sidebarEntries.add(new SidebarEntry(t, sideLeft + 4, y, sideWidth - 8, rowH));
                y += rowH + 4;
            }
        }

        // Legend at bottom of sidebar
        y += 6;
        context.fill(sideLeft + 6, y, sideLeft + sideWidth - 6, y + 1, 0x4400E5FF);
        y += 6;
        context.drawTextWithShadow(textRenderer, "LEGEND", sideLeft + 8, y, 0xFF00E5FF);
        y += 12;

        PoiType[] legendTypes = {
                PoiType.TOWER_UNCLAIMED, PoiType.TOWER_CLAIMED,
                PoiType.VILLAGE, PoiType.PILLAGER_OUTPOST,
                PoiType.TEMPLE, PoiType.TRIAL_CHAMBERS,
                PoiType.ANCIENT_CITY, PoiType.SPECIAL_CAVE
        };
        for (PoiType pt : legendTypes) {
            if (y + 12 > mapTop + mapHeight - 4) break;
            context.fill(sideLeft + 8, y + 1, sideLeft + 17, y + 10, pt.getColor());
            context.drawCenteredTextWithShadow(textRenderer, pt.getSymbol(), sideLeft + 12, y + 1, 0xFF000000);
            context.drawTextWithShadow(textRenderer, pt.getDisplayName(), sideLeft + 21, y + 1, 0xFFD0D8E0);
            y += 12;
        }
    }

    private int sampleCrispWorldColor(World world, int wx, int wz, BlockPos.Mutable mutable) {
        if (world != null) {
            if (world.isChunkLoaded(wx >> 4, wz >> 4)) {
                int topY = world.getTopY(Heightmap.Type.WORLD_SURFACE, wx, wz) - 1;
                int northY = world.getTopY(Heightmap.Type.WORLD_SURFACE, wx, wz - (int) Math.max(1, blocksPerPixel)) - 1;
                mutable.set(wx, Math.max(world.getBottomY(), topY), wz);
                MapColor mapColor = world.getBlockState(mutable).getMapColor(world, mutable);
                if (mapColor != null && mapColor.color != 0) {
                    int base = mapColor.color;
                    int r = (base >> 16) & 0xFF;
                    int g = (base >> 8) & 0xFF;
                    int b = base & 0xFF;
                    // Topographic slope shading for crisp terrain readability
                    float shade = topY > northY ? 1.12f : (topY < northY ? 0.85f : 1.0f);
                    r = Math.min(255, (int) (r * shade));
                    g = Math.min(255, (int) (g * shade));
                    b = Math.min(255, (int) (b * shade));
                    return 0xFF000000 | (r << 16) | (g << 8) | b;
                }
            }
            // Unloaded chunk inside unlocked region: use biome foliage/sky color tint
            mutable.set(wx, 64, wz);
            RegistryEntry<Biome> biome = world.getBiome(mutable);
            int grass = biome.value().getGrassColorAt(wx, wz);
            int r = ((grass >> 16) & 0xFF) * 3 / 4;
            int g = ((grass >> 8) & 0xFF) * 3 / 4;
            int b = (grass & 0xFF) * 3 / 4;
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }
        return 0xFF1C2D27;
    }

    private void drawRegionCircle(DrawContext context, int cx, int cz, int radius, int color) {
        int steps = 96;
        for (int i = 0; i < steps; i++) {
            double angle = (2.0 * Math.PI * i) / steps;
            double wx = cx + Math.cos(angle) * radius;
            double wz = cz + Math.sin(angle) * radius;
            int sx = worldToScreenX(wx);
            int sy = worldToScreenZ(wz);
            context.fill(sx - 1, sy - 1, sx + 1, sy + 1, color);
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
        if (button == 0) {
            // Check sidebar tower clicks (center map + toggle waypoint)
            for (SidebarEntry entry : sidebarEntries) {
                if (mouseX >= entry.x && mouseX <= entry.x + entry.w && mouseY >= entry.y && mouseY <= entry.y + entry.h) {
                    camX = entry.poi.x();
                    camZ = entry.poi.z();
                    ClientDiscoveryCache.setActiveWaypoint(entry.poi);
                    if (client != null) {
                        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.2f));
                    }
                    return true;
                }
            }

            if (mouseX >= mapLeft && mouseX <= mapLeft + mapWidth && mouseY >= mapTop && mouseY <= mapTop + mapHeight) {
                for (PoiData poi : ClientDiscoveryCache.getPois()) {
                    int px = worldToScreenX(poi.x());
                    int py = worldToScreenZ(poi.z());
                    if (Math.abs(mouseX - px) <= 9 && Math.abs(mouseY - py) <= 9) {
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
            blocksPerPixel = Math.min(14.0, blocksPerPixel * 1.25);
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
