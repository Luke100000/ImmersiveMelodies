package immersive_melodies.client;

import net.minecraft.item.ItemStack;

public class MelodyProgress {
    long lastTime;
    long time;

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

    float attackTime = 10.0f;
    float decayTime = 15.0f;

    public void tick(ItemStack stack) {
        long l = System.currentTimeMillis();
        long delta = l - lastTime;
        if (delta < 150) {
            time += delta;
        }
        lastTime = l;

        // reset progress on change
        String identifier = stack.getOrCreateNbt().getString("melody");
        long startTime = stack.getOrCreateNbt().getLong("start_time");

        // reset if the melody changed
        if (!currentlyPlaying.equals(identifier)) {
            currentlyPlaying = identifier;
            worldTime = startTime;
            time = 0;
            lastIndex = 0;
        }

        // reset when the start time appears to be off
        if (worldTime != startTime) {
            worldTime = startTime;
            time = 0;
            lastIndex = 0;
        }

        // todo sync with other players here, use entity id as a universal priority comparator
    }

    public long getTime() {
        return time;
    }

    public int getLastIndex() {
        return lastIndex;
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

    public void visualTick(float time) {
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
