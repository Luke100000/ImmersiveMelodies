package immersive_melodies.client;

import immersive_melodies.Common;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MelodyProgressHandler {
    public static final MelodyProgressHandler INSTANCE = new MelodyProgressHandler();

    Map<Integer, MelodyProgress> progress = new HashMap<>();

    public MelodyProgress getProgress(Entity entity) {
        return progress.computeIfAbsent(entity.getId(), a -> new MelodyProgress());
    }

    public long getProgress(Entity entity, ItemStack stack) {
        // reset progress on change
        String identifier = stack.getOrCreateNbt().getString("melody");
        long startTime = stack.getOrCreateNbt().getLong("start_time");

        MelodyProgress progress = getProgress(entity);

        // reset if the melody changed
        if (!progress.currentlyPlaying.equals(identifier)) {
            progress.currentlyPlaying = identifier;
            progress.worldTime = startTime;
            progress.startTime = Common.timer.getTime();
            progress.lastIndex = 0;
        }

        // reset when the start time appears to be off
        if (progress.worldTime != startTime) {
            progress.worldTime = startTime;
            progress.startTime = Common.timer.getTime();
            progress.lastIndex = 0;
        }

        // todo sync with other players here, use entity id as a universal priority comparator

        return Common.timer.getTime() - progress.startTime;
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

    public static class MelodyProgress {
        long startTime;
        String currentlyPlaying = "";
        long worldTime;
        int lastIndex;

        int lastNoteTime;
        float lastVolume;
        float lastPitch;
        long lastLength;

        float current;
        float currentVolume;
        float currentPitch;

        float lastAnimationTime;

        float attackTime = 5.0f;
        float decayTime = 15.0f;

        public long getStartTime() {
            return startTime;
        }

        public String getCurrentlyPlaying() {
            return currentlyPlaying;
        }

        public long getWorldTime() {
            return worldTime;
        }

        public int getLastIndex() {
            return lastIndex;
        }

        public int getLastNoteTime() {
            return lastNoteTime;
        }

        public float getCurrent() {
            return current;
        }

        public float getCurrentVolume() {
            return currentVolume;
        }

        public float getCurrentPitch() {
            return currentPitch;
        }

        public void tick(float time) {
            boolean decayPhase = time - lastNoteTime > attackTime;
            float delta = Math.max(0.0f, Math.min(1.0f, (time - lastAnimationTime) / (decayPhase ? decayTime : attackTime)));
            lastAnimationTime = time;

            if (delta > 0) {
                current = current * (1.0f - delta) + (decayPhase ? 0.0f : 1.0f) * delta;
                currentPitch = currentPitch * (1.0f - delta) + lastPitch * delta;
                currentVolume = currentVolume * (1.0f - delta) + lastVolume * delta;
            }
        }
    }
}
