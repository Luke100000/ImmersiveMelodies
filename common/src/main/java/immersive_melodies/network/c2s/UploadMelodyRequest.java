package immersive_melodies.network.c2s;

import immersive_melodies.Common;
import immersive_melodies.cobalt.network.Message;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.ServerMelodyManager;
import immersive_melodies.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class UploadMelodyRequest extends Message {
    private final String name;
    private final Melody melody;

    public UploadMelodyRequest(String name, Melody melody) {
        this.name = name;
        this.melody = melody;
    }

    public UploadMelodyRequest(PacketByteBuf b) {
        this.name = b.readString();
        this.melody = new Melody(b.readNbt());
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeString(name);
        b.writeNbt(melody.toNbt());
    }

    @Override
    public void receive(PlayerEntity e) {
        String id = Utils.escapeString(e.getGameProfile().getName()) + "/" + Utils.escapeString(name);
        Identifier identifier = Common.locate(id);

        ServerMelodyManager.registerMelody(
                identifier,
                melody
        );
    }
}
