package immersive_melodies.network.s2c;

import immersive_melodies.Common;
import immersive_melodies.cobalt.network.Message;
import immersive_melodies.resources.MelodyDescriptor;
import immersive_melodies.resources.ServerMelodyManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MelodyListMessage extends Message {
    private final Map<Identifier, MelodyDescriptor> melodies = new HashMap<>();

    public MelodyListMessage() {
        //datapack melodies
        this.melodies.putAll(ServerMelodyManager.getDatapackMelodies());

        //custom melodies
        this.melodies.putAll(ServerMelodyManager.get().getCustomServerMelodies());
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeInt(melodies.size());
        for (Map.Entry<Identifier, MelodyDescriptor> entry : melodies.entrySet()) {
            b.writeIdentifier(entry.getKey());
            b.writeNbt(entry.getValue().toLiteNbt());
        }
    }

    public MelodyListMessage(PacketByteBuf b) {
        int size = b.readInt();
        for (int i = 0; i < size; i++) {
            melodies.put(b.readIdentifier(), new MelodyDescriptor(Objects.requireNonNull(b.readNbt())));
        }
    }

    @Override
    public void receive(PlayerEntity e) {
        Common.networkManager.handleMelodyListMessage(this);
    }

    public Map<Identifier, MelodyDescriptor> getMelodies() {
        return melodies;
    }
}
