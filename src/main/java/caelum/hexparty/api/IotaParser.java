package caelum.hexparty.api;

import at.petrak.hexcasting.api.casting.iota.*;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import caelum.hexparty.Hexparty;
import com.google.gson.*;

import java.util.*;
import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import kotlin.Pair;
import kotlinx.serialization.json.Json;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;
import ram.talia.hexal.api.casting.iota.GateIota;
import ram.talia.hexal.api.casting.iota.MoteIota;
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager;
import ram.talia.hexal.api.util.Anyone;
import ram.talia.hexal.common.lib.hex.HexalIotaTypes;
import ram.talia.moreiotas.api.casting.iota.*;
import org.ejml.simple.SimpleMatrix;
import ram.talia.moreiotas.common.lib.hex.MoreIotasIotaTypes;

import static caelum.hexparty.api.JSONUtils.*;

public class IotaParser {
    // the set being the strings required to let it parse
    public static final HashMap<Set<String>, Converter<? extends Iota>> converterHashMap = new HashMap<>();
    // for converters with variable keys, so we can reduce iteration cost
    public static final HashMap<Set<String>, Converter<? extends Iota>> varKeysConverterHashMap = new HashMap<>();
    public static final HashMap<IotaType<? extends Iota>, Converter<? extends Iota>> iotaConverterHashmap = new HashMap<>();

    public static abstract class Converter<T extends Iota> {
          abstract public JsonElement toJson(Iota iota, ServerLevel world);
          abstract public T fromJson(JsonObject obj, ServerLevel world);
          public boolean validate(Set<String> keyset) {
              return true;
          }
    }

