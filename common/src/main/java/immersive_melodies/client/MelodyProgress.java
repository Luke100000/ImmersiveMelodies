package immersive_melodies.client;

import immersive_melodies.resources.ClientMelodyManager;
import immersive_melodies.resources.Melody;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class MelodyProgress {
    long lastTime;
    long time;

    String currentlyPlaying = "";
    String overwritten = null;
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
            overwritten = null;
            worldTime = startTime;
            time = 0;
            lastIndex = 0;
        }

        // reset when the start time appears to be off
        if (worldTime != startTime) {
            worldTime = startTime;
            overwritten = null;
            time = 0;
            lastIndex = 0;
        }
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

    public String getCurrentlyPlaying() {
        return overwritten == null ? currentlyPlaying : overwritten;
    }

    public void overwrite(String by) {
        overwritten = by;
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

    public boolean isPlaying() {
        return (System.currentTimeMillis() - lastTime) < 1000;
    }

    public Melody getMelody() {
        return ClientMelodyManager.getMelody(new Identifier(getCurrentlyPlaying()));
    }
}
