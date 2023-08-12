package immersive_melodies.resources;

import net.minecraft.nbt.NbtCompound;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;



public class Melody extends MelodyDescriptor {
    private final List<Note> notes;

    public Melody() {
        this("unknown", 120, new LinkedList<>());
    }

    public Melody(String name, int bpm, List<Note> notes) {
        super(name, bpm);
        this.notes = notes;
    }

    public Melody(NbtCompound compound) {
        super(compound);

        int noteCount = compound.getInt("noteCount");
        notes = new LinkedList<>();
        for (int i = 0; i < noteCount; i++) {
            notes.add(new Note(
                    compound.getCompound("note" + i)
            ));
        }
    }

    public List<Note> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = super.toLiteNbt();

        nbt.putInt("noteCount", notes.size());
        for (int i = 0; i < notes.size(); i++) {
            nbt.put("note" + i, notes.get(i).toNbt());
        }

        return nbt;
    }
}
