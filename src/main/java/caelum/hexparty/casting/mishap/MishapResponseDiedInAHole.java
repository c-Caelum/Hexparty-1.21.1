package caelum.hexparty.casting.mishap;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.utils.TreeList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public @NotNull TreeList<Iota> execute(@NotNull CastingEnvironment castingEnvironment, @NotNull Mishap.Context context, @NotNull TreeList<Iota> list) {
        return list;
    }

    @Override
    protected @Nullable Component errorMessage(@NotNull CastingEnvironment castingEnvironment, @NotNull Mishap.Context context) {
        return Component.literal(culprit.getLocalizedMessage());
    }
}
