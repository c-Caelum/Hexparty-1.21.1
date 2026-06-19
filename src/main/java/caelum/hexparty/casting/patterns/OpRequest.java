package caelum.hexparty.casting.patterns;

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import caelum.hexparty.HTTPHandler;
import caelum.hexparty.api.DefaultedConstMediaAction;
import caelum.hexparty.api.OperatorUtils;
import caelum.hexparty.casting.iota.ResponseIota;
import org.jetbrains.annotations.NotNull;
import ram.talia.moreiotas.api.OperatorUtilsKt;

import java.util.List;
import java.util.UUID;

public class OpRequest implements DefaultedConstMediaAction {
    public static final int argc = 4;

    @Override
    public int getArgc() {
        return argc;
    }

    @Override
    public long getMediaCost() {
        return 0;
    }

    @Override
    public @NotNull List<Iota> execute(@NotNull List<? extends Iota> args, @NotNull CastingEnvironment castingEnvironment) throws Mishap {

        String url = OperatorUtilsKt.getString(args, 3, argc);
        String[] headers = OperatorUtils.getStringList(args, 2, argc);
        String method = OperatorUtilsKt.getString(args, 1, argc);
        String body = OperatorUtilsKt.getString(args, 0, argc);

        UUID uuid = UUID.randomUUID();
        HTTPHandler.INSTANCE.makeAndQueueRequest(uuid, url, headers, method, body);
        ResponseIota response = new ResponseIota(uuid);

        return List.of(response);
    }
}
