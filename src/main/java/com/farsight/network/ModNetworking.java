package com.farsight.network;

import com.farsight.FarSightMod;
import com.farsight.data.DiscoveryState;
import com.farsight.data.PoiData;
import com.farsight.data.RegionData;
import com.farsight.world.TowerActivationHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ModNetworking {

    public record SyncDiscoveryPayload(List<RegionData> regions, List<PoiData> pois) implements CustomPayload {
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
        }

        private static SyncDiscoveryPayload read(RegistryByteBuf buf) {
            int rCount = buf.readInt();
            List<RegionData> regions = new ArrayList<>(rCount);
            for (int i = 0; i < rCount; i++) regions.add(RegionData.readFromBuf(buf));

            int pCount = buf.readInt();
            List<PoiData> pois = new ArrayList<>(pCount);
            for (int i = 0; i < pCount; i++) pois.add(PoiData.readFromBuf(buf));
            return new SyncDiscoveryPayload(regions, pois);
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

    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(SyncDiscoveryPayload.ID, SyncDiscoveryPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenPuzzlePayload.ID, OpenPuzzlePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RegionUnlockedPayload.ID, RegionUnlockedPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SolvePuzzlePayload.ID, SolvePuzzlePayload.CODEC);
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
    }

    public static void syncPlayer(ServerPlayerEntity player) {
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            com.farsight.world.TowerLocator.ensureNearbyTowersMarked(serverWorld, player.getBlockPos());
            DiscoveryState state = DiscoveryState.get(serverWorld);
            ServerPlayNetworking.send(player, new SyncDiscoveryPayload(
                    new ArrayList<>(state.getRegions()),
                    new ArrayList<>(state.getPois())
            ));
        }
    }

    public static void syncAllPlayers(ServerWorld world) {
        DiscoveryState state = DiscoveryState.get(world);
        SyncDiscoveryPayload packet = new SyncDiscoveryPayload(
                new ArrayList<>(state.getRegions()),
                new ArrayList<>(state.getPois())
        );
        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, packet);
        }
    }
}
