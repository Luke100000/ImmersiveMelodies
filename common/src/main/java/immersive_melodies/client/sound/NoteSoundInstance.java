package immersive_melodies.client.sound;

import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class NoteSoundInstance extends EntityTrackingSoundInstance {
    long age;
    long length;
    long fallOff;

    public NoteSoundInstance(SoundEvent sound, SoundCategory category, float volume, float pitch, long length, Entity entity) {
        super(sound, category, volume, pitch, entity, 1);

        this.length = length;
        this.fallOff = Math.min(length / 2, 200);
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
        return volume * Math.min(1.0f, ((float) (length - age)) / fallOff);
    }

    @Override
    public float getPitch() {
        return pitch;
    }
}
