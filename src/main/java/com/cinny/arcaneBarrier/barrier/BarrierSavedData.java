package com.cinny.arcaneBarrier.barrier;

import com.cinny.arcaneBarrier.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BarrierSavedData extends SavedData {
    public static final String DATA_ID = "arcane_barrier_state";

    private static final String KEY_BARRIER = "barrier";
    private static final String KEY_CURRENT_STAGE = "currentBarrierStage";
    private static final String KEY_FIRST_SPELLBOOK = "firstSpellbookCrafted";
    private static final String KEY_FIRST_TIME_ONLY = "firstTimeOnly";

    private int barrier;
    private BarrierStage currentBarrierStage;
    private boolean firstSpellbookCrafted;
    private Map<String, Set<UUID>> firstTimeOnlyPlayers;

    public BarrierSavedData() {
        this.barrier = Config.defaultBarrier;
        this.currentBarrierStage = BarrierStage.PROTECTED;
        this.firstSpellbookCrafted = false;
        this.firstTimeOnlyPlayers = new HashMap<>();
    }

    public static BarrierSavedData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(BarrierSavedData::load, BarrierSavedData::new, DATA_ID);
    }

    public static BarrierSavedData load(CompoundTag tag) {
        BarrierSavedData data = new BarrierSavedData();
        data.barrier = clamp(tag.contains(KEY_BARRIER) ? tag.getInt(KEY_BARRIER) : Config.defaultBarrier);
        data.currentBarrierStage = BarrierStage.fromString(tag.getString(KEY_CURRENT_STAGE));
        data.firstSpellbookCrafted = tag.getBoolean(KEY_FIRST_SPELLBOOK);
        
        // Load first-time-only player tracking
        if (tag.contains(KEY_FIRST_TIME_ONLY)) {
            CompoundTag ftoTag = tag.getCompound(KEY_FIRST_TIME_ONLY);
            for (String eventId : ftoTag.getAllKeys()) {
                Set<UUID> playerSet = new HashSet<>();
                ListTag playerList = ftoTag.getList(eventId, Tag.TAG_STRING);
                for (int i = 0; i < playerList.size(); i++) {
                    try {
                        playerSet.add(UUID.fromString(playerList.getString(i)));
                    } catch (IllegalArgumentException e) {
                        // Skip invalid UUIDs
                    }
                }
                data.firstTimeOnlyPlayers.put(eventId, playerSet);
            }
        }
        
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt(KEY_BARRIER, this.barrier);
        tag.putString(KEY_CURRENT_STAGE, this.currentBarrierStage.id());
        tag.putBoolean(KEY_FIRST_SPELLBOOK, this.firstSpellbookCrafted);
        
        // Save first-time-only player tracking
        CompoundTag ftoTag = new CompoundTag();
        for (Map.Entry<String, Set<UUID>> entry : this.firstTimeOnlyPlayers.entrySet()) {
            ListTag playerList = new ListTag();
            for (UUID playerId : entry.getValue()) {
                playerList.add(Tag.TAG_STRING, playerId.toString());
            }
            ftoTag.put(entry.getKey(), playerList);
        }
        tag.put(KEY_FIRST_TIME_ONLY, ftoTag);
        
        return tag;
    }

    public int getBarrier() {
        return this.barrier;
    }

    public void setBarrier(int barrier) {
        this.barrier = clamp(barrier);
        this.setDirty();
    }

    public BarrierStage getCurrentBarrierStage() {
        return this.currentBarrierStage;
    }

    public void setCurrentBarrierStage(BarrierStage currentBarrierStage) {
        this.currentBarrierStage = currentBarrierStage;
        this.setDirty();
    }

    public boolean isFirstSpellbookCrafted() {
        return this.firstSpellbookCrafted;
    }

    public void setFirstSpellbookCrafted(boolean firstSpellbookCrafted) {
        this.firstSpellbookCrafted = firstSpellbookCrafted;
        this.setDirty();
    }

    /**
     * Check if a player has already triggered a first-time-only event
     */
    public boolean hasPlayerTriggeredEvent(String eventId, UUID playerId) {
        return this.firstTimeOnlyPlayers.getOrDefault(eventId, new HashSet<>()).contains(playerId);
    }

    /**
     * Mark that a player has triggered a first-time-only event
     */
    public void markEventTriggered(String eventId, UUID playerId) {
        this.firstTimeOnlyPlayers.computeIfAbsent(eventId, k -> new HashSet<>()).add(playerId);
        this.setDirty();
    }

    /**
     * Get all players who have triggered an event
     */
    public Set<UUID> getPlayersWhoTriggeredEvent(String eventId) {
        return new HashSet<>(this.firstTimeOnlyPlayers.getOrDefault(eventId, new HashSet<>()));
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
