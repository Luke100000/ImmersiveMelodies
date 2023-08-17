package immersive_melodies.client.sound;

import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public interface SoundManager {
    void playSound(double x, double y, double z, SoundEvent event, SoundCategory category, float volume, float pitch, long length, long sustain, long delay, Entity entity);
}
