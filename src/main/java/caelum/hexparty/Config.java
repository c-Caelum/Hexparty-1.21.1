package caelum.hexparty;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final List<String> ALL_METHODS = List.of("GET","HEAD","POST","PUT","DELETE","CONNECT",
            "OPTIONS","TRACE","PATCH");
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> COPYPARTY_URL = BUILDER
            .comment("What copyparty you would like hexparty to fetch its iotas from.")
            .comment("Note that the UUID of the caster is sent as a header, under 'UUID'.")
            .define("copypartyURL", "https://copyparty2.chloes.media/");

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_METHODS = BUILDER
            .comment("The allowed methods for any given request. This includes Get/Set Copyparty, so be careful.")
            .defineListAllowEmpty("allowedMethods", ALL_METHODS, () -> "", Config::validateMethod);

    public static boolean validateMethod(final Object method) {
        return ALL_METHODS.contains((String)method);
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}
