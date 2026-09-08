package immersive_melodies.network.s2c;

import immersive_melodies.Common;
import immersive_melodies.Config;
import immersive_melodies.network.ImmersivePayload;
import immersive_melodies.resources.MelodyDescriptor;
import immersive_melodies.resources.MelodyLoader;
import immersive_melodies.resources.ServerMelodyManager;
import immersive_melodies.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public record MelodyListMessage(Map<Identifier, MelodyDescriptor> melodies) implements ImmersivePayload {
    public static final Type<MelodyListMessage> TYPE = new CustomPacketPayload.Type<>(Common.locate("melody_list_message"));
    public static final StreamCodec<FriendlyByteBuf, MelodyListMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, MelodyDescriptor.STREAM_CODEC), MelodyListMessage::melodies,
            MelodyListMessage::new
    );

    public MelodyListMessage(Player receiver) {
        this(createMelodiesMap(receiver));
    }

    private static Map<Identifier, MelodyDescriptor> createMelodiesMap(Player receiver) {
        Map<Identifier, MelodyDescriptor> melodies = new HashMap<>();
        //datapack melodies
        for (Map.Entry<Identifier, MelodyLoader.LazyMelody> lazyMelodyEntry : ServerMelodyManager.getDatapackMelodies().entrySet()) {
            melodies.put(lazyMelodyEntry.getKey(), lazyMelodyEntry.getValue().getDescriptor());
        }

        //custom melodies
        if (Config.getInstance().showOtherPlayersMelodies) {
            melodies.putAll(ServerMelodyManager.getIndex().getMelodies());
        } else {
            ServerMelodyManager.getIndex().getMelodies().forEach((id, desc) -> {
                if (Utils.ownsMelody(id, receiver)) {
                    melodies.put(id, desc);
                }
            });
        }
        return melodies;
    }

    @Override
    public void handle(Player e) {
        Common.networkManager.handleMelodyListMessage(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
