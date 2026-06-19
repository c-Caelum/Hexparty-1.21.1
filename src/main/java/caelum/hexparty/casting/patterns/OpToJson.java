package caelum.hexparty.casting.patterns;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import caelum.hexparty.Hexparty;
import caelum.hexparty.api.DefaultedConstMediaAction;
import caelum.hexparty.api.IotaParser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OpToJson implements DefaultedConstMediaAction {

    public static final int argc = 1;

    @Override
    public long getMediaCost() {
        return 0L;
    }

    @Override
    public @NotNull List<Iota> execute(@NotNull List<? extends Iota> list, @NotNull CastingEnvironment castingEnvironment) throws Mishap {
        Iota arg = list.getFirst();
        Hexparty.LOGGER.info("json is: {}", IotaParser.iotaToJson(arg, castingEnvironment.getWorld()));
        return List.of();
    }

    @Override
    public int getArgc() {
        return argc;
    }
}
