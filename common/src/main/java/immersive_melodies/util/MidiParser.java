package immersive_melodies.util;

import immersive_melodies.resources.Melody;
import immersive_melodies.resources.Note;

import javax.sound.midi.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MidiParser {
    public static List<Melody> parseMidi(InputStream inputStream, String baseName, boolean appendTrackName) {
        List<Melody> melodies = new LinkedList<>();
        try {
            Sequence sequence = MidiSystem.getSequence(inputStream);

            String name = baseName;
            int bpm = 120;

            // Iterate through tracks and MIDI events
            int trackNr = 0;
            for (Track track : sequence.getTracks()) {
                boolean customName = false;
                List<Note> notes = new LinkedList<>();

                HashMap<Integer, Note.Builder> currentNotes = new HashMap<>();

                for (int i = 0; i < track.size(); i++) {
                    MidiEvent event = track.get(i);
                    MidiMessage message = event.getMessage();

                    // Parse meta events
                    if (message instanceof MetaMessage metaMessage) {
                        byte[] data = metaMessage.getData();
                        int type = metaMessage.getType();
                        if (type == 0x03) {
                            if (sequence.getTracks().length > 1 && appendTrackName) {
                                String s = new String(data).strip();
                                if (s.length() > 0) {
                                    name = String.format("%s (%s)", name, s);
                                    customName = true;
                                }
                            }
                        } else if (type == 0x51) {
                            int microsecondsPerBeat = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
                            bpm = Math.round(60000000.0f / microsecondsPerBeat);
                        }
                    }

                    // Parse note on/off events
                    if (message instanceof ShortMessage sm) {
                        int command = sm.getCommand();

                        // Convert notes into ms
                        long tick = event.getTick();
                        int ms = (int) (tick * 60 * 1000 / sequence.getResolution() / bpm);

                        if (command == ShortMessage.NOTE_ON) {
                            int note = sm.getData1();
                            int velocity = sm.getData2();

                            // We simulate the (minimum) sustain as the time between releasing the key and pressing it again
                            if (currentNotes.containsKey(note)) {
                                Note.Builder previousNote = currentNotes.get(note);
                                previousNote.sustain = ms - previousNote.time;
                            }

                            currentNotes.put(note, new Note.Builder(note, velocity, ms));
                        } else if (command == ShortMessage.NOTE_OFF) {
                            int note = sm.getData1();
                            Note.Builder noteBuilder = currentNotes.get(note);
                            noteBuilder.length = ms - noteBuilder.time;
                            notes.add(noteBuilder.build());
                        }
                    }
                }

                if (notes.size() > 0) {
                    if (sequence.getTracks().length > 1 && !customName && appendTrackName) {
                        trackNr += 1;
                        name += " Track " + trackNr;
                    }

                    // Just to make sure
                    notes.sort((a, b) -> (int) (a.getTime() - b.getTime()));

                    melodies.add(new Melody(name, bpm, notes));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return melodies;
    }
}
