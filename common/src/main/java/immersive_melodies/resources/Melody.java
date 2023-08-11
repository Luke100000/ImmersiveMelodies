package immersive_melodies.resources;

import net.minecraft.nbt.NbtCompound;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Melody {
    private final String name;
    private final int bpm;
    private final List<Note> notes;

    public Melody() {
        this("unknown", 120, new LinkedList<>());
    }

    public Melody(String name, int bpm, List<Note> notes) {
        this.name = name;
        this.bpm = bpm;
        this.notes = notes;
    }

    public String getName() {
        return name;
    }

    public int getBPM() {
        return bpm;
    }

    public List<Note> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public static Melody fromNbt(NbtCompound compound) {
        // todo
        return new Melody();
    }

    public NbtCompound toNbt() {
        // todo
        return new NbtCompound();
    }
}
