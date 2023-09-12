package immersive_melodies.resources;

import net.minecraft.network.PacketByteBuf;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Track {
    private final List<Note> notes;
    private final String name;

    public Track() {
        this("unknown", new LinkedList<>());
    }

    public Track(String name, List<Note> notes) {
        this.name = name;
        this.notes = notes;
    }

    public Track(PacketByteBuf b) {
        name = b.readString();

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
        b.writeString(name);

        b.writeInt(notes.size());
        for (Note note : notes) {
            note.encode(b);
        }
    }
}
