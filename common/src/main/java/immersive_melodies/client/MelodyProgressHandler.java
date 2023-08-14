package immersive_melodies.client;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MelodyProgressHandler {
    Map<Integer, Long> progress = new HashMap<>();
    Map<Integer, String> currentlyPlaying = new HashMap<>();
    Map<Integer, Long> startTimes = new HashMap<>();
    Map<Integer, Integer> lastIndex = new HashMap<>();

    public long getAndAdvanceProgress(Entity entity, ItemStack stack) {
        // reset progress on change
        String identifier = stack.getOrCreateNbt().getString("melody");
        long startTime = stack.getOrCreateNbt().getLong("start_time");

        // reset if the melody changed
        if (!currentlyPlaying.containsKey(entity.getId()) || !currentlyPlaying.get(entity.getId()).equals(identifier)) {
            currentlyPlaying.put(entity.getId(), identifier);
            startTimes.put(entity.getId(), startTime);
            progress.put(entity.getId(), 0L);
            lastIndex.put(entity.getId(), 0);
        }

        // reset when the start time appears to be off
        if (startTimes.get(entity.getId()) != startTime) {
            startTimes.put(entity.getId(), startTime);
            progress.put(entity.getId(), 0L);
            lastIndex.put(entity.getId(), 0);
        }

        // advance
        long progress = this.progress.get(entity.getId()) + 50;
        this.progress.put(entity.getId(), progress);

        // todo sync with other players here, use entity id as a universal priority comparator

        return progress;
    }

    public int getLastIndex(Entity entity) {
        return lastIndex.getOrDefault(entity.getId(), 0);
    }

    public void setLastIndex(Entity entity, int index) {
        lastIndex.put(entity.getId(), index);
    }
}
