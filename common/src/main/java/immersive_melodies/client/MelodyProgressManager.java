package immersive_melodies.client;

import immersive_melodies.item.InstrumentItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
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
        for (Map.Entry<Entity, MelodyProgress> entry : progress.entrySet()) {
            if (entry.getValue().isPlaying() && entry.getKey().squaredDistanceTo(pos) < distance * distance) {
                return true;
            }
        }
        return false;
    }

    private long lastTime;

    public void sync(long time) {
        if (lastTime != time) {
            lastTime = time;
            progress.entrySet().removeIf(entry -> entry.getKey().isRemoved());

            List<Entity> list = MelodyProgressManager.INSTANCE.progress.entrySet().stream()
                    .filter(m -> m.getValue().isPlaying())
                    .map(Map.Entry::getKey)
                    .sorted((a, b) -> {
                        boolean b1 = a instanceof PlayerEntity;
                        boolean b2 = b instanceof PlayerEntity;
                        if (b1 && !b2) {
                            return 1;
                        } else if (!b1 && b2) {
                            return -1;
                        } else {
                            return a.getId() - b.getId();
                        }
                    })
                    .toList();

            for (int i0 = 0; i0 < list.size(); i0++) {
                Entity entity0 = list.get(i0);
                for (int i1 = list.size() - 1; i1 > i0; i1--) {
                    Entity entity1 = list.get(i1);
                    if (entity0.distanceTo(entity1) < 16.0f) {
                        // Two entities are close, entity 0 will try to mimic entity 1
                        // Thus, the higher in the order the higher the priority
                        MelodyProgress progress0 = getProgress(entity0);
                        MelodyProgress progress1 = getProgress(entity1);
                        if (progress0.getCurrentlyPlaying().equals(progress1.getCurrentlyPlaying())) {
                            if (Math.abs(progress0.time - progress1.time) > 100) {
                                progress0.time = progress1.time;
                                progress0.lastIndex = progress1.lastIndex;
                            }
                        } else if (!(entity0 instanceof PlayerEntity)) {
                            entity0.getHandItems().forEach(h -> {
                                if (h.getItem() instanceof InstrumentItem instrumentItem) {
                                    instrumentItem.play(h, new Identifier(progress1.getCurrentlyPlaying()), entity0.getWorld());
                                }
                            });
                        }
                        break;
                    }
                }
            }
        }
    }
}
