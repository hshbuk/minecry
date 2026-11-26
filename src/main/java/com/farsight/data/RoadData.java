package com.farsight.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public record RoadData(
        String id,
        int x1,
        int z1,
        int x2,
        int z2,
        String regionId
) {
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("id", id);
        nbt.putInt("x1", x1);
        nbt.putInt("z1", z1);
        nbt.putInt("x2", x2);
        nbt.putInt("z2", z2);
        nbt.putString("regionId", regionId);
        return nbt;
    }

    public static RoadData fromNbt(NbtCompound nbt) {
        return new RoadData(
                nbt.getString("id"),
                nbt.getInt("x1"),
                nbt.getInt("z1"),
                nbt.getInt("x2"),
                nbt.getInt("z2"),
                nbt.getString("regionId")
        );
    }

    public void writeToBuf(PacketByteBuf buf) {
        buf.writeString(id);
        buf.writeInt(x1);
        buf.writeInt(z1);
        buf.writeInt(x2);
        buf.writeInt(z2);
        buf.writeString(regionId);
    }

    public static RoadData readFromBuf(PacketByteBuf buf) {
        return new RoadData(
                buf.readString(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readString()
        );
    }
}
