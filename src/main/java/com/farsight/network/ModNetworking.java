package com.farsight.network;

import com.farsight.FarSightMod;
import com.farsight.data.DiscoveryState;
import com.farsight.data.PoiData;
import com.farsight.data.PoiType;
import com.farsight.data.RegionData;
import com.farsight.data.RoadData;
import com.farsight.world.CutsceneManager;
import com.farsight.world.TowerActivationHandler;
import com.farsight.world.TowerLocator;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;

public class ModNetworking {

    public record SyncDiscoveryPayload(List<RegionData> regions, List<PoiData> pois, List<RoadData> roads) implements CustomPayload {
        public static final Id<SyncDiscoveryPayload> ID = new Id<>(Identifier.of(FarSightMod.MOD_ID, "sync_discovery"));
        public static final PacketCodec<RegistryByteBuf, SyncDiscoveryPayload> CODEC = PacketCodec.of(
                SyncDiscoveryPayload::write,
                SyncDiscoveryPayload::read
        );

        private void write(RegistryByteBuf buf) {
            buf.writeInt(regions.size());
            for (RegionData r : regions) r.writeToBuf(buf);
            buf.writeInt(pois.size());
            for (PoiData p : pois) p.writeToBuf(buf);
            buf.writeInt(roads.size());
            for (RoadData road : roads) road.writeToBuf(buf);
        }

