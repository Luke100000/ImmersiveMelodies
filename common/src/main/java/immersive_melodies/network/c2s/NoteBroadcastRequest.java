package immersive_melodies.network.c2s;

import immersive_melodies.Common;
import immersive_melodies.network.ImmersivePayload;
import immersive_melodies.network.Network;
import immersive_melodies.network.s2c.NoteMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record NoteBroadcastRequest(int tone, int velocity) implements ImmersivePayload {
    public static final Type<NoteBroadcastRequest> TYPE = new CustomPacketPayload.Type<>(Common.locate("note_broadcast_request"));
    public static final StreamCodec<FriendlyByteBuf, NoteBroadcastRequest> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, NoteBroadcastRequest::tone,
            ByteBufCodecs.INT, NoteBroadcastRequest::velocity,
            NoteBroadcastRequest::new
    );

    @Override
    public void handle(Player e) {
        if (e instanceof ServerPlayer se) {
            se.level().players().stream()
                    .filter(player -> player != e && player.distanceTo(e) < 64)
                    .forEach(player -> {
                        Network.sendToPlayer(new NoteMessage(e.getId(), tone, velocity), player);
                    });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}