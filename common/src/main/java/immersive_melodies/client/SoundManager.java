package immersive_melodies.client;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public interface SoundManager {
    void playSound(double x, double y, double z, SoundEvent event, SoundCategory category, float volume, float pitch, float length);
}
