package com.farsight.world;

import com.farsight.FarSightMod;
import com.farsight.block.SignalRelayBlock;
import com.farsight.block.TowerTerminalBlock;
import com.farsight.block.entity.TowerTerminalBlockEntity;
import com.farsight.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class TowerPiece extends StructurePiece {
    private final TowerTheme theme;
    private final BlockPos origin;

    public TowerPiece(TowerTheme theme, BlockPos origin) {
        super(FarSightMod.TOWER_PIECE_TYPE, 0, new BlockBox(
                origin.getX() - 7, origin.getY() - 2, origin.getZ() - 7,
                origin.getX() + 7, origin.getY() + 34, origin.getZ() + 7
        ));
        this.theme = theme;
        this.origin = origin;
    }

    public TowerPiece(StructureContext context, NbtCompound nbt) {
        super(FarSightMod.TOWER_PIECE_TYPE, nbt);
        TowerTheme parsed;
        try {
            parsed = TowerTheme.valueOf(nbt.getString("Theme"));
        } catch (Exception e) {
            parsed = TowerTheme.PLAINS;
        }
        this.theme = parsed;
        this.origin = new BlockPos(nbt.getInt("OX"), nbt.getInt("OY"), nbt.getInt("OZ"));
    }

    @Override
    protected void writeNbt(StructureContext context, NbtCompound nbt) {
        nbt.putString("Theme", theme.name());
        nbt.putInt("OX", origin.getX());
        nbt.putInt("OY", origin.getY());
        nbt.putInt("OZ", origin.getZ());
    }

    @Override
    public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator,
                         Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        int baseX = origin.getX();
        int baseY = origin.getY();
        int baseZ = origin.getZ();

        BlockState primary = theme.primaryStone.getDefaultState();
        BlockState accent = theme.accentStone.getDefaultState();
        BlockState pillar = theme.pillar.getDefaultState();
        BlockState planks = theme.planks.getDefaultState();
        BlockState fence = theme.fence.getDefaultState();
        BlockState lantern = theme.lantern.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();

        // 1. Solid foundation (4 blocks deep so it sits cleanly on slopes)
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                for (int dy = -4; dy <= 0; dy++) {
                    setBlockIfInBox(world, chunkBox, baseX + dx, baseY + dy, baseZ + dz, primary);
                }
            }
        }

        // 2. Build 4 enclosed floors (each 5 blocks tall, total 20 blocks: baseY+1 .. baseY+20)
        for (int floor = 0; floor < 4; floor++) {
            int fy = baseY + 1 + floor * 5;
            for (int dy = 0; dy < 5; dy++) {
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        int x = baseX + dx;
                        int y = fy + dy;
                        int z = baseZ + dz;

                        boolean isCorner = Math.abs(dx) == 3 && Math.abs(dz) == 3;
                        boolean isWall = Math.abs(dx) == 3 || Math.abs(dz) == 3;

                        if (dy == 4) {
                            setBlockIfInBox(world, chunkBox, x, y, z, isWall ? accent : planks);
                        } else if (isCorner) {
                            setBlockIfInBox(world, chunkBox, x, y, z, pillar);
                        } else if (isWall) {
                            if (floor == 0 && dz == 3 && Math.abs(dx) <= 1 && dy <= 2) {
                                setBlockIfInBox(world, chunkBox, x, y, z, air);
                            } else if (dy == 2 && (dx == 0 || dz == 0)) {
                                setBlockIfInBox(world, chunkBox, x, y, z, Blocks.IRON_BARS.getDefaultState());
                            } else {
                                setBlockIfInBox(world, chunkBox, x, y, z, (dy == 0) ? accent : primary);
                            }
                        } else {
                            setBlockIfInBox(world, chunkBox, x, y, z, air);
                        }
                    }
                }
            }

            // Alternating ladder shaft
            int ladX = (floor % 2 == 0) ? baseX - 2 : baseX + 2;
            int ladZ = (floor % 2 == 0) ? baseZ - 2 : baseZ + 2;
            Direction ladFacing = (floor % 2 == 0) ? Direction.SOUTH : Direction.NORTH;
            for (int dy = 0; dy < 5; dy++) {
                setBlockIfInBox(world, chunkBox, ladX, fy + dy, ladZ,
                        Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, ladFacing));
            }

            setBlockIfInBox(world, chunkBox, baseX + 2, fy + 3, baseZ - 2, lantern);
        }

        // 3. Interior & Exterior Parkour Challenges!
        // Floor 2 (fy = baseY + 6): Stepping-stone parkour blocks leading up to Signal Relay #1
        int f2 = baseY + 6;
        setBlockIfInBox(world, chunkBox, baseX - 1, f2, baseZ + 1, accent);
        setBlockIfInBox(world, chunkBox, baseX + 1, f2 + 1, baseZ + 1, planks);
        setBlockIfInBox(world, chunkBox, baseX, f2 + 1, baseZ + 2, planks); // platform under relay 1
        setBlockIfInBox(world, chunkBox, baseX, f2 + 2, baseZ + 2,
                ModBlocks.SIGNAL_RELAY.getDefaultState().with(SignalRelayBlock.TUNED, false));

        // Floor 3 (fy = baseY + 11): Exterior Far Cry-style Ledge / Scaffold Parkour on East & North outer wall!
        int f3 = baseY + 11;
        // Open two doorways on East wall (dx = 3) for exterior ledge traversal
        setBlockIfInBox(world, chunkBox, baseX + 3, f3, baseZ + 1, air);
        setBlockIfInBox(world, chunkBox, baseX + 3, f3 + 1, baseZ + 1, air);
        setBlockIfInBox(world, chunkBox, baseX + 3, f3, baseZ - 2, air);
        setBlockIfInBox(world, chunkBox, baseX + 3, f3 + 1, baseZ - 2, air);
        // Exterior parkour platforms (with 1-block gaps to jump!)
        setBlockIfInBox(world, chunkBox, baseX + 4, f3 - 1, baseZ + 1, planks);
        setBlockIfInBox(world, chunkBox, baseX + 5, f3, baseZ, planks);
        setBlockIfInBox(world, chunkBox, baseX + 5, f3 + 1, baseZ - 2, planks);
        setBlockIfInBox(world, chunkBox, baseX + 4, f3, baseZ - 2, planks);

        // Signal Relay #2 on Floor 3
        setBlockIfInBox(world, chunkBox, baseX - 2, f3, baseZ,
                ModBlocks.SIGNAL_RELAY.getDefaultState().with(SignalRelayBlock.TUNED, false));

        // Floor 4 (fy = baseY + 16): Rafter beams to Loot Chest & Roof Ladder
        int f4 = baseY + 16;
        setBlockIfInBox(world, chunkBox, baseX, f4 + 1, baseZ + 1, planks);
        setBlockIfInBox(world, chunkBox, baseX - 1, f4 + 2, baseZ + 2, planks);

        // Security Barrier sealing the roof hatch above the top ladder at (baseX+2, baseY+21, baseZ+2)
        setBlockIfInBox(world, chunkBox, baseX + 2, baseY + 21, baseZ + 2, ModBlocks.TOWER_BARRIER.getDefaultState());

        // 4. Loot Chest on Floor 4 (scaled by Region Tier!)
        int tier = TierCalculator.getTierForPos(origin);
        BlockPos chestPos = new BlockPos(baseX - 2, f4, baseZ + 2);
        if (chunkBox.contains(chestPos)) {
            world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
            if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
                var loot = tier >= 4 ? LootTables.BASTION_TREASURE_CHEST
                        : (tier >= 2 ? LootTables.WOODLAND_MANSION_CHEST : LootTables.PILLAGER_OUTPOST_CHEST);
                chest.setLootTable(loot, random.nextLong());
            }
        }

        // 5. Rooftop Observation Deck (baseY + 21) with FULL guardrails so Pillagers cannot fall or jump off!
        int roofY = baseY + 21;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                boolean isEdge = Math.abs(dx) == 4 || Math.abs(dz) == 4;
                if (isEdge) {
                    setBlockIfInBox(world, chunkBox, baseX + dx, roofY - 1, baseZ + dz, accent);
                    setBlockIfInBox(world, chunkBox, baseX + dx, roofY, baseZ + dz, fence);
                    if (Math.abs(dx) == 4 && Math.abs(dz) == 4) {
                        setBlockIfInBox(world, chunkBox, baseX + dx, roofY + 1, baseZ + dz, lantern);
                    }
                }
            }
        }

        // 6. Radio Antenna Spire & Tower Signal Terminal at center of roof
        BlockPos terminalPos = new BlockPos(baseX, roofY, baseZ);
        if (chunkBox.contains(terminalPos)) {
            world.setBlockState(terminalPos,
                    ModBlocks.TOWER_TERMINAL.getDefaultState()
                            .with(TowerTerminalBlock.ACTIVATED, false)
                            .with(TowerTerminalBlock.FACING, Direction.SOUTH),
                    2);
            if (world.getBlockEntity(terminalPos) instanceof TowerTerminalBlockEntity be) {
                String regionName = theme.defaultRegionName + " (" + (baseX / 100) + "," + (baseZ / 100) + ")";
                be.configure(theme.id, regionName, 512, terminalPos.asLong(), tier);
            }
        }

        for (int dy = 0; dy < 7; dy++) {
            setBlockIfInBox(world, chunkBox, baseX, roofY + dy, baseZ - 2,
                    dy < 4 ? Blocks.IRON_BARS.getDefaultState() : Blocks.COPPER_BLOCK.getDefaultState());
        }
        setBlockIfInBox(world, chunkBox, baseX, roofY + 7, baseZ - 2, Blocks.LIGHTNING_ROD.getDefaultState());

        // 7. Spawn Tethered Pillager Guards (scaled by Tier!)
        spawnGuard(world, chunkBox, EntityType.PILLAGER, baseX + 1, baseY + 1, baseZ + 1, tier);
        spawnGuard(world, chunkBox, EntityType.PILLAGER, baseX - 1, f2, baseZ - 1, tier);
        spawnGuard(world, chunkBox, EntityType.VINDICATOR, baseX + 1, f3, baseZ - 1, tier);
        spawnGuard(world, chunkBox, EntityType.PILLAGER, baseX, f4, baseZ, tier);
        spawnGuard(world, chunkBox, EntityType.VINDICATOR, baseX + 2, roofY, baseZ, tier);
        if (tier >= 2) {
            spawnGuard(world, chunkBox, EntityType.VINDICATOR, baseX - 1, f2, baseZ + 1, tier);
        }
        if (tier >= 3) {
            spawnGuard(world, chunkBox, EntityType.PILLAGER, baseX + 1, f4, baseZ - 1, tier);
        }
        if (tier >= 4) {
            spawnGuard(world, chunkBox, EntityType.EVOKER, baseX - 2, roofY, baseZ, tier);
        }
    }

    private void setBlockIfInBox(StructureWorldAccess world, BlockBox box, int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        if (box.contains(pos)) {
            world.setBlockState(pos, state, 2);
        }
    }

    private void spawnGuard(StructureWorldAccess world, BlockBox box, EntityType<? extends IllagerEntity> type, int x, int y, int z, int tier) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!box.contains(pos)) return;
        IllagerEntity mob = type.create(world.toServerWorld());
        if (mob != null) {
            mob.setPersistent();
            mob.addCommandTag("farsight_tower_guard");
            mob.setPositionTarget(pos, 4);
            mob.refreshPositionAndAngles(x + 0.5, y, z + 0.5, 0.0f, 0.0f);
            mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.STRUCTURE, null);
            TierCalculator.scaleHostileMob(mob, tier);
            world.spawnEntityAndPassengers(mob);
        }
    }
}
