package immersive_melodies.client;

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

    public long getAndAdvanceProgress(Entity entity, ItemStack stack) {
        // reset progress on change
        String identifier = stack.getOrCreateNbt().getString("melody");
        long startTime = stack.getOrCreateNbt().getLong("start_time");

        MelodyProgress progress = getProgress(entity);

        // reset if the melody changed
        if (!progress.currentlyPlaying.equals(identifier)) {
            progress.currentlyPlaying = identifier;
            progress.startTime = startTime;
            progress.progress = 0;
            progress.lastIndex = 0;
        }

        // reset when the start time appears to be off
        if (progress.startTime != startTime) {
            progress.startTime = startTime;
            progress.progress = 0;
            progress.lastIndex = 0;
        }

        // advance
        progress.progress += 50;

        // todo sync with other players here, use entity id as a universal priority comparator

        return progress.progress;
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
        long progress;
        String currentlyPlaying = "";
        long startTime;
        int lastIndex;

        int lastNoteTime;
        float lastVolume;
        float lastPitch;
        long lastLength;

        float current;
        float currentVolume;
        float currentPitch;

        float lastTime;

        float attackTime = 5.0f;
        float decayTime = 15.0f;

        public long getProgress() {
            return progress;
        }

        public String getCurrentlyPlaying() {
            return currentlyPlaying;
        }

        public long getStartTime() {
            return startTime;
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
            float delta = Math.max(0.0f, Math.min(1.0f, (time - lastTime) / (decayPhase ? decayTime : attackTime)));
            lastTime = time;

            if (delta > 0) {
                current = current * (1.0f - delta) + (decayPhase ? 0.0f : 1.0f) * delta;
                currentPitch = currentPitch * (1.0f - delta) + lastPitch * delta;
                currentVolume = currentVolume * (1.0f - delta) + lastVolume * delta;
            }
        }
    }
}
