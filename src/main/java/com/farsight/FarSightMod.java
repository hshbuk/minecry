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
