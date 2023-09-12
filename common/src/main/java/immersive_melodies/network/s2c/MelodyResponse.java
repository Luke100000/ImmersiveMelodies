package immersive_melodies.network.s2c;

import immersive_melodies.network.FragmentedMessage;
import immersive_melodies.resources.ClientMelodyManager;
import immersive_melodies.resources.Melody;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class MelodyResponse extends FragmentedMessage {
    public MelodyResponse(PacketByteBuf b) {
        super(b);
    }

    public MelodyResponse(Identifier identifier, byte[] fragment, int length) {
        super(identifier.toString(), fragment, length);
    }

    @Override
    protected void finish(PlayerEntity e, String name, Melody melody) {
        ClientMelodyManager.setMelody(new Identifier(name), melody);
    }
}
