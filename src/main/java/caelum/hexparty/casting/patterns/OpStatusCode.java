package caelum.hexparty.casting.patterns;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import caelum.hexparty.api.DefaultedConstMediaAction;
import caelum.hexparty.api.OperatorUtils;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpResponse;
import java.util.List;

public class OpStatusCode implements DefaultedConstMediaAction {

    public static final int argc = 1;

    @Override
    public int getArgc() {
        return argc;
    }

    @Override
    public long getMediaCost() {
        return 0;
    }

    @Override
    public @NotNull List<Iota> execute(@NotNull List<? extends Iota> args, @NotNull CastingEnvironment env) throws Mishap {
        HttpResponse<String> response = OperatorUtils.getResponseUnchecked(args, 0, argc);
        return List.of(new DoubleIota(response == null ? -1 : response.statusCode()));
    }
}
