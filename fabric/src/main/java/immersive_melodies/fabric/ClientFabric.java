package immersive_melodies.fabric;

import immersive_melodies.Client;
import immersive_melodies.MidiListener;
import immersive_melodies.network.Network;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Network.registerClientSender(ClientPlayNetworking::send);

        ClientLifecycleEvents.CLIENT_STARTED.register(event -> Client.postLoad());
        ClientLifecycleEvents.CLIENT_STOPPING.register(event -> MidiListener.stop());
    }
}
