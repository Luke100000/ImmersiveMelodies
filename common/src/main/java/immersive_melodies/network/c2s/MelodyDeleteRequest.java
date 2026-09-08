package immersive_melodies.network.c2s;

import immersive_melodies.Common;
import immersive_melodies.network.ImmersivePayload;
import immersive_melodies.network.Network;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.resources.ServerMelodyManager;
import immersive_melodies.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record MelodyDeleteRequest(Identifier identifier) implements ImmersivePayload {
    public static final Type<MelodyDeleteRequest> TYPE = new CustomPacketPayload.Type<>(Common.locate("melody_delete_request"));
    public static final StreamCodec<FriendlyByteBuf, MelodyDeleteRequest> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, MelodyDeleteRequest::identifier,
            MelodyDeleteRequest::new
    );

    @Override
    public void handle(Player e) {
        if (Utils.canDelete(identifier, e)) {
            ServerMelodyManager.deleteMelody(identifier);

            Network.sendToPlayer(new MelodyListMessage(e), (ServerPlayer) e);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
