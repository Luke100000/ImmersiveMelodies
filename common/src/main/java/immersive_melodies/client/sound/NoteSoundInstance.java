package immersive_melodies.client.sound;

import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class NoteSoundInstance extends EntityTrackingSoundInstance {
    long age;
    long length;
    long sustain;
    long fallOff;

    public NoteSoundInstance(SoundEvent sound, SoundCategory category, float volume, float pitch, long length, long sustain, Entity entity) {
        super(sound, category, volume, pitch, entity);

        // The minimum time one need to e.g., repress a key
        long minSustain = 50;

        this.length = length + sustain;
        this.sustain = sustain;
        this.fallOff = Math.min(minSustain, sustain);
    }

    @Override
    public void tick() {
        super.tick();

        // Fade out
        age += 50;
        if (age > length) {
            this.setDone();
        }

        // todo Pitch bend
    }

    @Override
    public float getVolume() {
        float f = Math.min(1.0f, ((float) (length - age)) / fallOff);
        return volume * f;
    }

    @Override
    public float getPitch() {
        return pitch;
    }
}
