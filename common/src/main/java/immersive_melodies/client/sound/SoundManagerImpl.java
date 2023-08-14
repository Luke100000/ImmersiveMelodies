package immersive_melodies.client.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class SoundManagerImpl implements SoundManager {
    private final MinecraftClient client;

    public SoundManagerImpl(MinecraftClient client) {
        this.client = client;
    }

    public void playSound(double x, double y, double z, SoundEvent event, SoundCategory category, float volume, float pitch, long length, long sustain, Entity entity) {
        NoteSoundInstance positionedSoundInstance = new NoteSoundInstance(event, category, volume, pitch, length, sustain, entity);
            this.client.getSoundManager().play(positionedSoundInstance);
    }
}
