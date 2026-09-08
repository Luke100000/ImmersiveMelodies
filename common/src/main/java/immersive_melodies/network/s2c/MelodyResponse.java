package immersive_melodies.network.s2c;

import immersive_melodies.Common;
import immersive_melodies.network.FragmentedMessage;
import immersive_melodies.resources.ClientMelodyManager;
import immersive_melodies.resources.Melody;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public record MelodyResponse(String name, byte[] fragment, int length) implements FragmentedMessage {
    public static final Type<MelodyResponse> TYPE = new CustomPacketPayload.Type<>(Common.locate("melody_response"));
    public static final StreamCodec<FriendlyByteBuf, MelodyResponse> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MelodyResponse::name,
            ByteBufCodecs.BYTE_ARRAY, MelodyResponse::fragment,
            ByteBufCodecs.INT, MelodyResponse::length,
            MelodyResponse::new
    );

    @Override
    public void finish(Player e, String name, Melody melody) {
        ClientMelodyManager.setMelody(Identifier.parse(name), melody);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
