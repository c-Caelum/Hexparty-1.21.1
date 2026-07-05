package caelum.hexparty.casting.patterns.copyparty;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import caelum.hexparty.HTTPHandler;
import caelum.hexparty.Hexparty;
import caelum.hexparty.api.DefaultedConstMediaAction;
import caelum.hexparty.casting.iota.ResponseIota;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import ram.talia.moreiotas.api.OperatorUtilsKt;

import java.util.List;
import java.util.UUID;

public class OpGetCopyparty implements DefaultedConstMediaAction {

    public static final int argc = 2;

    @Override
    public long getMediaCost() {
        return 0L;
    }

    @Override
    public @NotNull List<Iota> execute(@NotNull List<? extends Iota> list, @NotNull CastingEnvironment env) throws Mishap {
        String path = OperatorUtilsKt.getString(list, 0, argc).concat("/hex.json");
        String password = OperatorUtilsKt.getString(list, 1, argc);
        String[] headers = {
                "user-agent", String.format("Hexparty v%s %s %s", Hexparty.VERSION, env.getClass().getSimpleName(), getCasterIdentifier(env)),
                "pw",password
        };
        UUID uuid = UUID.randomUUID();
        HTTPHandler.INSTANCE.makeAndQueueRequestHex(uuid, path, headers, null, null, list, 0, 0, env.getWorld().getServer().getTickCount());
        return List.of(new ResponseIota(uuid));
    }

    public static String getCasterIdentifier(CastingEnvironment env) {
        if (env instanceof CircleCastEnv circleEnv) {
            return circleEnv.getImpetus().getBlockPos().toString();
        }
        LivingEntity entity = env.getCastingEntity();
        if (entity != null) {
            return entity.getUUID().toString();
        }
        return "N/A";
    }

    @Override
    public int getArgc() {
        return argc;
    }
}
