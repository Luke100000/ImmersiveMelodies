package immersive_melodies.network;


import immersive_melodies.network.c2s.*;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.network.s2c.MelodyResponse;
import immersive_melodies.network.s2c.NoteMessage;
import immersive_melodies.network.s2c.OpenGuiRequest;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Function;

public class Network {
    private static Sender sender;
    private static ClientSender clientSender;

    public static void registerSender(Sender sender) {
        Network.sender = sender;
    }

    public static void registerClientSender(ClientSender clientSender) {
        Network.clientSender = clientSender;
    }

    public static void sendToServer(ImmersivePayload payload) {
        clientSender.sendToServer(payload);
    }

    public static void sendToPlayer(ImmersivePayload payload, ServerPlayer player) {
        sender.sendToPlayer(payload, player);
    }

    public static void sendToAllPlayers(MinecraftServer server, ImmersivePayload payload) {
        server.getPlayerList().getPlayers().forEach(p -> sendToPlayer(payload, p));
    }

    public static void register(Registrar c) {
        c.register(ItemActionMessage.class, ItemActionMessage::new);
        c.register(MelodyDeleteRequest.class, MelodyDeleteRequest::new);
        c.register(MelodyRequest.class, MelodyRequest::new);
        c.register(NoteBroadcastRequest.class, NoteBroadcastRequest::new);
        c.register(OpenMenuRequest.class, OpenMenuRequest::new);
        c.register(TrackToggleMessage.class, TrackToggleMessage::new);
        c.register(UploadMelodyRequest.class, UploadMelodyRequest::new);

        c.register(MelodyListMessage.class, MelodyListMessage::new);
        c.register(MelodyResponse.class, MelodyResponse::new);
        c.register(NoteMessage.class, NoteMessage::new);
        c.register(OpenGuiRequest.class, OpenGuiRequest::new);
    }

    public interface Registrar {
        <T extends ImmersivePayload> void register(Class<T> msg, Function<FriendlyByteBuf, T> constructor);
    }

    public interface Sender {
        void sendToPlayer(ImmersivePayload payload, ServerPlayer player);
    }

    public interface ClientSender {
        void sendToServer(ImmersivePayload payload);
    }
}
