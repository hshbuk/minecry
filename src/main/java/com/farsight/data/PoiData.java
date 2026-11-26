package com.farsight.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public record PoiData(
        String id,
        String name,
        PoiType type,
        int x,
        int y,
        int z,
        String regionId,
        String biomeId
) {
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("id", id);
        nbt.putString("name", name);
        nbt.putString("type", type.name());
        nbt.putInt("x", x);
        nbt.putInt("y", y);
        nbt.putInt("z", z);
        nbt.putString("regionId", regionId);
        nbt.putString("biomeId", biomeId);
        return nbt;
    }

    public static PoiData fromNbt(NbtCompound nbt) {
        PoiType parsedType;
        try {
            parsedType = PoiType.valueOf(nbt.getString("type"));
        } catch (IllegalArgumentException e) {
            parsedType = PoiType.TEMPLE;
        }
        return new PoiData(
                nbt.getString("id"),
                nbt.getString("name"),
                parsedType,
                nbt.getInt("x"),
                nbt.getInt("y"),
                nbt.getInt("z"),
                nbt.getString("regionId"),
                nbt.getString("biomeId")
        );
    }

    public void writeToBuf(PacketByteBuf buf) {
        buf.writeString(id);
        buf.writeString(name);
        buf.writeEnumConstant(type);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeString(regionId);
        buf.writeString(biomeId);
    }

    public static PoiData readFromBuf(PacketByteBuf buf) {
        return new PoiData(
                buf.readString(),
                buf.readString(),
                buf.readEnumConstant(PoiType.class),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readString(),
                buf.readString()
        );
    }
}
