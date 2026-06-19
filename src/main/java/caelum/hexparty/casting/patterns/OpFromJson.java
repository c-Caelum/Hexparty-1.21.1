package caelum.hexparty.casting.patterns;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import caelum.hexparty.Hexparty;
import caelum.hexparty.api.DefaultedConstMediaAction;
import caelum.hexparty.api.IotaParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import ram.talia.moreiotas.api.OperatorUtilsKt;

import java.util.List;

public class OpFromJson implements DefaultedConstMediaAction {

    public static final int argc = 1;

    @Override
    public long getMediaCost() {
        return 0L;
    }

    @Override
    public @NotNull List<Iota> execute(@NotNull List<? extends Iota> list, @NotNull CastingEnvironment castingEnvironment) throws Mishap {
        String arg = OperatorUtilsKt.getString(list, 0, argc);
        JsonElement json = JsonParser.parseString(arg);
        return List.of(IotaParser.jsonToIota(json, castingEnvironment.getWorld()));
    }

    @Override
    public int getArgc() {
        return argc;
    }
}
