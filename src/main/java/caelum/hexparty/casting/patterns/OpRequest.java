package caelum.hexparty.casting.patterns;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota;
import caelum.hexparty.HTTPHandler;
import caelum.hexparty.Hexparty;
import caelum.hexparty.api.DefaultedConstMediaAction;
import caelum.hexparty.api.OperatorUtils;
import caelum.hexparty.casting.iota.ResponseIota;
import caelum.hexparty.casting.patterns.copyparty.OpGetCopyparty;
import net.minecraft.network.chat.Component;
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
    public @NotNull List<Iota> execute(@NotNull List<? extends Iota> args, @NotNull CastingEnvironment env) throws Mishap {

        String url = OperatorUtilsKt.getString(args, 3, argc);
        String[] headers = OperatorUtils.getStringList(args, 2, argc);

        if (headers.length % 2 != 0) {
            throw new MishapInvalidIota(args.get(0), 0, Component.translatable("string_even_list"));
        }

        String[] newHeaders = new String[headers.length+2];
        System.arraycopy(headers, 0, newHeaders, 0, headers.length);
        newHeaders[headers.length] = "user-agent";
        newHeaders[headers.length+1] = String.format("Hexparty v%s %s %s", Hexparty.VERSION, env.getClass().getSimpleName(), OpGetCopyparty.getCasterIdentifier(env));

        String method = OperatorUtilsKt.getString(args, 1, argc);
        String body = OperatorUtilsKt.getString(args, 0, argc);

        UUID uuid = UUID.randomUUID();
        HTTPHandler.INSTANCE.makeAndQueueRequestHex(uuid, url, newHeaders, method, body, args, 2, 1, env.getWorld().getServer().getTickCount());
        ResponseIota response = new ResponseIota(uuid);

        return List.of(response);
    }
}
