package immersive_melodies.util;

import net.minecraft.client.MinecraftClient;

public class TimerImpl implements Timer {
    long time;
    long lastTime;
    final MinecraftClient client;

    public TimerImpl(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public long getTime() {
        long l = System.currentTimeMillis();
        if (!client.isPaused()) {
            time += (l - lastTime);
        }
        lastTime = l;
        return time;
    }
}
