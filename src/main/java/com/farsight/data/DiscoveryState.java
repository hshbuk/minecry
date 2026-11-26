package com.farsight.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class DiscoveryState extends PersistentState {
    private static final String KEY = "farsight_discovery";

    private final Map<String, RegionData> regions = new LinkedHashMap<>();
    private final Map<String, PoiData> pois = new LinkedHashMap<>();
    private final Map<String, RoadData> roads = new LinkedHashMap<>();

    public static final Type<DiscoveryState> TYPE = new Type<>(
            DiscoveryState::new,
            DiscoveryState::fromNbt,
            null
    );

    public static DiscoveryState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE, KEY);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList regionList = new NbtList();
        for (RegionData r : regions.values()) {
            regionList.add(r.toNbt());
        }
        nbt.put("regions", regionList);

        NbtList poiList = new NbtList();
        for (PoiData p : pois.values()) {
            poiList.add(p.toNbt());
        }
        nbt.put("pois", poiList);

        NbtList roadList = new NbtList();
        for (RoadData road : roads.values()) {
            roadList.add(road.toNbt());
        }
        nbt.put("roads", roadList);
        return nbt;
    }

    public static DiscoveryState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        DiscoveryState state = new DiscoveryState();
        NbtList regionList = nbt.getList("regions", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < regionList.size(); i++) {
            RegionData r = RegionData.fromNbt(regionList.getCompound(i));
            state.regions.put(r.id(), r);
        }
        NbtList poiList = nbt.getList("pois", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < poiList.size(); i++) {
            PoiData p = PoiData.fromNbt(poiList.getCompound(i));
            state.pois.put(p.id(), p);
        }
        NbtList roadList = nbt.getList("roads", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < roadList.size(); i++) {
            RoadData road = RoadData.fromNbt(roadList.getCompound(i));
            state.roads.put(road.id(), road);
        }
        return state;
    }

    public boolean hasRegion(String id) {
        return regions.containsKey(id);
    }

    public void addRegion(RegionData region) {
        regions.put(region.id(), region);
        markDirty();
    }

    public void addPoi(PoiData poi) {
        pois.put(poi.id(), poi);
        markDirty();
    }

    public void removePoi(String id) {
        if (pois.remove(id) != null) {
            markDirty();
        }
    }

    public void removePoisIf(java.util.function.Predicate<PoiData> predicate) {
        if (pois.values().removeIf(predicate)) {
            markDirty();
        }
    }

    public Collection<RegionData> getRegions() {
        return regions.values();
    }

    public Collection<PoiData> getPois() {
        return pois.values();
    }

    public void addRoad(RoadData road) {
        roads.put(road.id(), road);
        markDirty();
    }

    public Collection<RoadData> getRoads() {
        return roads.values();
    }

    public boolean isDiscovered(double x, double z) {
        for (RegionData r : regions.values()) {
            if (r.contains(x, z)) {
                return true;
            }
        }
        return false;
    }
}
