package com.farsight.client.screen;

import com.farsight.network.ModNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

/**
 * Far Cry-inspired Signal Circuit Alignment Puzzle shown when activating a Tower Terminal.
 * Players rotate 3x3 circuit conduits to route power from the Left Input to the Right Output.
 */
public class TowerPuzzleScreen extends Screen {
    // Bitmasks for connections: 1=UP, 2=RIGHT, 4=DOWN, 8=LEFT
    private static final int UP = 1, RIGHT = 2, DOWN = 4, LEFT = 8;

    private final BlockPos terminalPos;
    private final String biomeTheme;
    private final String regionName;
    private final long seed;

    // 3x3 grid of base masks & current rotations (0..3 clockwise)
    private final int[][] baseMasks = new int[3][3];
    private final int[][] rotations = new int[3][3];
    private final boolean[][] powered = new boolean[3][3];

    private boolean solved = false;
    private int solveTicks = 0;

    private int gridLeft;
    private int gridTop;
    private static final int CELL_SIZE = 44;

    public TowerPuzzleScreen(BlockPos terminalPos, String biomeTheme, String regionName, long seed) {
        super(Text.translatable("screen.farsight.puzzle.title"));
        this.terminalPos = terminalPos;
        this.biomeTheme = biomeTheme;
        this.regionName = regionName;
        this.seed = seed;
        generatePuzzle();
        recomputePower();
    }

