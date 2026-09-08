package immersive_melodies.client;

import immersive_melodies.Common;
import immersive_melodies.item.InstrumentItem;
import immersive_melodies.resources.ClientMelodyManager;
import immersive_melodies.resources.Melody;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MelodyProgress {
    long time;
    long loop = -1;

    Identifier currentlyPlaying = Common.locate("");
    Identifier overwritten = null;
    long overwrittenWorldTime;
    long worldTime;
    final Map<Integer, Integer> lastIndex = new HashMap<>();

    long lastNoteLongTime;
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

    public void tick(ItemStack stack, long gameTime) {
        // reset progress on change
        Identifier identifier = InstrumentItem.getMelody(stack);
        long startTime = stack.getOrDefault(InstrumentItem.START_TIME, 0L);

        // reset if the melody changed
        if (!currentlyPlaying.equals(identifier)) {
            currentlyPlaying = identifier;
            overwritten = null;
            worldTime = startTime;
            time = 0;
            loop = -1;
            lastIndex.clear();
        }

        // reset when the start time appears to be off
        if (worldTime != startTime) {
            worldTime = startTime;
            overwritten = null;
            time = 0;
            loop = -1;
            lastIndex.clear();
        }

        updateTime(gameTime);
    }

    private void updateTime(long gameTime) {
        // Derive playback from the server-synchronized start time instead of local wall-clock time.
        long elapsed = Math.max(0L, gameTime - getStartTime()) * 50L;
        int melodyLength = getMelody().getLength();
        if (melodyLength > 0) {
            long currentLoop = elapsed / melodyLength;
            if (loop != currentLoop) {
                loop = currentLoop;
                lastIndex.clear();
            }
            time = elapsed % melodyLength;
        } else {
            time = elapsed;
        }
    }

    long getStartTime() {
        return overwritten == null ? worldTime : overwrittenWorldTime;
    }

    public long getTime() {
        return time;
    }

    public int getLastIndex(int track) {
        return lastIndex.getOrDefault(track, 0);
    }

    public void setLastIndex(int track, int index) {
        lastIndex.put(track, index);
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

    public Identifier getCurrentlyPlaying() {
        return overwritten == null ? currentlyPlaying : overwritten;
    }

    public void overwrite(Identifier by, long startTime, long gameTime) {
        if (overwritten == null || !overwritten.equals(by) || overwrittenWorldTime != startTime) {
            overwritten = by;
            overwrittenWorldTime = startTime;
            loop = -1;
            lastIndex.clear();
        }
        updateTime(gameTime);
    }

    public void visualTick(float time) {
        boolean decayPhase = time - lastNoteTime > attackTime;
        float delta = Math.clamp((time - lastAnimationTime) / (decayPhase ? decayTime : attackTime), 0.0f, 1.0f);
        lastAnimationTime = time;

        if (delta > 0) {
            current = current * (1.0f - delta) + (decayPhase ? 0.0f : 1.0f) * delta;
            currentPitch = currentPitch * (1.0f - delta) + lastPitch * delta;
            currentVolume = currentVolume * (1.0f - delta) + lastVolume * delta;
        }
    }

    public boolean isPlaying() {
        return delta() < 1000;
    }

    public long delta() {
        return System.currentTimeMillis() - lastNoteLongTime;
    }

    public Melody getMelody() {
        return ClientMelodyManager.getMelody(getCurrentlyPlaying());
    }
}
