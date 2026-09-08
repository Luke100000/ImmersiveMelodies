package immersive_melodies.network.c2s;

import immersive_melodies.Common;
import immersive_melodies.Config;
import immersive_melodies.network.FragmentedMessage;
import immersive_melodies.network.Network;
import immersive_melodies.network.PacketSplitter;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.ServerMelodyManager;
import immersive_melodies.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public record UploadMelodyRequest(String name, byte[] fragment, int length) implements FragmentedMessage {
    public static final Type<UploadMelodyRequest> TYPE = new CustomPacketPayload.Type<>(Common.locate("upload_melody_request"));
    public static final StreamCodec<FriendlyByteBuf, UploadMelodyRequest> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, UploadMelodyRequest::name,
            ByteBufCodecs.BYTE_ARRAY, UploadMelodyRequest::fragment,
            ByteBufCodecs.INT, UploadMelodyRequest::length,
            UploadMelodyRequest::new
    );

    @Override
    public void finish(Player e, String name, Melody melody) {
        if (!Utils.hasPermissions(e, Config.getInstance().uploadPermissionLevel)) {
            e.sendSystemMessage(Component.translatable("immersive_melodies.error.upload.no_permission"));
            return;
        }
        String id = Utils.getPlayerName(e) + "/" + UUID.randomUUID();
        Identifier identifier = Identifier.fromNamespaceAndPath("player", id);

        // Register
        ServerMelodyManager.registerMelody(
                identifier,
                melody
        );

        // Update the index
        Network.sendToPlayer(new MelodyListMessage(e), (ServerPlayer) e);

        // Send the melody to all players
        e.level().players().forEach(player -> {
            PacketSplitter.sendToPlayer(identifier, melody, (ServerPlayer) player);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
