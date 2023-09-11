package immersive_melodies.util;

import immersive_melodies.resources.Melody;
import immersive_melodies.resources.Note;

import javax.sound.midi.*;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MidiParser {
    public static List<Melody> parseMidi(InputStream inputStream, String baseName, boolean appendTrackName) {
        List<Melody> melodies = new LinkedList<>();
        try {
            Sequence sequence = MidiSystem.getSequence(inputStream);

            String name = baseName;

            // Fetch shared events
            List<MidiEvent> sharedEvents = new LinkedList<>();
            for (Track track : sequence.getTracks()) {
                getEvents(track).stream()
                        .filter(event -> event.getMessage() instanceof MetaMessage m && m.getType() == 0x51)
                        .forEach(sharedEvents::add);
            }

            // Iterate through tracks and MIDI events
            int trackNr = 0;
            for (Track track : sequence.getTracks()) {
                // Merge with shared events and sort
                List<MidiEvent> events = getEvents(track);
                events.addAll(0, sharedEvents);
                events.sort((a, b) -> (int) (a.getTick() - b.getTick()));

                int bpm = 120;
                long lastTick = 0;
                double lastMs = 0;
                boolean customName = false;
                List<Note> notes = new LinkedList<>();
                HashMap<Integer, Note.Builder> currentNotes = new HashMap<>();

                for (MidiEvent event : events) {
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
                        int ms = (int) ((tick - lastTick) * 60 * 1000 / sequence.getResolution() / bpm + lastMs);
                        lastTick = tick;
                        lastMs = ms;

                        // Another way to decode note offs are note ons with velocity 0
                        if (command == ShortMessage.NOTE_ON && sm.getData2() == 0) {
                            command = ShortMessage.NOTE_OFF;
                        }

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
                            currentNotes.remove(note);
                            if (noteBuilder != null) {
                                noteBuilder.length = ms - noteBuilder.time;
                                notes.add(noteBuilder.build());
                            }
                        }
                    }
                }

                if (notes.size() > 0) {
                    if (sequence.getTracks().length > 1 && !customName && appendTrackName) {
                        trackNr += 1;
                        name += " Track " + trackNr;
                    }

                    // Sort
                    notes.sort(Comparator.comparingInt(Note::getTime));

                    melodies.add(new Melody(name, notes));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return melodies;
    }

    private static List<MidiEvent> getEvents(Track track) {
        List<MidiEvent> events = new LinkedList<>();
        for (int i = 0; i < track.size(); i++) {
            events.add(track.get(i));
        }
        return events;
    }
}
