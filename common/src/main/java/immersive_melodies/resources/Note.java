package immersive_melodies.resources;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class Note {
    private final int note;
    private final int velocity;
    private final long time;
    private final long length;
    private final long sustain;

    public Note(int note, int velocity, long time, long length, long sustain) {
        this.note = note;
        this.velocity = velocity;
        this.time = time;
        this.length = length;
        this.sustain = sustain;
    }

    public Note(NbtCompound nbt) {
        this.note = nbt.getInt("note");
        this.velocity = nbt.getInt("velocity");
        this.time = nbt.getLong("time");
        this.length = nbt.getLong("length");
        this.sustain = nbt.getLong("sustain");
    }

    public int getNote() {
        return note;
    }

    public int getVelocity() {
        return velocity;
    }

    public long getTime() {
        return time;
    }

    public long getLength() {
        return length;
    }

    public long getSustain() {
        return sustain;
    }

    public NbtElement toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("note", note);
        nbt.putInt("velocity", velocity);
        nbt.putLong("time", time);
        nbt.putLong("length", length);
        nbt.putLong("sustain", sustain);
        return nbt;
    }

    public static class Builder {
        public final int note;
        public final int velocity;
        public final long time;
        public long sustain = 9999;
        public long length;

        public Builder(int note, int velocity, long time) {
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