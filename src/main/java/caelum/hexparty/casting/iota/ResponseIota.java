package caelum.hexparty.casting.iota;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import caelum.hexparty.init.HexpartyIotas;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import caelum.hexparty.HTTPHandler;
import net.minecraft.network.codec.StreamCodec;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Supplier;

public class ResponseIota extends Iota {
    public final UUID uuid;
    // a nice light blue
    public static final int COLOR = 0x6386BF;

    public ResponseIota(UUID uuid) {
        super(() -> HexpartyIotas.RESPONSE);
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean isTruthy() {
        return HTTPHandler.INSTANCE.responses.containsKey(uuid);
    }

    @Override
    protected boolean toleratesOther(Iota iota) {
        if(iota instanceof ResponseIota responseIota) {
            return responseIota.uuid == this.uuid;
        }
        return false;
    }

    @Override
    public Component display() {
        return Component.translatable("hexparty.tooltip.http_response", uuid).withColor(COLOR);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public static final IotaType<ResponseIota> TYPE = new IotaType<>() {
        public static final MapCodec<ResponseIota> MAP_CODEC =
                UUIDUtil.CODEC.xmap(ResponseIota::new, ResponseIota::getUuid).fieldOf("uuid");
        public static final StreamCodec<RegistryFriendlyByteBuf, ResponseIota> STREAM_CODEC =
                (UUIDUtil.STREAM_CODEC).map(ResponseIota::new, ResponseIota::getUuid).mapStream(buf -> buf);


        @Override
        public MapCodec<ResponseIota> codec() {
            return MAP_CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ResponseIota> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public int color() {
            return COLOR;
        }
    };
}
