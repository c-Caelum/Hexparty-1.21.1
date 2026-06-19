package caelum.hexparty.init;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.OperationAction;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import caelum.hexparty.Hexparty;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.codec.binary.Hex;
import at.petrak.hexcasting.api.casting.castables.Action;
import caelum.hexparty.casting.patterns.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;


public class HexpartyPatterns {
    private static final Map<ResourceLocation, ActionRegistryEntry> ACTIONS = new HashMap<>();

    public static final void register(BiConsumer<ActionRegistryEntry, ResourceLocation> consumer) {
        for (Map.Entry<ResourceLocation, ActionRegistryEntry> entry : ACTIONS.entrySet()) {
            consumer.accept(entry.getValue(), entry.getKey());
        }
    }

    public static final ActionRegistryEntry TOJSON = make("tojson", HexPattern.fromAngles("eqaa", HexDir.EAST), new OpToJson());
    public static final ActionRegistryEntry FROMJSON = make("fromjson", HexPattern.fromAngles("eqaaw", HexDir.EAST), new OpFromJson());

    private static ActionRegistryEntry make(String name, HexPattern pattern, Action action) {
        return make(name, new ActionRegistryEntry(pattern, action));
    }

    private static ActionRegistryEntry make(String name, ActionRegistryEntry action) {
        ACTIONS.put(Hexparty.modLoc(name), action);
        return action;
    }
}
