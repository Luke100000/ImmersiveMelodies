package immersive_melodies.cobalt.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

import javax.annotation.Nullable;

public abstract class Message {
    protected Message() {

    }

    public abstract void encode(PacketByteBuf b);

    public abstract void receive(@Nullable PlayerEntity e);
}