    public static Converter<?> getConverter(Set<String> keys) {
        if (converterHashMap.containsKey(keys)) {
            return converterHashMap.get(keys);
        }

        for (Map.Entry<Set<String>, Converter<?>> entry : varKeysConverterHashMap.entrySet()) {
            if (keys.containsAll(entry.getKey()) && entry.getValue().validate(keys)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static Iota jsonToIota(JsonElement obj, ServerLevel world) {

        if (obj.isJsonNull()) {
            return new NullIota();
        }

        if (obj.isJsonArray()) {
            JsonArray arr = obj.getAsJsonArray();
            List<Iota> iotas = new ArrayList<>(arr.size());
            for (JsonElement i : arr.asList()) {
                iotas.add(jsonToIota(i, world));
            }
            return new ListIota(iotas);
        }
        if (obj.isJsonPrimitive()) {
            JsonPrimitive primitive = obj.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return new BooleanIota(primitive.getAsBoolean());
            }
            if (primitive.isNumber()) {
                return new DoubleIota(primitive.getAsDouble());
            }
            return StringIota.make(primitive.getAsString());
        }
        JsonObject jsonObject = obj.getAsJsonObject();

        Converter<? extends Iota> converter = getConverter(jsonObject.keySet());
        if (converter != null) {
            Iota iota = converter.fromJson(jsonObject, world);
            return iota == null ? new GarbageIota() : iota;
        }
        return new GarbageIota();
    }

    public static JsonElement iotaToJson(Iota iota, ServerLevel world) {

        Converter<? extends Iota> converter = iotaConverterHashmap.get(iota.getType());
        if (converter == null) {
            JsonObject obj = new JsonObject();
            obj.add("garbage", new JsonPrimitive(true));
            return obj;
        }
        return converter.toJson(iota, world);
    }


    static {
        Converter<NullIota> NULL = new Converter<NullIota>() {
            @Override
            public JsonObject toJson(Iota iota, ServerLevel world) {
                JsonObject obj = new JsonObject();
                obj.addProperty("null", true);
                return obj;
            }

            @Override
            public NullIota fromJson(JsonObject obj, ServerLevel world) {
                return new NullIota();
            }
        };

        converterHashMap.put(Set.of("null"), NULL);
        iotaConverterHashmap.put(HexIotaTypes.NULL, NULL);

        Converter<Vec3Iota> VEC3 = new Converter<Vec3Iota>() {
            @Override
            public JsonElement toJson(Iota a, ServerLevel world) {
                Vec3 vec3 = ((Vec3Iota) a).getVec3();
                JsonObject obj = new JsonObject();
                obj.addProperty("x", vec3.x);
                obj.addProperty("y", vec3.y);
                obj.addProperty("z", vec3.z);
                return obj;
            }

            @Override
            public Vec3Iota fromJson(JsonObject obj, ServerLevel world) {
                if (!(isNumber(obj, "x") && isNumber(obj, "y") && isNumber(obj, "z"))) {
                    return null;
                }

                double x = obj.get("x").getAsDouble();
                double y = obj.get("y").getAsDouble();
                double z = obj.get("z").getAsDouble();

                return new Vec3Iota(new Vec3(x,y,z));
            }
        };
        converterHashMap.put(Set.of("x","y","z"), VEC3);
        iotaConverterHashmap.put(HexIotaTypes.VEC3, VEC3);

        Converter<PatternIota> PATTERN = new Converter<PatternIota>() {
            @Override
            public JsonElement toJson(Iota iota, ServerLevel world) {
                HexPattern pattern = ((PatternIota)iota).getPattern();
                JsonObject obj = new JsonObject();
                obj.addProperty("startDir", pattern.getStartDir().toString());
                obj.addProperty("angles", pattern.anglesSignature());
                return obj;
            }

            @Override
            public PatternIota fromJson(JsonObject obj, ServerLevel world) {
                if (!(isString(obj, "startDir") && isString(obj, "string"))) {
                    return null;
                }
                try {
                    HexDir startDir = HexDir.fromString(obj.get("startDir").getAsString());
                    String angles = obj.get("angles").getAsString();
                    HexPattern pattern = HexPattern.fromAnglesUnchecked(angles, startDir);
                    return new PatternIota(pattern);
                } catch (Exception e) {
                    return null;
                }
            }
        };
        converterHashMap.put(Set.of("startDir", "angles"), PATTERN);
        iotaConverterHashmap.put(HexIotaTypes.PATTERN, PATTERN);

        Converter<EntityIota> ENTITY = new Converter<EntityIota>() {
            @Override
            public JsonElement toJson(Iota iota, ServerLevel world) {
                EntityIota entityIota = (EntityIota)iota;
                UUID uuid = entityIota.getEntityId();
                Component temp = entityIota.getEntityName();
                temp = temp == null ? Component.empty() : temp;
                String name = temp.toString();
                JsonObject obj = new JsonObject();
                obj.addProperty("uuid", uuid.toString());
                obj.addProperty("name", name);
                return null;
            }

            @Override
            public EntityIota fromJson(JsonObject obj, ServerLevel world) {
                if (/*isString(obj, "name") &&*/ isString(obj, "uuid")) {
                    return null;
                }

                //String name = obj.get("name").getAsString();
                UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
                Entity entity = world.getEntity(uuid);

                if (entity == null || entity instanceof Player) {
                    return null;
                }

                return new EntityIota(entity);
            }
        };
        converterHashMap.put(Set.of("uuid", "name"), ENTITY);
        iotaConverterHashmap.put(HexIotaTypes.ENTITY, ENTITY);

        Converter<MatrixIota> MATRIX = new Converter<MatrixIota>() {
            @Override
            public JsonElement toJson(Iota iota, ServerLevel world) {
                SimpleMatrix matrix = ((MatrixIota) iota).getMatrix();

                JsonObject obj = new JsonObject();
                int rows = matrix.getNumRows();
                int cols = matrix.getNumCols();
                obj.addProperty("row", rows);
                obj.addProperty("col", cols);
                JsonArray arr = new JsonArray();
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        arr.add(matrix.get(i,j));
                    }
                }
                obj.add("matrix", arr);

                return null;
            }

            @Override
            public MatrixIota fromJson(JsonObject obj, ServerLevel world) {
                if (!(isNumber(obj, "row") && isNumber(obj, "col")) && isListOf(obj, "matrix", JSONUtils::isNumber)) {
                    return null;
                }

                int rows = obj.get("row").getAsNumber().intValue();
                int cols = obj.get("col").getAsNumber().intValue();
                SimpleMatrix matrix = new SimpleMatrix(rows, cols);
                JsonArray arr = obj.getAsJsonArray("matrix");

                for (int i = 0; i < rows*cols; i++) {
                    matrix.set(i, arr.get(i).getAsDouble());
                }

                return new MatrixIota(matrix);
            }
        };
        converterHashMap.put(Set.of("matrix", "row", "col"), MATRIX);
        iotaConverterHashmap.put(MoreIotasIotaTypes.MATRIX, MATRIX);

        Converter<IotaTypeIota> IOTA_TYPE = new Converter<IotaTypeIota>() {
            @Override
            public JsonElement toJson(Iota iota, ServerLevel world) {
                IotaType<?> type = ((IotaTypeIota)iota).getIotaType();
                if (!HexIotaTypes.REGISTRY.containsValue(type)) {
                    return null;
                }
                ResourceLocation loc = HexIotaTypes.REGISTRY.getKey(type);
                if (loc == null) {
                    return null;
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("iotaType", loc.toString());
                return obj;
            }

            @Override
            public IotaTypeIota fromJson(JsonObject obj, ServerLevel world) {
                if (!isString(obj, "iotaType")) {
                    return null;
                }
                ResourceLocation loc = ResourceLocation.tryParse(obj.get("iotaType").getAsString());
                if (loc == null || !HexIotaTypes.REGISTRY.containsKey(loc)) {
                    return null;
                }
                IotaType<?> type = HexIotaTypes.REGISTRY.get(loc);
                assert type != null;
                return new IotaTypeIota(type);
            }
        };
        converterHashMap.put(Set.of("iotaType"), IOTA_TYPE);
        iotaConverterHashmap.put(MoreIotasIotaTypes.IOTA_TYPE, IOTA_TYPE);

        Converter<EntityTypeIota> ENTITY_TYPE = new Converter<EntityTypeIota>() {
            @Override
            public JsonElement toJson(Iota iota, ServerLevel world) {
                EntityType<?> type = ((EntityTypeIota)iota).entityType;
                JsonObject obj = new JsonObject();
                if (!BuiltInRegistries.ENTITY_TYPE.containsValue(type)) {
                    obj.addProperty("null", true);
                    return obj;
                }
                ResourceLocation loc = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                obj.addProperty("entityType", loc.toString());
                return obj;
            }

            @Override
            public EntityTypeIota fromJson(JsonObject obj, ServerLevel world) {
                if (!isString(obj, "entityType")) {
                    return null;
                }
                String typeName = obj.get("entityType").getAsString();
                ResourceLocation loc = ResourceLocation.tryParse(typeName);
                if (loc == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(loc)) {
                    return null;
                }
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(loc);
                return new EntityTypeIota(type);
            }
        };
        converterHashMap.put(Set.of("entityType"), ENTITY_TYPE);
        iotaConverterHashmap.put(MoreIotasIotaTypes.ENTITY_TYPE, ENTITY_TYPE);

        Converter<ItemTypeIota> ITEM_TYPE = new Converter<ItemTypeIota>() {
            @Override
            public JsonElement toJson(Iota iota, ServerLevel world) {
                ItemTypeIota itemTypeIota = (ItemTypeIota) iota;
                Either<Item, Block> type = itemTypeIota.type;
                String name = type.map(item -> BuiltInRegistries.ITEM.getKey(item).toString(),
                        block -> BuiltInRegistries.BLOCK.getKey(block).toString());
                JsonObject obj = new JsonObject();
                obj.addProperty("itemType", name);
                obj.addProperty("isItem", type.left().isPresent());

                return obj;
            }

            @Override
            public ItemTypeIota fromJson(JsonObject obj, ServerLevel world) {
                if (!(isString(obj, "itemType") && isBoolean(obj, "isItem"))) {
                    return null;
                }
                String itemType = obj.get("itemType").getAsString();
                ResourceLocation loc = ResourceLocation.tryParse(itemType);
                if (loc == null ||
                        !(BuiltInRegistries.BLOCK.containsKey(loc) || BuiltInRegistries.ITEM.containsKey(loc))) {
                    return null;
                }
                if (obj.get("isItem").getAsBoolean()) {
                    Item item = BuiltInRegistries.ITEM.get(loc);
                    return new ItemTypeIota(item);
                } else {
                    Block block = BuiltInRegistries.BLOCK.get(loc);
                    return new ItemTypeIota(block);
                }
            }
        };
        converterHashMap.put(Set.of("itemType", "isItem"), ITEM_TYPE);
        iotaConverterHashmap.put(MoreIotasIotaTypes.ITEM_TYPE, ITEM_TYPE);

        Converter<MoteIota> MOTE = new Converter<MoteIota>() {
            @Override
            public JsonElement toJson(Iota iota, ServerLevel world) {
                MoteIota moteIota = (MoteIota) iota;
                MediafiedItemManager.Index itemIndex = moteIota.getItemIndex();
                if (itemIndex == null) {return null;}
                UUID storage = itemIndex.getStorage();
                int index = itemIndex.getIndex();
                String item = moteIota.getItem().toString();
                HexalObfMapState.MoteData thisMoteData = new HexalObfMapState.MoteData(storage, index, item);
                UUID thisMoteUUID = HexalObfMapState.getServerState(world.getServer()).getOrCreateMoteObfUUID(thisMoteData);
                JsonObject tag = new JsonObject();

                tag.addProperty("moteUuid", thisMoteUUID.toString());
                tag.addProperty("itemID", item);
                tag.addProperty("nexusUUID", storage.toString());

                return tag;
            }

            @Override
            public MoteIota fromJson(JsonObject obj, ServerLevel world) {
                if (!(isString(obj, "moteUuid") && isString(obj, "itemID") && isString(obj, "nexusUUID"))) {
                    return null;
                }
                UUID moteUUID = UUID.fromString(obj.get("moteUuid").getAsString());
                String itemID = obj.get("itemID").getAsString();
                UUID nexusUUID = UUID.fromString(obj.get("nexusUUID").getAsString());

                HexalObfMapState.MoteData data =
                        HexalObfMapState.getServerState(world.getServer()).getMoteData(moteUUID);

                if (data == null || !Objects.equals(data.itemID(), itemID)) {
                    return null;
                }

                UUID storageUUID = data.uuid();
                int index = data.index();
                MediafiedItemManager.Index index1 = new MediafiedItemManager.Index(storageUUID, index);

                return new MoteIota(index1);
            }
        };
        converterHashMap.put(Set.of("moteUuid", "itemID", "nexusUUID"), MOTE);
        iotaConverterHashmap.put(HexalIotaTypes.MOTE, MOTE);

        Converter<GateIota> GATE = new Converter<GateIota>() {
            @Override
            public JsonElement toJson(Iota u, ServerLevel world) {
                GateIota iota = (GateIota)u;
                HexalObfMapState.GateData gateData = HexalObfMapState.GateDataFromIota(iota);
                UUID gateUUID = HexalObfMapState.getServerState(world.getServer()).getOrCreateGateUUID(gateData);
                JsonObject obj = new JsonObject();
                obj.addProperty("gate", gateUUID.toString());
                Anyone<Vec3, GateIota.EntityAnchor, GateIota.DriftingAnchor> target = iota.getTarget();

                if (target.isC()) {
                    obj.addProperty("gateType", "drifting");
                    return obj;
                }
                Vec3 pos = gateData.tVec();
                JsonObject locObj = new JsonObject();
                locObj.addProperty("x", pos.x);
                locObj.addProperty("y", pos.y);
                locObj.addProperty("z", pos.z);
                // loc anchored
                if (target.isA()) {
                    obj.addProperty("type", "location");
                    obj.add("location", locObj);
                    return obj;
                }
                // now we know it's entity anchored
                obj.addProperty("type", "entity");
                obj.addProperty("entity", gateData.entUuid().toString());
                return obj;
            }

            @Override
            public GateIota fromJson(JsonObject obj, ServerLevel world) {
                if (!isString(obj, "type") && !isString(obj, "gate")) {
                    Hexparty.LOGGER.info("wrong key types");
                    return null;
                }
                String type = obj.get("type").getAsString();
                assert type != null;
                if (!type.equals("location") && !type.equals("entity") && !type.equals("drifting")) {
                    Hexparty.LOGGER.info("not in the types");
                    return null;
                }
                UUID gateUUID = UUID.fromString(obj.get("gate").getAsString());
                HexalObfMapState.GateData gateData = HexalObfMapState.getServerState(world.getServer()).getGateData(gateUUID);
                if (gateData == null) {
                    Hexparty.LOGGER.info("gatedata is null..");
                    return null;
                }
                return switch (gateData.type()) {
                    case 0 -> new GateIota(gateData.index(), null);
                    case 1 -> new GateIota(gateData.index(), Either.left(gateData.tVec()));
                    case 2 -> {
                        Pair<Entity, Vec3> gatePair = new Pair<Entity, Vec3>(world.getEntity(gateData.entUuid()), gateData.tVec());
                        yield new GateIota(gateData.index(), Either.right(gatePair));
                    }
                    default -> null;
                };
            }
        };
        varKeysConverterHashMap.put(Set.of("gate", "type"), GATE);
        iotaConverterHashmap.put(HexalIotaTypes.GATE, GATE);
    }
}
