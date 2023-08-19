package immersive_melodies.client;

import net.minecraft.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public class MelodyProgressHandler {
    public static final MelodyProgressHandler INSTANCE = new MelodyProgressHandler();

    Map<Integer, MelodyProgress> progress = new HashMap<>();

    public MelodyProgress getProgress(Entity entity) {
        return progress.computeIfAbsent(entity.getId(), a -> new MelodyProgress());
    }

    public void setLastIndex(Entity entity, int index) {
        getProgress(entity).lastIndex = index;
    }

    public void setLastNote(Entity entity, float volume, float pitch, long length) {
        MelodyProgress progress = getProgress(entity);

        progress.lastNoteTime = entity.age;
        progress.lastVolume = volume;
        progress.lastPitch = pitch;
        progress.lastLength = length;

        progress.decayTime = Math.min(30.0f, length / 50.0f) / 4.0f;
        progress.attackTime = Math.min(5.0f, progress.decayTime / 2.0f);
    }
}
