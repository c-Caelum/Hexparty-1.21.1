package caelum.hexparty.api;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota;
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs;
import at.petrak.hexcasting.api.utils.TreeList;
import caelum.hexparty.HTTPHandler;
import caelum.hexparty.casting.iota.ResponseIota;
import caelum.hexparty.casting.mishap.MishapResponseDiedInAHole;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import ram.talia.moreiotas.api.casting.iota.StringIota;
c
import java.net.http.HttpResponse;
import java.util.*;

public class OperatorUtils {
    public static String[] getStringList(List<? extends Iota> args, int idx, int argc) throws Mishap {
        if (args.size() < argc) {
            throw new MishapNotEnoughArgs(argc, args.size());
        }
        Iota arg = args.get(idx);
        if (!(arg instanceof ListIota listIota)) {
            throw MishapInvalidIota.of(arg, idx, "string_list");
        }
        TreeList<Iota> list = listIota.getList();
        String[] output = new String[list.size()];
        ListIterator<Iota> iterator = list.iterator();
        int size = list.size();
        for (int iteration = 0; iteration < size && iterator.hasNext(); iteration++) {
            Iota i = iterator.next();
            if (!(i instanceof StringIota stringIota)) {
                throw MishapInvalidIota.of(arg, idx, "string_list");
            }
            output[iteration] = stringIota.string;
        }
        return output;
    }

    public static HttpResponse<String> getResponseUnchecked(List<? extends Iota> args, int idx, int argc) throws Mishap {
        if (args.size() < argc) {
            throw new MishapNotEnoughArgs(argc, args.size());
        }
        Iota arg = args.get(idx);
        if (!(arg instanceof ResponseIota rsp)) {
            throw MishapInvalidIota.of(arg, idx, "response");
        }
        if (!HTTPHandler.INSTANCE.responses.containsKey(rsp.uuid)) {
            return null;
        }
        Either<HttpResponse<String>, Throwable> either = HTTPHandler.INSTANCE.responses.get(rsp.uuid).getFirst();
        if (either.right().isPresent()) {
            throw new MishapResponseDiedInAHole(either.right().get());
        }

        return either.left().get();
    }

    public static HttpResponse<String> getResponse(List<? extends Iota> args, int idx, int argc) throws Mishap {
        HttpResponse<String> str = getResponseUnchecked(args, idx, argc);
        if (str == null) {
            throw MishapInvalidIota.ofType(args.get(idx), idx, "finished_response");
        }
        return str;
    }
}
