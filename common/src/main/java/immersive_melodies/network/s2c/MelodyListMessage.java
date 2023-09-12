package immersive_melodies.network.s2c;

import immersive_melodies.Common;
import immersive_melodies.Config;
import immersive_melodies.cobalt.network.Message;
import immersive_melodies.resources.MelodyDescriptor;
import immersive_melodies.resources.ServerMelodyManager;
import immersive_melodies.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class MelodyListMessage extends Message {
    private final Map<Identifier, MelodyDescriptor> melodies = new HashMap<>();

    public MelodyListMessage(PlayerEntity receiver) {
        //datapack melodies
        this.melodies.putAll(ServerMelodyManager.getDatapackMelodies());

        //custom melodies
        if (Config.getInstance().showOtherPlayersMelodies) {
            this.melodies.putAll(ServerMelodyManager.getIndex().getMelodies());
        } else {
            ServerMelodyManager.getIndex().getMelodies().forEach((id, desc) -> {
                if (Utils.ownsMelody(id, receiver)) {
                    this.melodies.put(id, desc);
                }
            });
        }
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeInt(melodies.size());
        for (Map.Entry<Identifier, MelodyDescriptor> entry : melodies.entrySet()) {
            b.writeIdentifier(entry.getKey());
            entry.getValue().encodeLite(b);
        }
    }

    public MelodyListMessage(PacketByteBuf b) {
        int size = b.readInt();
        for (int i = 0; i < size; i++) {
            melodies.put(b.readIdentifier(), new MelodyDescriptor(b));
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
