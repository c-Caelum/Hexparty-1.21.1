package caelum.hexparty.casting.patterns.copyparty;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota;
import caelum.hexparty.HTTPHandler;
import caelum.hexparty.api.DefaultedConstMediaAction;
import caelum.hexparty.api.IotaParser;
import caelum.hexparty.api.OperatorUtils;
import caelum.hexparty.casting.iota.ResponseIota;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpResponse;
import java.util.List;

public class OpGetIota implements DefaultedConstMediaAction {
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
        HttpResponse<String> request = OperatorUtils.getResponse(args, 0, argc);
        env.printMessage(Component.literal(request.request().uri().toString()));
        if (request.statusCode() == 404) {
            throw MishapInvalidIota.ofType(args.get(0), 0, "wrong_path");
        }
        JsonElement requestAsJson;
        try {
            requestAsJson = JsonParser.parseString(request.body());
        } catch (JsonParseException jsonGoneWrong) {
            throw MishapInvalidIota.ofType(args.get(0), 0, "valid_json_response");
        }
        Iota iota = IotaParser.jsonToIota(requestAsJson, env.getWorld());
        HTTPHandler.INSTANCE.responses.remove(((ResponseIota)args.getFirst()).uuid);

        return List.of(iota);
    }
}
