package immersive_melodies.fabric.cobalt.registration;

import immersive_melodies.cobalt.registration.Registration;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public class RegistrationImpl extends Registration.Impl {
    @Override
    public <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> obj) {
        T register = Registry.register(registry, id, obj.get());
        return () -> register;
    }
}
