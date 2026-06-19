package caelum.hexparty.init;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import caelum.hexparty.casting.iota.ResponseIota;
import net.minecraft.resources.ResourceLocation;
import static caelum.hexparty.Hexparty.id;

import java.util.HashMap;
import java.util.function.BiConsumer;

public class HexpartyIotas {
    private static final HashMap<ResourceLocation, IotaType<?>> map = new HashMap<>();

    public static final IotaType<ResponseIota> RESPONSE = type("response", ResponseIota.TYPE);

    private static <T extends Iota> IotaType<T> type(String loc, IotaType<T> type) {
        IotaType<?> old = map.put(id(loc), type);
        if (old != null) {throw new IllegalArgumentException("Duplicate iotatype. Typo?");}
        return type;
    }

    public static void register(BiConsumer<IotaType<?>, ResourceLocation> consumer) {
        for (ResourceLocation location : map.keySet()) {
            consumer.accept(map.get(location), location);
        }
    }
}
