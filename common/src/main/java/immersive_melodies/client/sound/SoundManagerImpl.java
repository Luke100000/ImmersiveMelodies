package immersive_melodies.client.sound;

import immersive_melodies.Config;
import immersive_melodies.mixin.MusicTrackerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SoundManagerImpl implements SoundManager {
    private final Minecraft client;
    private final ScheduledExecutorService executor;

    public SoundManagerImpl(Minecraft client) {
        this.client = client;
        this.executor = new ScheduledThreadPoolExecutor(1);
    }

    public CancelableSoundInstance playSound(double x, double y, double z, SoundEvent event, SoundSource category, float volume, float pitch, long length, long sustain, long delay, Entity entity) {
        delay = Math.max(0, delay + Config.getInstance().bufferDelay);
        NoteSoundInstance positionedSoundInstance = new NoteSoundInstance(event, category, volume, pitch, length, sustain, entity, isFirstPerson(entity));
        executor.schedule(() -> {
            this.client.execute(() -> this.client.getSoundManager().play(positionedSoundInstance));
        }, delay, TimeUnit.MILLISECONDS);
        return positionedSoundInstance;
    }

    @Override
    public boolean isFirstPerson(Entity entity) {
        return Minecraft.getInstance().getCameraEntity() == entity && !Minecraft.getInstance().gameRenderer.mainCamera().isDetached();
    }

    @Override
    public boolean audible(Entity entity) {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        return cameraEntity != null && cameraEntity.distanceTo(entity) < Config.getInstance().maxAudibleDistance;
    }

    @Override
    public void suppressGameMusic() {
        MusicTrackerAccessor musicTrackerAccessor = (MusicTrackerAccessor) this.client.getMusicManager();
        if (musicTrackerAccessor.getCurrentMusic() != null) {
            this.client.getMusicManager().stopPlaying();
        }

        MusicSuppression.suppress();
    }
}
