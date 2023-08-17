package immersive_melodies.fabric;

import immersive_melodies.Client;
import immersive_melodies.Common;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class ClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(event -> Client.postLoad());
        ClientTickEvents.START_CLIENT_TICK.register(event -> Common.timer.getTime());
    }
}
