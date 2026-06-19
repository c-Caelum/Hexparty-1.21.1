package caelum.hexparty.api;

import caelum.hexparty.Hexparty;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.api.casting.iota.GateIota;
import ram.talia.hexal.api.util.Anyone;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

// I hate rewriting classes...
public class HexalObfMapState extends SavedData {
    private BiMap<UUID, Integer> gateMap = HashBiMap.create();
    private BiMap<UUID, GateData> typedGateMap = HashBiMap.create();
    private BiMap<UUID, MoteData> moteMap = HashBiMap.create();
    public static final SavedData.Factory<HexalObfMapState> FACTORY = new SavedData.Factory<HexalObfMapState>(HexalObfMapState::new, (tag, provider) -> load(tag));

    public HexalObfMapState() {
    }

    public static HexalObfMapState getServerState(MinecraftServer server) {
        ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);
        assert level != null;
        HexalObfMapState state = level.getDataStorage().computeIfAbsent(FACTORY, "hexparty:hexal_gate_map");
        state.setDirty();
        return state;
    }

    @javax.annotation.Nullable
    public Integer getGateInt(UUID uuid) {
        return (Integer)this.gateMap.get(uuid);
    }

    @javax.annotation.Nullable
    public GateData getGateData(UUID uuid) {
        return (GateData)this.typedGateMap.get(uuid);
    }

    @javax.annotation.Nullable
    public UUID getGateUUID(int gateInt) {
        return (UUID)this.gateMap.inverse().get(gateInt);
    }

    public UUID getOrCreateGateUUID(GateData gData) {
        UUID uuid = this.getGateUUID(gData.index());
        if (uuid == null) {
            uuid = this.newGate(gData);
        }

        return uuid;
    }

    private void addGateToMap(UUID uuid, GateData gData) {
        this.gateMap.put(uuid, gData.index());
        this.typedGateMap.put(uuid, gData);
        this.setDirty();
    }

    public UUID newGate(GateData gData) {
        UUID uuid;
        for(uuid = UUID.randomUUID(); this.gateMap.containsKey(uuid) || this.typedGateMap.containsKey(uuid); uuid = UUID.randomUUID()) {
        }

        this.addGateToMap(uuid, gData);
        return uuid;
    }

    @javax.annotation.Nullable
    public MoteData getMoteData(UUID uuid) {
        return (MoteData)this.moteMap.get(uuid);
    }

    @Nullable
    public UUID getMoteObfUUID(MoteData moteData) {
        return (UUID)this.moteMap.inverse().get(moteData);
    }

    public UUID getOrCreateMoteObfUUID(MoteData moteData) {
        UUID uuid = this.getMoteObfUUID(moteData);
        if (uuid == null) {
            uuid = this.newMote(moteData);
        }

        return uuid;
    }

    private void addMoteToMap(UUID uuid, MoteData moteData) {
        this.moteMap.put(uuid, moteData);
        this.setDirty();
    }

    public UUID newMote(MoteData moteData) {
        UUID uuid;
        for(uuid = UUID.randomUUID(); this.moteMap.containsKey(uuid); uuid = UUID.randomUUID()) {
        }

        this.addMoteToMap(uuid, moteData);
        return uuid;
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        CompoundTag gateMapNbt = new CompoundTag();

        for(UUID uuid : this.typedGateMap.keySet()) {
            CompoundTag gateDataNbt = ((GateData)this.typedGateMap.get(uuid)).toNbt();
            gateMapNbt.put(uuid.toString(), gateDataNbt);
        }

        nbt.put("hexalTypedGateMap", gateMapNbt);
        CompoundTag moteMapNbt = new CompoundTag();

        for(UUID uuid : this.moteMap.keySet()) {
            MoteData moteData = (MoteData)this.moteMap.get(uuid);
            CompoundTag moteDataNbt = new CompoundTag();
            moteDataNbt.putUUID("storageUuid", moteData.uuid);
            moteDataNbt.putInt("index", moteData.index);
            moteDataNbt.putString("itemID", moteData.itemID);
            moteMapNbt.put(uuid.toString(), moteDataNbt);
        }

        nbt.put("hexalMoteMap", moteMapNbt);
        return nbt;
    }

    public static HexalObfMapState load(CompoundTag tag) {
        HexalObfMapState serverState = new HexalObfMapState();
        CompoundTag typedGateMapNbt = tag.getCompound("hexalTypedGateMap");
        if (typedGateMapNbt != null && !typedGateMapNbt.isEmpty()) {
            for(String uuidString : typedGateMapNbt.getAllKeys()) {
                GateData gData = HexalObfMapState.GateData.fromNbt(typedGateMapNbt.getCompound(uuidString));
                serverState.addGateToMap(UUID.fromString(uuidString), gData);
            }
        } else {
            CompoundTag gateMapNbt = tag.getCompound("hexalGateMap");

            for(String uuidString : gateMapNbt.getAllKeys()) {
                GateData simpleData = new GateData(gateMapNbt.getInt(uuidString), 0, (Vec3)null, (UUID)null);
                serverState.addGateToMap(UUID.fromString(uuidString), simpleData);
            }
        }

        CompoundTag moteMapNbt = tag.getCompound("hexalMoteMap");

        for(String uuidString : moteMapNbt.getAllKeys()) {
            CompoundTag moteDataNbt = moteMapNbt.getCompound(uuidString);
            MoteData moteData = new MoteData(moteDataNbt.getUUID("storageUuid"), moteDataNbt.getInt("index"), moteDataNbt.getString("itemID"));
            serverState.addMoteToMap(UUID.fromString(uuidString), moteData);
        }

        return serverState;
    }

    public static record GateData(int index, int type, Vec3 tVec, UUID entUuid) {
        public CompoundTag toNbt() {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("index", this.index);
            nbt.putInt("type", this.type);
            if (this.type == 1 || this.type == 2) {
                nbt.putDouble("tVecX", this.tVec.x);
                nbt.putDouble("tVecY", this.tVec.y);
                nbt.putDouble("tVecZ", this.tVec.z);
            }

            if (this.type == 2) {
                nbt.putUUID("entUuid", this.entUuid);
            }

            return nbt;
        }

        public static GateData fromNbt(CompoundTag nbt) {
            int index = nbt.getInt("index");
            int type = nbt.getInt("type");
            Vec3 tVec = null;
            UUID entUuid = null;
            if (type == 1 || type == 2) {
                tVec = new Vec3(nbt.getDouble("tVecX"), nbt.getDouble("tVecY"), nbt.getDouble("tVecZ"));
            }

            if (type == 2) {
                entUuid = nbt.getUUID("entUuid");
            }

            return new GateData(index, type, tVec, entUuid);
        }
    }

    @NotNull
    public static GateData GateDataFromIota(GateIota iota){
        Anyone<Vec3, GateIota.EntityAnchor, GateIota.DriftingAnchor> target = iota.getTarget();
        if (iota.isDrifting()) {
            return new GateData(iota.getGateIndex(), 0, null, null);
        }
        // means it's location anchored
        if (target.isA()) {
            return new GateData(iota.getGateIndex(), 1, target.getA(), null);
        }
        // entity anchored
        if (target.isB()) {
            GateIota.EntityAnchor anchor = target.getB();
            return new GateData(iota.getGateIndex(), 2, anchor.offset(), anchor.uuid());
        }
        return new GateData(iota.getGateIndex(), 0, null, null);
    }

    public static record MoteData(UUID uuid, int index, String itemID) {
    }
}
