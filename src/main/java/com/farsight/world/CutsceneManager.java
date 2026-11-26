package com.farsight.world;

import com.farsight.data.PoiData;
import com.farsight.data.PoiType;
import com.farsight.data.RegionData;
import com.farsight.network.ModNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;

import java.util.*;

/**
 * Far Cry-style Region Discovery Cinematic Flyover:
 * Smoothly flies the camera around up to 4 real POIs inside the newly unlocked region
 * (Villages, Checkpoints, Temples, Caves), then returns the player to their exact tower spot.
 */
public class CutsceneManager {
    private static final int TICKS_PER_STOP = 65; // ~3.25s per stop -> max ~13s total

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private record CutsceneStop(PoiData poi, String subtitle, boolean isCave) {}

    private static class Session {
        final ServerWorld world;
        final Vec3d returnPos;
        final float returnYaw;
        final float returnPitch;
        final GameMode returnGameMode;
        final List<CutsceneStop> stops;
        int tick = 0;
        int lastStopIndex = -1;

        Session(ServerWorld world, Vec3d returnPos, float returnYaw, float returnPitch,
                GameMode returnGameMode, List<CutsceneStop> stops) {
            this.world = world;
            this.returnPos = returnPos;
            this.returnYaw = returnYaw;
            this.returnPitch = returnPitch;
            this.returnGameMode = returnGameMode;
            this.stops = stops;
        }
    }

    public static boolean isInCutscene(ServerPlayerEntity player) {
        return SESSIONS.containsKey(player.getUuid());
    }

    public static void startRegionCutscene(ServerWorld world, ServerPlayerEntity player,
                                           RegionData region, List<PoiData> allPois) {
        List<CutsceneStop> stops = selectHighlights(region, allPois);
        if (stops.isEmpty()) return;

        Session session = new Session(
                world,
                player.getPos(),
                player.getYaw(),
                player.getPitch(),
                player.interactionManager.getGameMode(),
                stops
        );
        SESSIONS.put(player.getUuid(), session);
        player.changeGameMode(GameMode.SPECTATOR);
    }

    private static List<CutsceneStop> selectHighlights(RegionData region, List<PoiData> allPois) {
        List<PoiData> inRegion = new ArrayList<>();
        for (PoiData p : allPois) {
            if (region.contains(p.x(), p.z()) && p.type() != PoiType.TOWER_UNCLAIMED) {
                inRegion.add(p);
            }
        }

        List<CutsceneStop> stops = new ArrayList<>();
        // Pick up to 4 diverse highlights: Village, Checkpoint, Temple/Monument/Trial, Cave/Deep Dark
        addFirstMatching(inRegion, stops, PoiType.VILLAGE, "Village Discovered", false);
        addFirstMatching(inRegion, stops, PoiType.CHECKPOINT_LOCKED, "Enemy Road Checkpoint", false);
        addFirstMatching(inRegion, stops, PoiType.TRIAL_CHAMBERS, "Trial Chambers Below", true);
        if (stops.size() < 4) addFirstMatching(inRegion, stops, PoiType.TEMPLE, "Ancient Structure", false);
        if (stops.size() < 4) addFirstMatching(inRegion, stops, PoiType.PILLAGER_OUTPOST, "Pillager Outpost", false);
        if (stops.size() < 4) addFirstMatching(inRegion, stops, PoiType.ANCIENT_CITY, "Ancient City Below", true);
        if (stops.size() < 4) addFirstMatching(inRegion, stops, PoiType.SPECIAL_CAVE, "Special Cavern / Biome", true);
        if (stops.size() < 4) addFirstMatching(inRegion, stops, PoiType.RUINED_PORTAL, "Ruined Portal", false);

        // Always cap at 4 stops so cutscene stays snappy (~13s max)
        if (stops.size() > 4) {
            stops = new ArrayList<>(stops.subList(0, 4));
        }

        // If nothing else was found, do a quick 1-stop sweep of the claimed tower itself
        if (stops.isEmpty()) {
            for (PoiData p : inRegion) {
                if (p.type() == PoiType.TOWER_CLAIMED) {
                    stops.add(new CutsceneStop(p, region.name() + " Secured", false));
                    break;
                }
            }
        }
        return stops;
    }

    private static void addFirstMatching(List<PoiData> pool, List<CutsceneStop> out,
                                         PoiType type, String subtitle, boolean isCave) {
        for (PoiData p : pool) {
            if (p.type() == type) {
                out.add(new CutsceneStop(p, subtitle, isCave));
                return;
            }
        }
    }

    public static void skip(ServerPlayerEntity player) {
        Session session = SESSIONS.remove(player.getUuid());
        if (session != null) {
            restorePlayer(player, session);
        }
    }

    public static void tick(MinecraftServer server) {
        if (SESSIONS.isEmpty()) return;

        Iterator<Map.Entry<UUID, Session>> it = SESSIONS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Session> entry = it.next();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            Session s = entry.getValue();

            if (player == null || player.isDisconnected()) {
                it.remove();
                continue;
            }

            int totalTicks = s.stops.size() * TICKS_PER_STOP;
            if (s.tick >= totalTicks) {
                it.remove();
                restorePlayer(player, s);
                continue;
            }

            int stopIdx = s.tick / TICKS_PER_STOP;
            int localTick = s.tick % TICKS_PER_STOP;
            double progress = localTick / (double) TICKS_PER_STOP;
            CutsceneStop stop = s.stops.get(stopIdx);

            if (stopIdx != s.lastStopIndex) {
                s.lastStopIndex = stopIdx;
                ServerPlayNetworking.send(player, new ModNetworking.CutsceneHudPayload(
                        true,
                        stop.poi().name(),
                        stop.subtitle() + " (" + (stopIdx + 1) + "/" + s.stops.size() + ")"
                ));
            }

            // Compute smooth orbital flyover camera around the real POI coordinates
            int surfaceY = s.world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, stop.poi().x(), stop.poi().z());
            double targetY = stop.isCave() ? Math.max(stop.poi().y() + 2, surfaceY - 8) : Math.max(surfaceY + 4, 68);
            double orbitRadius = stop.isCave() ? 22.0 : 38.0;
            double camHeight = stop.isCave() ? surfaceY + 24.0 : targetY + 22.0;

            double angle = progress * Math.PI * 0.9 + stopIdx * 1.4;
            double camX = stop.poi().x() + Math.cos(angle) * orbitRadius;
            double camZ = stop.poi().z() + Math.sin(angle) * orbitRadius;

            double dx = stop.poi().x() + 0.5 - camX;
            double dy = targetY - camHeight;
            double dz = stop.poi().z() + 0.5 - camZ;
            double horiz = Math.hypot(dx, dz);

            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float pitch = (float) Math.toDegrees(-Math.atan2(dy, horiz));

            player.teleport(s.world, camX, camHeight, camZ, yaw, pitch);
            s.tick++;
        }
    }

    private static void restorePlayer(ServerPlayerEntity player, Session s) {
        player.changeGameMode(s.returnGameMode);
        player.teleport(s.world, s.returnPos.x, s.returnPos.y, s.returnPos.z, s.returnYaw, s.returnPitch);
        ServerPlayNetworking.send(player, new ModNetworking.CutsceneHudPayload(false, "", ""));
    }
}
