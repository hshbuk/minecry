package com.farsight.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public class PlayerProgress {
    public int score = 0;
    public int level = 1;
    public int skillPoints = 0;
    public int vitality = 0;   // +2 HP per level (max 5)
    public int swiftness = 0;  // +8% speed per level (max 5)
    public int lethality = 0;  // +1.5 attack damage per level (max 5)
    public int scavenger = 0;  // +25% bonus score per level (max 5)

    public int getNextLevelThreshold() {
        return level * 250;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("score", score);
        nbt.putInt("level", level);
        nbt.putInt("skillPoints", skillPoints);
        nbt.putInt("vitality", vitality);
        nbt.putInt("swiftness", swiftness);
        nbt.putInt("lethality", lethality);
        nbt.putInt("scavenger", scavenger);
        return nbt;
    }

    public static PlayerProgress fromNbt(NbtCompound nbt) {
        PlayerProgress p = new PlayerProgress();
        p.score = nbt.getInt("score");
        p.level = Math.max(1, nbt.getInt("level"));
        p.skillPoints = nbt.getInt("skillPoints");
        p.vitality = nbt.getInt("vitality");
        p.swiftness = nbt.getInt("swiftness");
        p.lethality = nbt.getInt("lethality");
        p.scavenger = nbt.getInt("scavenger");
        return p;
    }

    public void writeToBuf(PacketByteBuf buf) {
        buf.writeInt(score);
        buf.writeInt(level);
        buf.writeInt(skillPoints);
        buf.writeInt(vitality);
        buf.writeInt(swiftness);
        buf.writeInt(lethality);
        buf.writeInt(scavenger);
    }

    public static PlayerProgress readFromBuf(PacketByteBuf buf) {
        PlayerProgress p = new PlayerProgress();
        p.score = buf.readInt();
        p.level = buf.readInt();
        p.skillPoints = buf.readInt();
        p.vitality = buf.readInt();
        p.swiftness = buf.readInt();
        p.lethality = buf.readInt();
        p.scavenger = buf.readInt();
        return p;
    }
}
