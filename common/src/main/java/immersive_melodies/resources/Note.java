package immersive_melodies.resources;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

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

    public Note(NbtCompound nbt) {
        this.note = nbt.getInt("note");
        this.velocity = nbt.getInt("velocity");
        this.time = nbt.getInt("time");
        this.length = nbt.getInt("length");
        this.sustain = nbt.getInt("sustain");
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

    public NbtElement toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("note", note);
        nbt.putInt("velocity", velocity);
        nbt.putInt("time", time);
        nbt.putInt("length", length);
        nbt.putInt("sustain", sustain);
        return nbt;
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