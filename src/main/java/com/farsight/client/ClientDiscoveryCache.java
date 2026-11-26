package com.farsight.client;

import com.farsight.data.PlayerProgress;
import com.farsight.data.PoiData;
import com.farsight.data.PoiType;
import com.farsight.data.RegionData;
import com.farsight.data.RoadData;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ClientDiscoveryCache {
    private static List<RegionData> regions = new ArrayList<>();
    private static List<PoiData> pois = new ArrayList<>();
    private static List<RoadData> roads = new ArrayList<>();
    private static final Set<PoiType> hiddenTypes = EnumSet.noneOf(PoiType.class);
    private static PoiData activeWaypoint = null;
    private static PlayerProgress progress = new PlayerProgress();

    public static PlayerProgress getProgress() {
        return progress;
    }

    public static void setProgress(PlayerProgress newProgress) {
        progress = newProgress;
    }

    private static String bannerRegionName = "";
    private static int bannerPoisCount = 0;
    private static int bannerTicks = 0;

    private static boolean cutsceneActive = false;
    private static String cutsceneTitle = "";
    private static String cutsceneSubtitle = "";

    public static void update(List<RegionData> newRegions, List<PoiData> newPois, List<RoadData> newRoads) {
        regions = new ArrayList<>(newRegions);
        pois = new ArrayList<>(newPois);
        roads = new ArrayList<>(newRoads);
        if (activeWaypoint != null) {
            PoiData updated = pois.stream()
                    .filter(p -> p.id().equals(activeWaypoint.id()))
                    .findFirst()
                    .orElse(null);
            activeWaypoint = updated;
        }
    }

    public static List<RegionData> getRegions() {
        return regions;
    }

    public static List<PoiData> getPois() {
        return pois;
    }

    public static List<RoadData> getRoads() {
        return roads;
    }

    public static boolean isDiscovered(double x, double z) {
        for (RegionData r : regions) {
            if (r.contains(x, z)) {
                return true;
            }
        }
        return false;
    }

    public static PoiData getActiveWaypoint() {
        return activeWaypoint;
    }

    public static void setActiveWaypoint(PoiData waypoint) {
        activeWaypoint = waypoint;
    }

    public static void toggleTypeHidden(PoiType type) {
        if (!hiddenTypes.remove(type)) {
            hiddenTypes.add(type);
        }
    }

    public static boolean isTypeHidden(PoiType type) {
        return hiddenTypes.contains(type);
    }

    public static void triggerBanner(String regionName, int poisRevealed) {
        bannerRegionName = regionName;
        bannerPoisCount = poisRevealed;
        bannerTicks = 120;
    }

    public static void setCutscene(boolean active, String title, String subtitle) {
        cutsceneActive = active;
        cutsceneTitle = title;
        cutsceneSubtitle = subtitle;
    }

    public static boolean isCutsceneActive() {
        return cutsceneActive;
    }

    public static String getCutsceneTitle() {
        return cutsceneTitle;
    }

    public static String getCutsceneSubtitle() {
        return cutsceneSubtitle;
    }

    public static void tick() {
        if (bannerTicks > 0) {
            bannerTicks--;
        }
    }

    public static boolean hasBanner() {
        return bannerTicks > 0;
    }

    public static String getBannerRegionName() {
        return bannerRegionName;
    }

    public static int getBannerPoisCount() {
        return bannerPoisCount;
    }

    public static int getBannerTicks() {
        return bannerTicks;
    }
}
