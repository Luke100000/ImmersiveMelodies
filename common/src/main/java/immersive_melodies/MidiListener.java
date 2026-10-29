package immersive_melodies;

import net.minecraft.client.Minecraft;

import javax.sound.midi.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MidiListener {
    private static MidiListenerThread thread;

    public static synchronized void launch() {
        if (thread != null && thread.isAlive()) return;

        thread = new MidiListenerThread();
        thread.start();
    }

    public static synchronized void stop() {
        if (thread == null) return;

        // Providers may consume interrupts, so cancellation is tracked separately.
        thread.running = false;
        thread.interrupt();
        if (thread == Thread.currentThread()) return;

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        boolean interrupted = false;
        while (thread.isAlive()) {
            long remainingNanos = deadline - System.nanoTime();
            if (remainingNanos <= 0) break;

            try {
                TimeUnit.NANOSECONDS.timedJoin(thread, remainingNanos);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        if (thread.isAlive()) {
            Common.LOGGER.warn("MIDI listener did not stop within 5 seconds");
        }
    }

    private static final class MidiListenerThread extends Thread {
        private final Map<MidiDevice.Info, MidiConnection> connections = new HashMap<>();
        private volatile boolean running = true;

        MidiListenerThread() {
            super("ImmersiveMelodies-MIDI");
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (running && !isInterrupted()) {
                    try {
                        //noinspection BusyWait
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    if (!running) break;

                    try {
                        scanDevices();
                    } catch (Exception e) {
                        Common.LOGGER.error("Error initializing MIDI devices", e);
                    }
                }
            } finally {
                connections.values().forEach(MidiConnection::close);
                connections.clear();
            }
        }

        private void scanDevices() {
            MidiDevice.Info[] midiDevices = MidiSystem.getMidiDeviceInfo();
            Set<MidiDevice.Info> availableDevices = new HashSet<>(Arrays.asList(midiDevices));
            connections.entrySet().removeIf(entry -> {
                MidiConnection connection = entry.getValue();
                if (!availableDevices.contains(entry.getKey()) || !connection.device.isOpen() || connection.receiver.closed) {
                    connection.close();
                    return true;
                }

                return false;
            });

            for (MidiDevice.Info info : midiDevices) {
                if (!running || isInterrupted()) break;
                if (connections.containsKey(info)) continue;

                try {
                    MidiDevice device = MidiSystem.getMidiDevice(info);
                    if (device.getMaxTransmitters() == 0) continue;

                    connections.put(info, new MidiConnection(device));
                    Common.LOGGER.info("MIDI Device: {} - {}", info.getName(), info.getDescription());
                } catch (MidiUnavailableException e) {
                    Common.LOGGER.warn("MIDI Device unavailable: {}", info.getName(), e);
                }
            }
        }
    }

    private static final class MidiConnection implements AutoCloseable {
        private final MidiDevice device;
        private final boolean ownsDevice;
        private final MidiReceiver receiver = new MidiReceiver();
        private final Transmitter transmitter;

        MidiConnection(MidiDevice device) throws MidiUnavailableException {
            this.device = device;
            ownsDevice = !device.isOpen();

            try {
                if (ownsDevice) {
                    device.open();
                }

                transmitter = device.getTransmitter();
                transmitter.setReceiver(receiver);
            } catch (MidiUnavailableException | RuntimeException e) {
                close();
                throw e;
            }
        }

        @Override
        public void close() {
            receiver.close();
            try {
                // Release the device even if its transmitter fails to close.
                try {
                    if (transmitter != null) {
                        transmitter.close();
                    }
                } finally {
                    if (ownsDevice) {
                        device.close();
                    }
                }
            } catch (RuntimeException e) {
                Common.LOGGER.warn("Error closing MIDI device: {}", device.getDeviceInfo().getName(), e);
            }
        }
    }

    private static final class MidiReceiver implements Receiver {
        private final Set<Integer> sustainedNotes = new HashSet<>();
        private volatile boolean closed;
        private boolean sustain;

        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (closed || !(message instanceof ShortMessage shortMessage)) return;

            int command = shortMessage.getCommand();
            int data1 = shortMessage.getData1();
            int data2 = shortMessage.getData2();

            // Preserve accepted note-offs even if the device disconnects before execution.
            Minecraft.getInstance().execute(() -> handleMessage(command, data1, data2));
        }

        @Override
        public void close() {
            closed = true;
        }

        private void handleMessage(int command, int data1, int data2) {
            switch (command) {
                case ShortMessage.NOTE_ON -> {
                    if (data2 == 0) {
                        noteOff(data1);
                        return;
                    }

                    if (sustainedNotes.remove(data1)) {
                        Client.playNote(data1, 0);
                    }

                    Client.playNote(data1, data2);
                }
                case ShortMessage.NOTE_OFF -> {
                    noteOff(data1);
                }
                case ShortMessage.CONTROL_CHANGE -> {
                    if (data1 != 64) return;

                    sustain = data2 >= 64;
                    if (sustain) return;

                    for (int note : sustainedNotes) {
                        Client.playNote(note, 0);
                    }

                    sustainedNotes.clear();
                }
            }
        }

        private void noteOff(int note) {
            if (sustain) {
                sustainedNotes.add(note);
            } else {
                Client.playNote(note, 0);
            }
        }
    }
}
