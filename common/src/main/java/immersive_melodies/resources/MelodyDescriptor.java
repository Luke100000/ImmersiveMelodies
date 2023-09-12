package immersive_melodies.resources;

import net.minecraft.network.PacketByteBuf;

public class MelodyDescriptor {
    private final String name;

    public MelodyDescriptor(String name) {
        this.name = name;
    }

    public MelodyDescriptor(PacketByteBuf b) {
        this.name = b.readString();
    }

    public String getName() {
        return name;
    }

    public void encodeLite(PacketByteBuf b) {
        b.writeString(name);
    }
}