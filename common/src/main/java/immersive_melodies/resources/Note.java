package immersive_melodies.resources;

import net.minecraft.network.PacketByteBuf;

public class Note {
    private final int note;
    private final int velocity;
    private final int time;
    private final int length;
    private final int sustain;

    public Note(int note, int velocity, int time, int length, int sustain) {
        this.note = note;
        this.velocity = velocity;
        this.time = time;
        this.length = length;
        this.sustain = sustain;
    }

    public Note(PacketByteBuf b) {
        this.note = b.readByte() & 0xFF;
        this.velocity = b.readByte() & 0xFF;
        this.time = b.readInt();
        this.length = b.readInt();
        this.sustain = b.readInt();
    }

    public int getNote() {
        return note;
    }

    public int getVelocity() {
        return velocity;
    }

    public int getTime() {
        return time;
    }

    public int getLength() {
        return length;
    }

    public int getSustain() {
        return sustain;
    }

    public void encode(PacketByteBuf b) {
        b.writeByte(note);
        b.writeByte(velocity);
        b.writeInt(time);
        b.writeInt(length);
        b.writeInt(sustain);
    }

    public static class Builder {
        public final int note;
        public final int velocity;
        public final int time;
        public int sustain = 9999;
        public int length;

        public Builder(int note, int velocity, int time) {
            this.note = note;
            this.velocity = velocity;
            this.time = time;
        }

        public Note build() {
            return new Note(
                    note,
                    velocity,
                    time,
                    length,
                    sustain
            );
        }
    }
}