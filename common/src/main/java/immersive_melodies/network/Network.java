package immersive_melodies.network;


import immersive_melodies.network.c2s.*;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.network.s2c.MelodyResponse;
import immersive_melodies.network.s2c.NoteMessage;
import immersive_melodies.network.s2c.OpenGuiRequest;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

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
        sender.sendToPlayer(player, payload);
    }

    public static void register(Registrar c) {
        c.register(ItemActionMessage.TYPE, ItemActionMessage.STREAM_CODEC, true);
        c.register(MelodyDeleteRequest.TYPE, MelodyDeleteRequest.STREAM_CODEC, true);
        c.register(MelodyRequest.TYPE, MelodyRequest.STREAM_CODEC, true);
        c.register(NoteBroadcastRequest.TYPE, NoteBroadcastRequest.STREAM_CODEC, true);
        c.register(OpenMenuRequest.TYPE, OpenMenuRequest.STREAM_CODEC, true);
        c.register(TrackToggleMessage.TYPE, TrackToggleMessage.STREAM_CODEC, true);
        c.register(UploadMelodyRequest.TYPE, UploadMelodyRequest.STREAM_CODEC, true);

        c.register(MelodyListMessage.TYPE, MelodyListMessage.STREAM_CODEC, false);
        c.register(MelodyResponse.TYPE, MelodyResponse.STREAM_CODEC, false);
        c.register(NoteMessage.TYPE, NoteMessage.STREAM_CODEC, false);
        c.register(OpenGuiRequest.TYPE, OpenGuiRequest.STREAM_CODEC, false);
    }

    public interface Registrar {
        <T extends ImmersivePayload> void register(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, boolean isServer);
    }

    public interface Sender {
        void sendToPlayer(ServerPlayer player, ImmersivePayload payload);
    }

    public interface ClientSender {
        void sendToServer(ImmersivePayload payload);
    }
}
