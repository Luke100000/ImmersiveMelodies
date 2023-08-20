package immersive_melodies.cobalt.registration;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public class Registration {
    private static Impl INSTANCE;

    public static <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> obj) {
        return INSTANCE.register(registry, id, obj);
    }

    public abstract static class Impl {
        protected Impl() {
            INSTANCE = this;
        }

        public abstract <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> obj);
    }
}
