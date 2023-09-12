package immersive_melodies.resources;

import net.minecraft.network.PacketByteBuf;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


public class Melody extends MelodyDescriptor {
    private final List<Track> tracks = new LinkedList<>();
    private Track primaryTrack;

    public Melody() {
        super("unknown");
        addTrack(new Track("unknown", new LinkedList<>()));
    }

    public Melody(String name) {
        super(name);
    }

    public Melody(PacketByteBuf b) {
        super(b);

        int trackCount = b.readInt();
        for (int i = 0; i < trackCount; i++) {
            tracks.add(new Track(b));
        }
    }

    public List<Track> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    public void addTrack(Track track) {
        tracks.add(track);
        primaryTrack = null;
    }

    public Track getPrimaryTrack() {
        if (primaryTrack == null) {
            primaryTrack = getTracks().stream().max(Comparator.comparingInt(m -> (int) (m.getNotes().size() * m.getNotes().stream().mapToInt(Note::getNote).distinct().count()))).orElse(null);
        }
        return primaryTrack;
    }

    public void encode(PacketByteBuf b) {
        super.encodeLite(b);

        b.writeInt(tracks.size());
        for (Track note : tracks) {
            note.encode(b);
        }
    }
}
