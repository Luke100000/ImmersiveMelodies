package immersive_melodies.network.c2s;

import immersive_melodies.Common;
import immersive_melodies.network.ImmersivePayload;
import immersive_melodies.network.PacketSplitter;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.ServerMelodyManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record MelodyRequest(Identifier identifier) implements ImmersivePayload {
    public static final Type<MelodyRequest> TYPE = new CustomPacketPayload.Type<>(Common.locate("melody_request"));
    public static final StreamCodec<FriendlyByteBuf, MelodyRequest> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, MelodyRequest::identifier,
            MelodyRequest::new
    );

    @Override
    public void handle(Player e) {
        Melody melody = ServerMelodyManager.getMelody(identifier);
        PacketSplitter.sendToPlayer(identifier, melody, (ServerPlayer) e);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
