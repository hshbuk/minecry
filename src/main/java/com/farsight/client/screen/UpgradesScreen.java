package com.farsight.client.screen;

import com.farsight.client.ClientDiscoveryCache;
import com.farsight.data.PlayerProgress;
import com.farsight.network.ModNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class UpgradesScreen extends Screen {
    public UpgradesScreen() {
        super(Text.translatable("screen.farsight.upgrades.title"));
    }

    @Override
    protected void init() {
        super.init();
        int panelW = 430;
        int panelH = 248;
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;

        // Left column: 4 Skill upgrade buttons
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(Text.literal("+1"), btn -> {
                ClientPlayNetworking.send(new ModNetworking.UpgradeSkillPayload(idx));
            }).dimensions(left + 174, top + 62 + i * 38, 28, 20).build());
        }

        // Right column: 6 Armory items (3 Weapons + 3 Vehicles/Mounts)
        int[] costs = {200, 300, 450, 150, 600, 900};
        for (int i = 0; i < 6; i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(Text.literal(costs[i] + " PTS"), btn -> {
                ClientPlayNetworking.send(new ModNetworking.BuyWeaponPayload(idx));
            }).dimensions(left + 356, top + 60 + i * 29, 62, 18).build());
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xE0070B10);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int panelW = 430;
        int panelH = 248;
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;

        context.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, 0xFF00E5FF);
        context.fill(left, top, left + panelW, top + panelH, 0xEE0D141C);

        PlayerProgress p = ClientDiscoveryCache.getProgress();
        context.drawCenteredTextWithShadow(textRenderer,
                "FARSIGHT SKILLS, ARMORY & VEHICLES",
                width / 2, top + 10, 0xFF00E5FF);
        context.drawCenteredTextWithShadow(textRenderer,
                "LVL " + p.level + "  |  SCORE: " + p.score + " / " + p.getNextLevelThreshold() +
                        "  |  SKILL POINTS: " + p.skillPoints,
                width / 2, top + 24, 0xFFFFCC00);

        // Divider
        context.fill(left + 210, top + 42, left + 211, top + panelH - 12, 0x4400E5FF);

        // Skills Column
        context.drawTextWithShadow(textRenderer, "SKILLS (1 SP EACH)", left + 14, top + 44, 0xFF00E5FF);
        String[] skillNames = {"Vitality (+2 HP)", "Swiftness (+8% Spd)", "Lethality (+1.5 Dmg)", "Scavenger (+25% Pts)"};
        int[] skillVals = {p.vitality, p.swiftness, p.lethality, p.scavenger};
        for (int i = 0; i < 4; i++) {
            int y = top + 60 + i * 38;
            context.fill(left + 12, y, left + 168, y + 26, 0xFF141E29);
            context.drawTextWithShadow(textRenderer, skillNames[i], left + 16, y + 4, 0xFFFFFFFF);
            context.drawTextWithShadow(textRenderer, "Rank " + skillVals[i] + " / 5", left + 16, y + 15, 0xFF34C759);
        }

        // Armory & Vehicles Column
        context.drawTextWithShadow(textRenderer, "ARMORY & VEHICLES", left + 222, top + 44, 0xFF00E5FF);
        String[] itemNames = {
                "Tactical Machete", "Hunter Bow", "Flare Launcher",
                "Scout Horse Whistle", "Scout Plane Kit", "Recon Helicopter Kit"
        };
        int[] itemColors = {0xFFFF9500, 0xFFFF9500, 0xFFFF9500, 0xFF34C759, 0xFF00E5FF, 0xFF00E5FF};
        for (int i = 0; i < 6; i++) {
            int y = top + 58 + i * 29;
            context.fill(left + 220, y, left + 350, y + 22, 0xFF141E29);
            context.drawTextWithShadow(textRenderer, itemNames[i], left + 224, y + 7, itemColors[i]);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
