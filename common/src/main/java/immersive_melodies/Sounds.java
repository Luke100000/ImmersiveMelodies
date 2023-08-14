package immersive_melodies;

import immersive_melodies.cobalt.registration.Registration;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class Sounds {
    public static final Instrument PIANO = new Instrument("piano");

    static Supplier<SoundEvent> register(String path) {
        return Registration.register(Registries.SOUND_EVENT, Common.locate(path), () -> SoundEvent.of(Common.locate(path)));
    }

    public static void bootstrap() {
        // nop
    }

    public static class Instrument {
        List<Supplier<SoundEvent>> octaves = new LinkedList<>();

        public Instrument(String name) {
            for (int octave = 1; octave <= 8; octave++) {
                octaves.add(register(name + ".c" + octave));
            }
        }

        public SoundEvent get(int octave) {
            return octaves.get(octave - 1).get();
        }
    }
}
