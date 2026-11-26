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
        int panelW = 420;
        int panelH = 220;
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;

        // Left column: 4 Skill upgrade buttons
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(Text.literal("+1"), btn -> {
                ClientPlayNetworking.send(new ModNetworking.UpgradeSkillPayload(idx));
            }).dimensions(left + 172, top + 64 + i * 36, 28, 20).build());
        }

        // Right column: 3 Armory weapon buttons
        int[] costs = {200, 300, 450};
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(Text.literal(costs[i] + " PTS"), btn -> {
                ClientPlayNetworking.send(new ModNetworking.BuyWeaponPayload(idx));
            }).dimensions(left + 340, top + 64 + i * 44, 66, 20).build());
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xE0070B10);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int panelW = 420;
        int panelH = 220;
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;

        context.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, 0xFF00E5FF);
        context.fill(left, top, left + panelW, top + panelH, 0xEE0D141C);

        PlayerProgress p = ClientDiscoveryCache.getProgress();
        context.drawCenteredTextWithShadow(textRenderer,
                "FARSIGHT SKILLS & ARMORY",
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
            int y = top + 62 + i * 36;
            context.fill(left + 12, y, left + 166, y + 26, 0xFF141E29);
            context.drawTextWithShadow(textRenderer, skillNames[i], left + 16, y + 4, 0xFFFFFFFF);
            context.drawTextWithShadow(textRenderer, "Rank " + skillVals[i] + " / 5", left + 16, y + 15, 0xFF34C759);
        }

        // Armory Column
        context.drawTextWithShadow(textRenderer, "ARMORY (WEAPONS)", left + 224, top + 44, 0xFF00E5FF);
        String[] weaponNames = {"Tactical Machete", "Hunter Bow", "Flare Launcher"};
        String[] weaponDescs = {"9 DMG + Speed on Kill", "Fast High-Velocity Bow", "Incendiary Flare Shot"};
        for (int i = 0; i < 3; i++) {
            int y = top + 62 + i * 44;
            context.fill(left + 222, y, left + 335, y + 32, 0xFF141E29);
            context.drawTextWithShadow(textRenderer, weaponNames[i], left + 226, y + 5, 0xFFFF9500);
            context.drawTextWithShadow(textRenderer, weaponDescs[i], left + 226, y + 17, 0xFFB0BEC5);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
