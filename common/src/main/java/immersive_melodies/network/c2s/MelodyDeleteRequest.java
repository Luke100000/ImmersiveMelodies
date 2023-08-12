package immersive_melodies.network.c2s;

import immersive_melodies.cobalt.network.Message;
import immersive_melodies.resources.ServerMelodyManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class MelodyDeleteRequest extends Message {
    private final Identifier identifier;

    public MelodyDeleteRequest(Identifier identifier) {
        this.identifier = identifier;
    }

    public MelodyDeleteRequest(PacketByteBuf b) {
        this.identifier = b.readIdentifier();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeIdentifier(identifier);
    }

    @Override
    public void receive(PlayerEntity e) {
        // todo security
        ServerMelodyManager.deleteMelody(identifier);
    }
}
