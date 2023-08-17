package immersive_melodies.client.sound;

import immersive_melodies.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SoundManagerImpl implements SoundManager {
    private final MinecraftClient client;
    private final ScheduledExecutorService executor;

    public SoundManagerImpl(MinecraftClient client) {
        this.client = client;
        this.executor = new ScheduledThreadPoolExecutor(1);
    }

    public void playSound(double x, double y, double z, SoundEvent event, SoundCategory category, float volume, float pitch, long length, long sustain, long delay, Entity entity) {
        delay = Math.max(0, delay + Config.getInstance().getBufferDelay);
        executor.schedule(() -> {
            NoteSoundInstance positionedSoundInstance = new NoteSoundInstance(event, category, volume, pitch, length, sustain, entity);
            this.client.execute(() -> this.client.getSoundManager().play(positionedSoundInstance));
        }, delay, TimeUnit.MILLISECONDS);
    }
}
