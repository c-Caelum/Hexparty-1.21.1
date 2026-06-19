package caelum.hexparty.api;

import at.petrak.hexcasting.api.casting.SpellList;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota;
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs;
import net.minecraft.network.chat.Component;
import ram.talia.moreiotas.api.casting.iota.StringIota;

import java.util.ArrayList;
import java.util.List;

public class OperatorUtils {
    public static String[] getStringList(List<? extends Iota> args, int idx, int argc) throws Mishap {
        if (args.size() < argc) {
            throw new MishapNotEnoughArgs(argc, args.size());
        }
        Iota arg = args.get(idx);
        if (!(arg instanceof ListIota listIota)) {
            throw new MishapInvalidIota(arg, idx, Component.translatable("hexcasting.iota.hexparty.response.desc"));
        }
        SpellList list = listIota.getList();
        String[] output = new String[list.size()];
        SpellList.SpellListIterator iterator = list.iterator();
        int size = list.size();
        for (int iteration = 0; iteration < size && iterator.hasNext(); iteration++) {
            Iota i = iterator.next();
            if (!(i instanceof StringIota stringIota)) {
                throw new MishapInvalidIota(arg, idx, Component.translatable("hexcasting.iota.hexparty.response.desc"));
            }
            output[iteration] = stringIota.string;
        }
        return output;
    }
}
