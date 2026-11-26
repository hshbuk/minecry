package com.farsight.data;

import com.farsight.FarSightMod;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerProgressState extends PersistentState {
    private static final String KEY = "farsight_player_progress";

    private static final Identifier VITALITY_ID = Identifier.of(FarSightMod.MOD_ID, "skill_vitality");
    private static final Identifier SWIFTNESS_ID = Identifier.of(FarSightMod.MOD_ID, "skill_swiftness");
    private static final Identifier LETHALITY_ID = Identifier.of(FarSightMod.MOD_ID, "skill_lethality");

    private final Map<UUID, PlayerProgress> players = new HashMap<>();

    public static final Type<PlayerProgressState> TYPE = new Type<>(
            PlayerProgressState::new,
            PlayerProgressState::fromNbt,
            null
    );

    public static PlayerProgressState get(ServerWorld world) {
        ServerWorld overworld = world.getServer().getOverworld();
        return overworld.getPersistentStateManager().getOrCreate(TYPE, KEY);
    }

    public PlayerProgress getOrCreate(UUID uuid) {
        return players.computeIfAbsent(uuid, k -> new PlayerProgress());
    }

    public void addScore(ServerPlayerEntity player, int baseAmount, int bonusSkillPoints) {
        PlayerProgress p = getOrCreate(player.getUuid());
        int boosted = (int) Math.round(baseAmount * (1.0 + p.scavenger * 0.25));
        p.score += boosted;
        p.skillPoints += bonusSkillPoints;

        while (p.score >= p.getNextLevelThreshold()) {
            p.score -= p.getNextLevelThreshold();
            p.level++;
            p.skillPoints++;
            player.getWorld().playSound(null, player.getBlockPos(),
                    SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.15f);
            player.sendMessage(Text.translatable("message.farsight.level_up", p.level).formatted(Formatting.GOLD), false);
        }
        markDirty();
    }

    public boolean upgradeSkill(ServerPlayerEntity player, int skillIndex) {
        PlayerProgress p = getOrCreate(player.getUuid());
        if (p.skillPoints <= 0) return false;

        boolean upgraded = switch (skillIndex) {
            case 0 -> { if (p.vitality < 5) { p.vitality++; yield true; } yield false; }
            case 1 -> { if (p.swiftness < 5) { p.swiftness++; yield true; } yield false; }
            case 2 -> { if (p.lethality < 5) { p.lethality++; yield true; } yield false; }
            case 3 -> { if (p.scavenger < 5) { p.scavenger++; yield true; } yield false; }
            default -> false;
        };

        if (upgraded) {
            p.skillPoints--;
            applyAttributes(player);
            markDirty();
        }
        return upgraded;
    }

    public void applyAttributes(ServerPlayerEntity player) {
        PlayerProgress p = getOrCreate(player.getUuid());

        var hp = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (hp != null) {
            hp.removeModifier(VITALITY_ID);
            if (p.vitality > 0) {
                hp.addPersistentModifier(new EntityAttributeModifier(
                        VITALITY_ID, p.vitality * 2.0, EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }

        var speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SWIFTNESS_ID);
            if (p.swiftness > 0) {
                speed.addPersistentModifier(new EntityAttributeModifier(
                        SWIFTNESS_ID, p.swiftness * 0.08, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        var dmg = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (dmg != null) {
            dmg.removeModifier(LETHALITY_ID);
            if (p.lethality > 0) {
                dmg.addPersistentModifier(new EntityAttributeModifier(
                        LETHALITY_ID, p.lethality * 1.5, EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound map = new NbtCompound();
        for (var entry : players.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue().toNbt());
        }
        nbt.put("players", map);
        return nbt;
    }

    public static PlayerProgressState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        PlayerProgressState state = new PlayerProgressState();
        NbtCompound map = nbt.getCompound("players");
        for (String key : map.getKeys()) {
            try {
                state.players.put(UUID.fromString(key), PlayerProgress.fromNbt(map.getCompound(key)));
            } catch (IllegalArgumentException ignored) {}
        }
        return state;
    }
}