        private static SyncDiscoveryPayload read(RegistryByteBuf buf) {
            int rCount = buf.readInt();
            List<RegionData> regions = new ArrayList<>(rCount);
            for (int i = 0; i < rCount; i++) regions.add(RegionData.readFromBuf(buf));

            int pCount = buf.readInt();
            List<PoiData> pois = new ArrayList<>(pCount);
            for (int i = 0; i < pCount; i++) pois.add(PoiData.readFromBuf(buf));

            int roadCount = buf.readInt();
            List<RoadData> roads = new ArrayList<>(roadCount);
            for (int i = 0; i < roadCount; i++) roads.add(RoadData.readFromBuf(buf));

            return new SyncDiscoveryPayload(regions, pois, roads);
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record OpenPuzzlePayload(BlockPos pos, String biomeTheme, String regionName, long seed) implements CustomPayload {
        public static final Id<OpenPuzzlePayload> ID = new Id<>(Identifier.of(FarSightMod.MOD_ID, "open_puzzle"));
        public static final PacketCodec<RegistryByteBuf, OpenPuzzlePayload> CODEC = PacketCodec.of(
                OpenPuzzlePayload::write,
                OpenPuzzlePayload::read
        );

        private void write(RegistryByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeString(biomeTheme);
            buf.writeString(regionName);
            buf.writeLong(seed);
        }

        private static OpenPuzzlePayload read(RegistryByteBuf buf) {
            return new OpenPuzzlePayload(buf.readBlockPos(), buf.readString(), buf.readString(), buf.readLong());
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record SolvePuzzlePayload(BlockPos pos) implements CustomPayload {
        public static final Id<SolvePuzzlePayload> ID = new Id<>(Identifier.of(FarSightMod.MOD_ID, "solve_puzzle"));
        public static final PacketCodec<RegistryByteBuf, SolvePuzzlePayload> CODEC = PacketCodec.of(
                SolvePuzzlePayload::write,
                SolvePuzzlePayload::read
        );

        private void write(RegistryByteBuf buf) {
            buf.writeBlockPos(pos);
        }

        private static SolvePuzzlePayload read(RegistryByteBuf buf) {
            return new SolvePuzzlePayload(buf.readBlockPos());
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record RegionUnlockedPayload(String regionName, String biomeTheme, int poisRevealed) implements CustomPayload {
        public static final Id<RegionUnlockedPayload> ID = new Id<>(Identifier.of(FarSightMod.MOD_ID, "region_unlocked"));
        public static final PacketCodec<RegistryByteBuf, RegionUnlockedPayload> CODEC = PacketCodec.of(
                RegionUnlockedPayload::write,
                RegionUnlockedPayload::read
        );

        private void write(RegistryByteBuf buf) {
            buf.writeString(regionName);
            buf.writeString(biomeTheme);
            buf.writeInt(poisRevealed);
        }

        private static RegionUnlockedPayload read(RegistryByteBuf buf) {
            return new RegionUnlockedPayload(buf.readString(), buf.readString(), buf.readInt());
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record FastTravelPayload(String poiId) implements CustomPayload {
        public static final Id<FastTravelPayload> ID = new Id<>(Identifier.of(FarSightMod.MOD_ID, "fast_travel"));
        public static final PacketCodec<RegistryByteBuf, FastTravelPayload> CODEC = PacketCodec.of(
                FastTravelPayload::write,
                FastTravelPayload::read
        );

        private void write(RegistryByteBuf buf) {
            buf.writeString(poiId);
        }

        private static FastTravelPayload read(RegistryByteBuf buf) {
            return new FastTravelPayload(buf.readString());
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record CutsceneHudPayload(boolean active, String title, String subtitle) implements CustomPayload {
        public static final Id<CutsceneHudPayload> ID = new Id<>(Identifier.of(FarSightMod.MOD_ID, "cutscene_hud"));
        public static final PacketCodec<RegistryByteBuf, CutsceneHudPayload> CODEC = PacketCodec.of(
                CutsceneHudPayload::write,
                CutsceneHudPayload::read
        );

        private void write(RegistryByteBuf buf) {
            buf.writeBoolean(active);
            buf.writeString(title);
            buf.writeString(subtitle);
        }

        private static CutsceneHudPayload read(RegistryByteBuf buf) {
            return new CutsceneHudPayload(buf.readBoolean(), buf.readString(), buf.readString());
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record SkipCutscenePayload() implements CustomPayload {
        public static final Id<SkipCutscenePayload> ID = new Id<>(Identifier.of(FarSightMod.MOD_ID, "skip_cutscene"));
        public static final PacketCodec<RegistryByteBuf, SkipCutscenePayload> CODEC = PacketCodec.of(
                (v, buf) -> {},
                buf -> new SkipCutscenePayload()
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(SyncDiscoveryPayload.ID, SyncDiscoveryPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenPuzzlePayload.ID, OpenPuzzlePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RegionUnlockedPayload.ID, RegionUnlockedPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CutsceneHudPayload.ID, CutsceneHudPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SolvePuzzlePayload.ID, SolvePuzzlePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FastTravelPayload.ID, FastTravelPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SkipCutscenePayload.ID, SkipCutscenePayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SolvePuzzlePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                if (player.getWorld() instanceof ServerWorld serverWorld) {
                    TowerActivationHandler.tryActivateTower(serverWorld, player, payload.pos());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(FastTravelPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> handleFastTravel(player, payload.poiId()));
        });

        ServerPlayNetworking.registerGlobalReceiver(SkipCutscenePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> CutsceneManager.skip(player));
        });
    }

    private static void handleFastTravel(ServerPlayerEntity player, String poiId) {
        if (!(player.getWorld() instanceof ServerWorld world)) return;
        if (CutsceneManager.isInCutscene(player)) return;

        DiscoveryState state = DiscoveryState.get(world);
        PoiData target = state.getPois().stream()
                .filter(p -> p.id().equals(poiId))
                .findFirst()
                .orElse(null);

        if (target == null) return;
        if (target.type() != PoiType.TOWER_CLAIMED && target.type() != PoiType.CHECKPOINT_CLAIMED) {
            player.sendMessage(Text.translatable("message.farsight.fast_travel.locked").formatted(Formatting.RED), true);
            return;
        }

        // Force-load target chunk so heightmap & blocks are valid even 1000+ blocks away
        world.getChunk(target.x() >> 4, target.z() >> 4);

        double destX = target.x() + 0.5;
        double destZ = target.z() + 0.5;
        int topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, target.x(), target.z());
        double destY;
        if (target.type() == PoiType.TOWER_CLAIMED) {
            destY = target.y() > world.getBottomY() + 5 ? target.y() + 1.0 : Math.max(topY, world.getSeaLevel() + 1);
            destX = target.x() + 1.5;
            destZ = target.z() + 1.5;
        } else {
            destY = Math.max(topY, target.y() > world.getBottomY() + 5 ? target.y() : world.getSeaLevel() + 1);
        }

        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.1f);
        player.fallDistance = 0.0f;
        player.teleport(world, destX, destY, destZ, player.getYaw(), player.getPitch());
        world.playSound(null, BlockPos.ofFloored(destX, destY, destZ), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.2f);
        world.spawnParticles(ParticleTypes.END_ROD, destX, destY + 1.0, destZ, 20, 0.5, 0.7, 0.5, 0.05);
        player.sendMessage(Text.translatable("message.farsight.fast_travel.arrived", target.name()).formatted(Formatting.AQUA), true);
    }

    public static void syncPlayer(ServerPlayerEntity player) {
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            TowerLocator.ensureNearbyTowersMarked(serverWorld, player.getBlockPos());
            DiscoveryState state = DiscoveryState.get(serverWorld);
            ServerPlayNetworking.send(player, new SyncDiscoveryPayload(
                    new ArrayList<>(state.getRegions()),
                    new ArrayList<>(state.getPois()),
                    new ArrayList<>(state.getRoads())
            ));
        }
    }

    public static void syncAllPlayers(ServerWorld world) {
        DiscoveryState state = DiscoveryState.get(world);
        SyncDiscoveryPayload packet = new SyncDiscoveryPayload(
                new ArrayList<>(state.getRegions()),
                new ArrayList<>(state.getPois()),
                new ArrayList<>(state.getRoads())
        );
        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, packet);
        }
    }
}
