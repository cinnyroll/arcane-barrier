package com.cinny.arcaneBarrier.barrier;

import com.cinny.arcaneBarrier.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class BarrierSavedData extends SavedData {
    public static final String DATA_ID = "arcane_barrier_state";

    private static final String KEY_BARRIER = "barrier";
    private static final String KEY_CURRENT_STAGE = "currentBarrierStage";
    private static final String KEY_FIRST_SPELLBOOK = "firstSpellbookCrafted";

    private int barrier;
    private BarrierStage currentBarrierStage;
    private boolean firstSpellbookCrafted;

    public BarrierSavedData() {
        this.barrier = Config.defaultBarrier;
        this.currentBarrierStage = BarrierStage.PROTECTED;
        this.firstSpellbookCrafted = false;
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
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt(KEY_BARRIER, this.barrier);
        tag.putString(KEY_CURRENT_STAGE, this.currentBarrierStage.id());
        tag.putBoolean(KEY_FIRST_SPELLBOOK, this.firstSpellbookCrafted);
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

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
