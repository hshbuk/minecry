package com.farsight.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public record RegionData(
        String id,
        String name,
        String biomeTheme,
        int centerX,
        int centerZ,
        int radius,
        long unlockedAt,
        String unlockedBy,
        int tier
) {
    public boolean contains(double x, double z) {
        double dx = x - centerX;
        double dz = z - centerZ;
        return (dx * dx + dz * dz) <= ((double) radius * radius);
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("id", id);
        nbt.putString("name", name);
        nbt.putString("biomeTheme", biomeTheme);
        nbt.putInt("centerX", centerX);
        nbt.putInt("centerZ", centerZ);
        nbt.putInt("radius", radius);
        nbt.putLong("unlockedAt", unlockedAt);
        nbt.putString("unlockedBy", unlockedBy);
        nbt.putInt("tier", tier);
        return nbt;
    }

    public static RegionData fromNbt(NbtCompound nbt) {
        int tier = nbt.contains("tier") ? Math.max(1, nbt.getInt("tier")) : 1;
        return new RegionData(
                nbt.getString("id"),
                nbt.getString("name"),
                nbt.getString("biomeTheme"),
                nbt.getInt("centerX"),
                nbt.getInt("centerZ"),
                nbt.getInt("radius"),
                nbt.getLong("unlockedAt"),
                nbt.getString("unlockedBy"),
                tier
        );
    }

    public void writeToBuf(PacketByteBuf buf) {
        buf.writeString(id);
        buf.writeString(name);
        buf.writeString(biomeTheme);
        buf.writeInt(centerX);
        buf.writeInt(centerZ);
        buf.writeInt(radius);
        buf.writeLong(unlockedAt);
        buf.writeString(unlockedBy);
        buf.writeInt(tier);
    }

    public static RegionData readFromBuf(PacketByteBuf buf) {
        return new RegionData(
                buf.readString(),
                buf.readString(),
                buf.readString(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readLong(),
                buf.readString(),
                buf.readInt()
        );
    }
}