    private void generatePuzzle() {
        Random rng = new Random(seed ^ 0x5A17C0DEL);

        // Pick one of 4 guaranteed-solvable path layouts from (col=0,row=1) to (col=2,row=1)
        int layout = rng.nextInt(4);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                baseMasks[r][c] = UP | DOWN; // default filler
            }
        }

        if (layout == 0) {
            // Straight across middle with a bend
            baseMasks[1][0] = LEFT | UP;
            baseMasks[0][0] = DOWN | RIGHT;
            baseMasks[0][1] = LEFT | RIGHT;
            baseMasks[0][2] = LEFT | DOWN;
            baseMasks[1][2] = UP | RIGHT;
        } else if (layout == 1) {
            // Bottom route
            baseMasks[1][0] = LEFT | DOWN;
            baseMasks[2][0] = UP | RIGHT;
            baseMasks[2][1] = LEFT | RIGHT;
            baseMasks[2][2] = LEFT | UP;
            baseMasks[1][2] = DOWN | RIGHT;
        } else if (layout == 2) {
            // S-curve top-to-bottom
            baseMasks[1][0] = LEFT | UP;
            baseMasks[0][0] = DOWN | RIGHT;
            baseMasks[0][1] = LEFT | DOWN;
            baseMasks[1][1] = UP | DOWN;
            baseMasks[2][1] = UP | RIGHT;
            baseMasks[2][2] = LEFT | UP;
            baseMasks[1][2] = DOWN | RIGHT;
        } else {
            // S-curve bottom-to-top
            baseMasks[1][0] = LEFT | DOWN;
            baseMasks[2][0] = UP | RIGHT;
            baseMasks[2][1] = LEFT | UP;
            baseMasks[1][1] = DOWN | UP;
            baseMasks[0][1] = DOWN | RIGHT;
            baseMasks[0][2] = LEFT | DOWN;
            baseMasks[1][2] = UP | RIGHT;
        }

        // Scramble rotations (ensure not already solved)
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                rotations[r][c] = rng.nextInt(4);
            }
        }
        // Force at least two tiles out of alignment
        rotations[1][0] = (rotations[1][0] + 1) % 4;
        rotations[1][2] = (rotations[1][2] + 2) % 4;
    }

    private int getMask(int r, int c) {
        int m = baseMasks[r][c];
        int rot = rotations[r][c];
        for (int i = 0; i < rot; i++) {
            int next = 0;
            if ((m & UP) != 0) next |= RIGHT;
            if ((m & RIGHT) != 0) next |= DOWN;
            if ((m & DOWN) != 0) next |= LEFT;
            if ((m & LEFT) != 0) next |= UP;
            m = next;
        }
        return m;
    }

    private void recomputePower() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                powered[r][c] = false;
            }
        }
        // BFS from (1, 0) if it accepts LEFT input
        if ((getMask(1, 0) & LEFT) != 0) {
            flood(1, 0);
        }

        if (powered[1][2] && (getMask(1, 2) & RIGHT) != 0 && !solved) {
            solved = true;
            if (client != null) {
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_BEACON_ACTIVATE, 1.2f));
            }
            ClientPlayNetworking.send(new ModNetworking.SolvePuzzlePayload(terminalPos));
        }
    }

    private void flood(int r, int c) {
        if (powered[r][c]) return;
        powered[r][c] = true;
        int m = getMask(r, c);

        if ((m & UP) != 0 && r > 0 && (getMask(r - 1, c) & DOWN) != 0) flood(r - 1, c);
        if ((m & DOWN) != 0 && r < 2 && (getMask(r + 1, c) & UP) != 0) flood(r + 1, c);
        if ((m & LEFT) != 0 && c > 0 && (getMask(r, c - 1) & RIGHT) != 0) flood(r, c - 1);
        if ((m & RIGHT) != 0 && c < 2 && (getMask(r, c + 1) & LEFT) != 0) flood(r, c + 1);
    }

    @Override
    protected void init() {
        super.init();
        gridLeft = (width - CELL_SIZE * 3) / 2;
        gridTop = (height - CELL_SIZE * 3) / 2 + 10;
    }

    @Override
    public void tick() {
        super.tick();
        if (solved) {
            solveTicks++;
            if (solveTicks >= 22) {
                close();
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (solved) return true;
        if (mouseX >= gridLeft && mouseX < gridLeft + CELL_SIZE * 3 &&
                mouseY >= gridTop && mouseY < gridTop + CELL_SIZE * 3) {
            int c = (int) ((mouseX - gridLeft) / CELL_SIZE);
            int r = (int) ((mouseY - gridTop) / CELL_SIZE);
            rotations[r][c] = (rotations[r][c] + 1) % 4;
            if (client != null) {
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.15f));
            }
            recomputePower();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        int panelW = 240;
        int panelH = 215;
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;

        // Dark tactical Far Cry panel
        context.fill(px - 2, py - 2, px + panelW + 2, py + panelH + 2, 0xFF00E5FF);
        context.fill(px, py, px + panelW, py + panelH, 0xEE10161D);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("screen.farsight.puzzle.header", regionName),
                width / 2, py + 12, 0xFF00E5FF);
        context.drawCenteredTextWithShadow(textRenderer,
                solved ? Text.translatable("screen.farsight.puzzle.synced")
                        : Text.translatable("screen.farsight.puzzle.hint"),
                width / 2, py + 26, solved ? 0xFF34C759 : 0xFFB0BEC5);

        // Input & Output indicators
        int midY = gridTop + CELL_SIZE + CELL_SIZE / 2;
        context.fill(gridLeft - 22, midY - 5, gridLeft, midY + 5, 0xFF00E5FF);
        context.drawTextWithShadow(textRenderer, "IN", gridLeft - 38, midY - 4, 0xFF00E5FF);

        int outColor = solved ? 0xFF34C759 : 0xFFFF3B30;
        context.fill(gridLeft + CELL_SIZE * 3, midY - 5, gridLeft + CELL_SIZE * 3 + 22, midY + 5, outColor);
        context.drawTextWithShadow(textRenderer, "OUT", gridLeft + CELL_SIZE * 3 + 25, midY - 4, outColor);

        // Draw 3x3 cells
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int x = gridLeft + c * CELL_SIZE;
                int y = gridTop + r * CELL_SIZE;
                boolean hovered = !solved && mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE;

                context.fill(x + 2, y + 2, x + CELL_SIZE - 2, y + CELL_SIZE - 2, hovered ? 0xFF233242 : 0xFF18222D);
                int borderColor = powered[r][c] ? 0xFF00E5FF : 0xFF37474F;
                context.drawBorder(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4, borderColor);

                int cx = x + CELL_SIZE / 2;
                int cy = y + CELL_SIZE / 2;
                int wireColor = powered[r][c] ? (solved ? 0xFF34C759 : 0xFF00E5FF) : 0xFF607D8B;
                int m = getMask(r, c);

                // Center hub
                context.fill(cx - 5, cy - 5, cx + 5, cy + 5, wireColor);
                if ((m & UP) != 0) context.fill(cx - 3, y + 3, cx + 3, cy, wireColor);
                if ((m & DOWN) != 0) context.fill(cx - 3, cy, cx + 3, y + CELL_SIZE - 3, wireColor);
                if ((m & LEFT) != 0) context.fill(x + 3, cy - 3, cx, cy + 3, wireColor);
                if ((m & RIGHT) != 0) context.fill(cx, cy - 3, x + CELL_SIZE - 3, cy + 3, wireColor);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
