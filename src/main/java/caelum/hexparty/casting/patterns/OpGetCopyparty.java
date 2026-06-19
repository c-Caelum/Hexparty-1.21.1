package caelum.hexparty.casting.patterns;

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.SpellCircleContext;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import caelum.hexparty.HTTPHandler;
import caelum.hexparty.Hexparty;
import caelum.hexparty.api.DefaultedConstMediaAction;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.NotNull;
import ram.talia.moreiotas.api.OperatorUtilsKt;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class OpGetCopyparty implements DefaultedConstMediaAction {

    public static final int argc = 2;

    @Override
    public long getMediaCost() {
        return 0L;
    }

    @Override
    public @NotNull List<Iota> execute(@NotNull List<? extends Iota> list, @NotNull CastingEnvironment castingEnvironment) throws Mishap {
        String path = OperatorUtilsKt.getString(list, 0, argc);
        String password = OperatorUtilsKt.getString(list, 1, argc);
        String[] headers = {
                "user-agent", String.format("Hexparty %s %s ", Hexparty.VERSION, castingEnvironment.getClass().getSimpleName(), getCasterIdentifier(castingEnvironment)),
                "pw",password
        };
        UUID uuid = UUID.randomUUID();
        HTTPHandler.INSTANCE.makeAndQueueRequest(uuid, path, headers, null, null);
        return List.of();
    }

    private String getCasterIdentifier(CastingEnvironment env) {
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
