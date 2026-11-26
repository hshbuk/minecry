package com.farsight.client;

import com.farsight.data.PoiData;
import com.farsight.data.RegionData;

import java.util.ArrayList;
import java.util.List;

public class ClientDiscoveryCache {
    private static List<RegionData> regions = new ArrayList<>();
    private static List<PoiData> pois = new ArrayList<>();
    private static PoiData activeWaypoint = null;

    private static String bannerRegionName = "";
    private static int bannerPoisCount = 0;
    private static int bannerTicks = 0;

    public static void update(List<RegionData> newRegions, List<PoiData> newPois) {
        regions = new ArrayList<>(newRegions);
        pois = new ArrayList<>(newPois);
        if (activeWaypoint != null) {
            boolean stillExists = pois.stream().anyMatch(p -> p.id().equals(activeWaypoint.id()));
            if (!stillExists) {
                activeWaypoint = null;
            }
        }
    }

    public static List<RegionData> getRegions() {
        return regions;
    }

    public static List<PoiData> getPois() {
        return pois;
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

    public static void triggerBanner(String regionName, int poisRevealed) {
        bannerRegionName = regionName;
        bannerPoisCount = poisRevealed;
        bannerTicks = 120; // 6 seconds
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
