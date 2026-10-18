package caelum.hexparty.init;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import caelum.hexparty.Hexparty;
import caelum.hexparty.casting.patterns.copyparty.OpGetCopyparty;
import caelum.hexparty.casting.patterns.copyparty.OpGetIota;
import caelum.hexparty.casting.patterns.copyparty.OpSetCopyparty;
import net.minecraft.resources.ResourceLocation;
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

    //public static final ActionRegistryEntry TOJSON = make("tojson", HexPattern.fromAngles("eqaa", HexDir.EAST), new OpToJson());
    //public static final ActionRegistryEntry FROMJSON = make("fromjson", HexPattern.fromAngles("eqaaw", HexDir.EAST), new OpFromJson());
    public static final ActionRegistryEntry GET_COPYPARTY = make("get_copyparty", HexPattern.fromAngleString("wqqqqwqdqdwdqdeede", HexDir.NORTH_EAST), new OpGetCopyparty());
    public static final ActionRegistryEntry SET_COPYPARTY = make("set_copyparty", HexPattern.fromAngleString("wqqqqwqdqdwdqddqaq", HexDir.NORTH_EAST), new OpSetCopyparty());
    public static final ActionRegistryEntry GET_IOTA_FROM_RESPONSE = make("iota_from_response", HexPattern.fromAngleString("qqqqwqdwaawe", HexDir.NORTH_EAST), new OpGetIota());
    public static final ActionRegistryEntry REQUEST = make("request", HexPattern.fromAngleString("qqqqwqdqddqe", HexDir.NORTH_EAST), new OpRequest());
    public static final ActionRegistryEntry GET_RESPONSE = make("get_response", HexPattern.fromAngleString("qqqqwweaaead", HexDir.NORTH_EAST), new OpRequest());
    public static final ActionRegistryEntry STATUS_CODE = make("status_code", HexPattern.fromAngleString("qqqqwaedewed", HexDir.NORTH_EAST), new OpRequest());

    private static ActionRegistryEntry make(String name, HexPattern pattern, Action action) {
        return make(name, new ActionRegistryEntry(pattern, action));
    }

    private static ActionRegistryEntry make(String name, ActionRegistryEntry action) {
        ACTIONS.put(Hexparty.modLoc(name), action);
        return action;
    }
}
