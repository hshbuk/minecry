package com.farsight.client.render;

import com.farsight.entity.AttackHeliEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * Renders stylized 3D Scout Planes and Recon Helicopters using crisp Minecraft block geometry
 * and animated spinning propeller / rotor blades.
 */
public class VehicleEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    private final boolean isHeli;

    public VehicleEntityRenderer(EntityRendererFactory.Context ctx, boolean isHeli) {
        super(ctx);
        this.isHeli = isHeli;
        this.shadowRadius = 1.1f;
    }

    @Override
    public Identifier getTexture(T entity) {
        return Identifier.ofVanilla("textures/block/iron_block.png");
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));

        float spin = (entity.age + tickDelta) * (entity.hasPassengers() ? 45.0f : 4.0f);

        if (isHeli) {
            // Helicopter Cabin
            drawBox(matrices, vertexConsumers, light, Blocks.CYAN_TERRACOTTA.getDefaultState(), 0, 0.55f, 0, 1.3f, 1.0f, 2.2f);
            // Cockpit Glass
            drawBox(matrices, vertexConsumers, light, Blocks.LIGHT_BLUE_STAINED_GLASS.getDefaultState(), 0, 0.75f, 0.85f, 1.1f, 0.65f, 0.7f);
            // Tail Boom
            drawBox(matrices, vertexConsumers, light, Blocks.GRAY_CONCRETE.getDefaultState(), 0, 0.85f, -1.7f, 0.35f, 0.35f, 1.8f);
            // Landing Skids
            drawBox(matrices, vertexConsumers, light, Blocks.IRON_BLOCK.getDefaultState(), -0.75f, 0.1f, 0, 0.15f, 0.15f, 2.2f);
            drawBox(matrices, vertexConsumers, light, Blocks.IRON_BLOCK.getDefaultState(), 0.75f, 0.1f, 0, 0.15f, 0.15f, 2.2f);

            // Spinning Main Rotor
            matrices.push();
            matrices.translate(0, 1.65f, 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin));
            drawBox(matrices, vertexConsumers, light, Blocks.BLACK_CONCRETE.getDefaultState(), 0, 0, 0, 4.2f, 0.08f, 0.24f);
            drawBox(matrices, vertexConsumers, light, Blocks.BLACK_CONCRETE.getDefaultState(), 0, 0, 0, 0.24f, 0.08f, 4.2f);
            matrices.pop();
        } else {
            // Airplane Fuselage
            drawBox(matrices, vertexConsumers, light, Blocks.ORANGE_TERRACOTTA.getDefaultState(), 0, 0.55f, 0, 0.95f, 0.75f, 2.6f);
            // Cockpit Glass
            drawBox(matrices, vertexConsumers, light, Blocks.LIGHT_BLUE_STAINED_GLASS.getDefaultState(), 0, 0.95f, 0.1f, 0.75f, 0.45f, 0.9f);
            // Main Wings
            drawBox(matrices, vertexConsumers, light, Blocks.WHITE_CONCRETE.getDefaultState(), 0, 0.6f, 0.25f, 4.4f, 0.14f, 0.95f);
            // Tail Fin
            drawBox(matrices, vertexConsumers, light, Blocks.RED_CONCRETE.getDefaultState(), 0, 1.05f, -1.1f, 0.16f, 0.75f, 0.6f);
            drawBox(matrices, vertexConsumers, light, Blocks.WHITE_CONCRETE.getDefaultState(), 0, 0.7f, -1.1f, 1.6f, 0.12f, 0.45f);

            // Spinning Front Propeller
            matrices.push();
            matrices.translate(0, 0.55f, 1.38f);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(spin));
            drawBox(matrices, vertexConsumers, light, Blocks.BLACK_CONCRETE.getDefaultState(), 0, 0, 0, 1.4f, 0.16f, 0.1f);
            matrices.pop();
        }

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void drawBox(MatrixStack matrices, VertexConsumerProvider vcs, int light, BlockState state,
                         float x, float y, float z, float sx, float sy, float sz) {
        matrices.push();
        matrices.translate(x - sx * 0.5f, y - sy * 0.5f, z - sz * 0.5f);
        matrices.scale(sx, sy, sz);
        MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
                state, matrices, vcs, light, OverlayTexture.DEFAULT_UV
        );
        matrices.pop();
    }
}
