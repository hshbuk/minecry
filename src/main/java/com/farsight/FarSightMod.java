package com.farsight;

import com.farsight.data.DiscoveryState;
import com.farsight.network.ModNetworking;
import com.farsight.registry.ModBlockEntities;
import com.farsight.registry.ModBlocks;
import com.farsight.registry.ModItems;
import com.farsight.world.TowerPiece;
import com.farsight.world.TowerStructure;
import com.farsight.world.TowerTheme;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.StructureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FarSightMod implements ModInitializer {
    public static final String MOD_ID = "farsight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static StructurePieceType TOWER_PIECE_TYPE;
    public static StructureType<TowerStructure> TOWER_STRUCTURE_TYPE;

    @Override
    public void onInitialize() {
        ModBlocks.init();
        ModBlockEntities.init();
        com.farsight.registry.ModEntities.init();
        ModItems.init();

        TOWER_PIECE_TYPE = Registry.register(
                Registries.STRUCTURE_PIECE,
                Identifier.of(MOD_ID, "pillager_tower_piece"),
                TowerPiece::new
        );
        TOWER_STRUCTURE_TYPE = Registry.register(
                Registries.STRUCTURE_TYPE,
                Identifier.of(MOD_ID, "pillager_tower"),
                () -> TowerStructure.CODEC
        );

        ModNetworking.registerPayloads();
        ModNetworking.registerServerReceivers();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ModNetworking.syncPlayer(handler.getPlayer());
            ModNetworking.syncProgress(handler.getPlayer());
        });

        // Scale hostile mobs by region/distance Tier
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof net.minecraft.entity.mob.HostileEntity hostile) {
                int tier = com.farsight.world.TierCalculator.getTierForPos(hostile.getBlockPos());
                com.farsight.world.TierCalculator.scaleHostileMob(hostile, tier);
            }
        });

        // Award Score when players defeat enemies
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (damageSource.getAttacker() instanceof net.minecraft.server.network.ServerPlayerEntity player &&
                    entity instanceof net.minecraft.entity.mob.HostileEntity &&
                    player.getWorld() instanceof ServerWorld sw) {
                int tier = com.farsight.world.TierCalculator.getTierForPos(entity.getBlockPos());
                int pts = (entity instanceof net.minecraft.entity.mob.IllagerEntity ? 50 : 15) * tier;
                com.farsight.data.PlayerProgressState.get(sw).addScore(player, pts, 0);
                ModNetworking.syncProgress(player);
            }
        });

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            com.farsight.world.CutsceneManager.tick(server);
            if (server.getTicks() % 10 == 0) {
                for (ServerWorld world : server.getWorlds()) {
                    for (var player : world.getPlayers()) {
                        var box = player.getBoundingBox().expand(48);
                        for (var illager : world.getEntitiesByClass(
                                net.minecraft.entity.mob.IllagerEntity.class, box,
                                e -> e.isAlive() && e.getCommandTags().contains("farsight_tower_guard"))) {
                            illager.fallDistance = 0.0f;
                            var home = illager.getPositionTarget();
                            if (home != null && !home.equals(BlockPos.ORIGIN) &&
                                    (illager.getY() < home.getY() - 3.5 || illager.squaredDistanceTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5) > 100.0)) {
                                illager.teleport(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, false);
                            }
                        }
                    }
                }
            }
        });

        registerCommands();
        LOGGER.info("FarSight: World Discovery initialized!");
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("farsight")
                    .requires(src -> src.hasPermissionLevel(2))
                    .then(CommandManager.literal("spawn_tower")
                            .executes(ctx -> {
                                ServerWorld world = ctx.getSource().getWorld();
                                BlockPos playerPos = BlockPos.ofFloored(ctx.getSource().getPosition());
                                BlockPos origin = new BlockPos(
                                        playerPos.getX() + 14,
                                        world.getTopY(Heightmap.Type.WORLD_SURFACE, playerPos.getX() + 14, playerPos.getZ()),
                                        playerPos.getZ()
                                );
                                TowerTheme theme = TowerTheme.fromBiome(world.getBiome(origin));
                                TowerPiece piece = new TowerPiece(theme, origin);
                                BlockBox box = new BlockBox(
                                        origin.getX() - 8, origin.getY() - 5, origin.getZ() - 8,
                                        origin.getX() + 8, origin.getY() + 36, origin.getZ() + 8
                                );
                                piece.generate(world, world.getStructureAccessor(),
                                        world.getChunkManager().getChunkGenerator(),
                                        world.random, box, new ChunkPos(origin), origin);
                                ctx.getSource().sendFeedback(() -> Text.literal(
                                        "Spawned " + theme.defaultRegionName + " Pillager Tower at " + origin.toShortString()
                                ).formatted(Formatting.AQUA), true);
                                return 1;
                            }))
                    .then(CommandManager.literal("spawn_checkpoint")
                            .executes(ctx -> {
                                ServerWorld world = ctx.getSource().getWorld();
                                BlockPos playerPos = BlockPos.ofFloored(ctx.getSource().getPosition());
                                BlockPos origin = new BlockPos(
                                        playerPos.getX() + 10,
                                        world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, playerPos.getX() + 10, playerPos.getZ()),
                                        playerPos.getZ()
                                );
                                TowerTheme theme = TowerTheme.fromBiome(world.getBiome(origin));
                                com.farsight.world.RoadAndCheckpointGenerator.buildCheckpoint(world, origin, theme);
                                ctx.getSource().sendFeedback(() -> Text.literal(
                                        "Spawned Road Checkpoint at " + origin.toShortString()
                                ).formatted(Formatting.GOLD), true);
                                return 1;
                            }))
                    .then(CommandManager.literal("spawn_merchant")
                            .executes(ctx -> {
                                ServerWorld world = ctx.getSource().getWorld();
                                BlockPos p = BlockPos.ofFloored(ctx.getSource().getPosition());
                                var merchant = com.farsight.registry.ModEntities.FARSIGHT_MERCHANT.create(world);
                                if (merchant != null) {
                                    merchant.refreshPositionAndAngles(p.getX() + 2, p.getY(), p.getZ(), 0, 0);
                                    world.spawnEntity(merchant);
                                }
                                return 1;
                            }))
                    .then(CommandManager.literal("sync")
                            .executes(ctx -> {
                                ModNetworking.syncAllPlayers(ctx.getSource().getWorld());
                                return 1;
                            }))
                    .then(CommandManager.literal("info")
                            .executes(ctx -> {
                                DiscoveryState state = DiscoveryState.get(ctx.getSource().getWorld());
                                ctx.getSource().sendFeedback(() -> Text.literal(
                                        "Unlocked Regions: " + state.getRegions().size() +
                                                " | Discovered POIs: " + state.getPois().size()
                                ).formatted(Formatting.GREEN), false);
                                return 1;
                            }))
            );
        });
    }
}
