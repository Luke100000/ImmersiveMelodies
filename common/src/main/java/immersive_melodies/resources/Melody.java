package immersive_melodies.resources;

import net.minecraft.network.PacketByteBuf;

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

    public Melody(PacketByteBuf b) {
        super(b);

        int noteCount = b.readInt();
        notes = new LinkedList<>();
        for (int i = 0; i < noteCount; i++) {
            notes.add(new Note(b));
        }
    }

    public List<Note> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public void encode(PacketByteBuf b) {
        super.encodeLite(b);

        b.writeInt(notes.size());
        for (Note note : notes) {
            note.encode(b);
        }
    }
}
