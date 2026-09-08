package immersive_melodies.network.c2s;

import immersive_melodies.Common;
import immersive_melodies.util.Utils;
import immersive_melodies.item.InstrumentItem;
import immersive_melodies.network.ImmersivePayload;
import immersive_melodies.resources.ServerMelodyManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public record TrackToggleMessage(Identifier melody, int track, boolean enabled) implements ImmersivePayload {
    public static final Type<TrackToggleMessage> TYPE = new CustomPacketPayload.Type<>(Common.locate("track_toggle_message"));
    public static final StreamCodec<FriendlyByteBuf, TrackToggleMessage> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, TrackToggleMessage::melody,
            ByteBufCodecs.INT, TrackToggleMessage::track,
            ByteBufCodecs.BOOL, TrackToggleMessage::enabled,
            TrackToggleMessage::new
    );

    @Override
    public void handle(Player e) {
        Utils.getHandItems(e).forEach(stack -> {
            if (stack.getItem() instanceof InstrumentItem item) {
                ServerMelodyManager.MelodyTrackSettings settings = ServerMelodyManager.getSettings();
                String identifier = ServerMelodyManager.getIdentifier(e, item);
                if (enabled) {
                    settings.enableTrack(melody, identifier, track);
                } else {
                    settings.disableTrack(melody, identifier, track);
                }

                item.refreshTracks(stack, e);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
