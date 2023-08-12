package immersive_melodies.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;

public class SoundManagerImpl implements SoundManager {
    private final MinecraftClient client;

    public SoundManagerImpl(MinecraftClient client) {
        this.client = client;
    }

    public void playSound(double x, double y, double z, SoundEvent event, SoundCategory category, float volume, float pitch, float length) {
        PositionedSoundInstance positionedSoundInstance = new PositionedSoundInstance(event, category, volume, pitch, Random.create(0), x, y, z);
            this.client.getSoundManager().play(positionedSoundInstance);
    }
}
