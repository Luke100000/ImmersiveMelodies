package immersive_melodies.util;

import immersive_melodies.resources.Melody;
import immersive_melodies.resources.Note;

import javax.sound.midi.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MidiParser {
    public static Melody parseMidi(InputStream inputStream, String name) {
        int bpm = 120;
        List<Note> notes = new LinkedList<>();

        HashMap<Integer, Note.Builder> currentNotes = new HashMap<>();

        try {
            Sequence sequence = MidiSystem.getSequence(inputStream);

            // Iterate through tracks and MIDI events
            for (Track track : sequence.getTracks()) {
                for (int i = 0; i < track.size(); i++) {
                    MidiEvent event = track.get(i);
                    MidiMessage message = event.getMessage();

                    // Parse meta events
                    if (message instanceof MetaMessage metaMessage) {
                        byte[] data = metaMessage.getData();
                        int type = metaMessage.getType();
                        if (type == 0x03) {
                            String s = new String(data).strip();
                            if (s.length() > 0) {
                                name = s;
                            }
                        } else if (type == 0x51) {
                            int microsecondsPerBeat = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
                            bpm = Math.round(60000000.0f / microsecondsPerBeat);
                        }
                    }

                    // Parse note on/off events
                    if (message instanceof ShortMessage sm) {
                        int command = sm.getCommand();
                        if (command == ShortMessage.NOTE_ON) {
                            int note = sm.getData1();
                            int velocity = sm.getData2();
                            currentNotes.put(note, new Note.Builder(note, velocity, event.getTick()));
                        } else if (command == ShortMessage.NOTE_OFF) {
                            int note = sm.getData1();
                            Note.Builder noteBuilder = currentNotes.get(note);
                            noteBuilder.length = event.getTick() - noteBuilder.time;
                            notes.add(noteBuilder.build());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Melody(name, bpm, notes);
    }
}
