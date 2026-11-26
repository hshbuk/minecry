package com.farsight.block.entity;

import com.farsight.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class TowerTerminalBlockEntity extends BlockEntity {
    private String biomeTheme = "plains";
    private String regionName = "Frontier Valley";
    private int radius = 512;
    private boolean activated = false;
    private long puzzleSeed = 0L;
    private int tier = 1;

    public TowerTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOWER_TERMINAL_BE, pos, state);
        this.puzzleSeed = pos.asLong();
        this.tier = com.farsight.world.TierCalculator.getTierForPos(pos);
    }

    public void configure(String biomeTheme, String regionName, int radius, long puzzleSeed, int tier) {
        this.biomeTheme = biomeTheme;
        this.regionName = regionName;
        this.radius = radius;
        this.puzzleSeed = puzzleSeed;
        this.tier = tier;
        markDirty();
    }

    public int getTier() {
        return tier;
    }

    public String getBiomeTheme() {
        return biomeTheme;
    }

    public String getRegionName() {
        return regionName;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
        markDirty();
    }

    public long getPuzzleSeed() {
        return puzzleSeed;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putString("biomeTheme", biomeTheme);
        nbt.putString("regionName", regionName);
        nbt.putInt("radius", radius);
        nbt.putBoolean("activated", activated);
        nbt.putLong("puzzleSeed", puzzleSeed);
        nbt.putInt("tier", tier);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.contains("biomeTheme")) biomeTheme = nbt.getString("biomeTheme");
        if (nbt.contains("regionName")) regionName = nbt.getString("regionName");
        if (nbt.contains("radius")) radius = nbt.getInt("radius");
        activated = nbt.getBoolean("activated");
        if (nbt.contains("puzzleSeed")) puzzleSeed = nbt.getLong("puzzleSeed");
        if (nbt.contains("tier")) tier = Math.max(1, nbt.getInt("tier"));
    }
}
