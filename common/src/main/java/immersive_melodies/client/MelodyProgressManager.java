package immersive_melodies.client;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MelodyProgressManager {
    public static final MelodyProgressManager INSTANCE = new MelodyProgressManager();

    Map<Entity, MelodyProgress> progress = new HashMap<>();

    public MelodyProgress getProgress(Entity entity) {
        return progress.computeIfAbsent(entity, a -> new MelodyProgress());
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

    public boolean isClose(Vec3d pos, float distance) {
        Iterator<Map.Entry<Entity, MelodyProgress>> iterator = progress.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Entity, MelodyProgress> entry = iterator.next();
            if ((System.currentTimeMillis() - entry.getValue().lastTime) < 1000 && entry.getKey().squaredDistanceTo(pos) < distance * distance) {
                return true;
            }
            if (entry.getKey().isRemoved()) {
                iterator.remove();
            }
        }

        return false;
    }
}
