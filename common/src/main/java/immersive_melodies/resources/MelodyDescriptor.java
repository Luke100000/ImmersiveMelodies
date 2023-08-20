package immersive_melodies.resources;

import net.minecraft.network.PacketByteBuf;

public class MelodyDescriptor {
    private final String name;
    private final int bpm;

    public MelodyDescriptor() {
        this("unknown", 120);
    }

    public MelodyDescriptor(String name, int bpm) {
        this.name = name;
        this.bpm = bpm;
    }

    public MelodyDescriptor(PacketByteBuf b) {
        this.name = b.readString();
        this.bpm = b.readInt();
    }

    public String getName() {
        return name;
    }

    public int getBPM() {
        return bpm;
    }

    public void encodeLite(PacketByteBuf b) {
        b.writeString(name);
        b.writeInt(bpm);
    }
}