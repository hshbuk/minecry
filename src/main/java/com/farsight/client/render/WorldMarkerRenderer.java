package com.farsight.client.render;

import com.farsight.client.ClientDiscoveryCache;
import com.farsight.data.PoiData;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Renders glowing Far Cry-style vertical beacon pillars in the 3D world
 * for nearby discovered POIs and the player's active Waypoint.
 */
public class WorldMarkerRenderer {

    public static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Vec3d camPos = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();
        if (matrices == null) return;

        PoiData activeWp = ClientDiscoveryCache.getActiveWaypoint();
        boolean anyToDraw = false;

        for (PoiData poi : ClientDiscoveryCache.getPois()) {
            double distSq = client.player.squaredDistanceTo(poi.x() + 0.5, client.player.getY(), poi.z() + 0.5);
            boolean isWp = activeWp != null && activeWp.id().equals(poi.id());
            if (isWp || distSq < 280.0 * 280.0) {
                anyToDraw = true;
                break;
            }
        }
        if (!anyToDraw) return;

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        boolean hasVertices = false;

        for (PoiData poi : ClientDiscoveryCache.getPois()) {
            boolean isWp = activeWp != null && activeWp.id().equals(poi.id());
            if (!isWp && ClientDiscoveryCache.isTypeHidden(poi.type())) continue;
            double distSq = client.player.squaredDistanceTo(poi.x() + 0.5, client.player.getY(), poi.z() + 0.5);
            if (!isWp && distSq > 280.0 * 280.0) continue;

            int argb = poi.type().getColor();
            float r = ((argb >> 16) & 0xFF) / 255.0f;
            float g = ((argb >> 8) & 0xFF) / 255.0f;
            float b = (argb & 0xFF) / 255.0f;
            float a = isWp ? 0.55f : 0.25f;

            float x = poi.x() + 0.5f;
            float z = poi.z() + 0.5f;
            float yMin = Math.max(poi.y(), -32);
            float yMax = yMin + (isWp ? 160.0f : 90.0f);
            float half = isWp ? 0.45f : 0.25f;

            addPillarQuad(buffer, matrix, x - half, yMin, z - half, x + half, yMax, z - half, r, g, b, a);
            addPillarQuad(buffer, matrix, x + half, yMin, z - half, x + half, yMax, z + half, r, g, b, a);
            addPillarQuad(buffer, matrix, x + half, yMin, z + half, x - half, yMax, z + half, r, g, b, a);
            addPillarQuad(buffer, matrix, x - half, yMin, z + half, x - half, yMax, z - half, r, g, b, a);
            hasVertices = true;
        }

        if (hasVertices) {
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private static void addPillarQuad(BufferBuilder buf, Matrix4f m,
                                      float x1, float y1, float z1,
                                      float x2, float y2, float z2,
                                      float r, float g, float b, float a) {
        buf.vertex(m, x1, y1, z1).color(r, g, b, a);
        buf.vertex(m, x2, y1, z2).color(r, g, b, a);
        buf.vertex(m, x2, y2, z2).color(r, g, b, 0.02f);
        buf.vertex(m, x1, y2, z1).color(r, g, b, 0.02f);
    }
}
