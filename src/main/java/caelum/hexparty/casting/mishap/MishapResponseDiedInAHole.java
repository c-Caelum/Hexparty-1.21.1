package caelum.hexparty.casting.mishap;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MishapResponseDiedInAHole extends Mishap {

    Throwable culprit;

    public MishapResponseDiedInAHole(Throwable perp) {
        super();
        this.culprit=perp;
    }

    @Override
    public @NotNull FrozenPigment accentColor(@NotNull CastingEnvironment castingEnvironment, @NotNull Mishap.Context context) {
        return dyeColor(DyeColor.BLACK);
    }

    @Override
    public void execute(@NotNull CastingEnvironment castingEnvironment, @NotNull Mishap.Context context, @NotNull List<Iota> list) {
    }

    @Override
    protected @Nullable Component errorMessage(@NotNull CastingEnvironment castingEnvironment, @NotNull Mishap.Context context) {
        return Component.literal(culprit.getLocalizedMessage());
    }
}
