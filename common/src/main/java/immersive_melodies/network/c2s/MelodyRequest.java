package immersive_melodies.network.c2s;

import immersive_melodies.cobalt.network.Message;
import immersive_melodies.network.PacketSplitter;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.ServerMelodyManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MelodyRequest extends Message {
    private final Identifier identifier;

    public MelodyRequest(Identifier identifier) {
        this.identifier = identifier;
    }

    public MelodyRequest(PacketByteBuf b) {
        this.identifier = b.readIdentifier();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeIdentifier(identifier);
    }

    @Override
    public void receive(PlayerEntity e) {
        Melody melody = ServerMelodyManager.getMelody(identifier);
        PacketSplitter.sendToPlayer(identifier, melody, (ServerPlayerEntity) e);
    }
}
