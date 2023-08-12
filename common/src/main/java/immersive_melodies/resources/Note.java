package immersive_melodies.resources;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class Note {
    private final int note;
    private final int velocity;
    private final long time;
    private final long length;

    public Note(int note, int velocity, long time, long length) {
        this.note = note;
        this.velocity = velocity;
        this.time = time;
        this.length = length;
    }

    public Note(NbtCompound nbt) {
        this.note = nbt.getInt("note");
        this.velocity = nbt.getInt("velocity");
        this.time = nbt.getLong("time");
        this.length = nbt.getLong("length");
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

    public NbtElement toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("note", note);
        nbt.putInt("velocity", velocity);
        nbt.putLong("time", time);
        nbt.putLong("length", length);
        return nbt;
    }

    public static class Builder {
        public final int note;
        public final int velocity;
        public final long time;
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
                    length
            );
        }
    }
}