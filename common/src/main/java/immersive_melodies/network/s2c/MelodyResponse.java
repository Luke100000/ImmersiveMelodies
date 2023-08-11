package immersive_melodies.network.s2c;

import immersive_melodies.cobalt.network.Message;
import immersive_melodies.resources.ClientMelodyManager;
import immersive_melodies.resources.Melody;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class MelodyResponse extends Message {
    private final Identifier identifier;
    private final Melody melody;

    public MelodyResponse(Identifier identifier, Melody melody) {
        this.identifier = identifier;
        this.melody = melody;
    }

    public MelodyResponse(PacketByteBuf b) {
        this.identifier = b.readIdentifier();
        this.melody = Melody.fromNbt(b.readNbt());
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeIdentifier(identifier);
        b.writeNbt(melody.toNbt());
    }

    @Override
    public void receive(PlayerEntity e) {
        ClientMelodyManager.setMelody(identifier, melody);
    }
}
