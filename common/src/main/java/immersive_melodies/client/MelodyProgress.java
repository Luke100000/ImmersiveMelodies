package immersive_melodies.client;

public class MelodyProgress {
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

    float attackTime = 10.0f;
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
