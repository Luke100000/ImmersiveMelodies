package immersive_melodies.resources;

public class Note {
    private final int note;
    private final int velocity;
    private final long time;
    private final long length;

    public Note(int note, int velocity, long time, long length) {
        this.note = note;
        this.velocity = velocity;
        this.time = time;
        this.length = length;
    }

    public int getNote() {
        return note;
    }

    public int getVelocity() {
        return velocity;
    }

    public long getTime() {
        return time;
    }

    public long getLength() {
        return length;
    }

    public static class Builder {
        public final int note;
        public final int velocity;
        public final long time;
        public long length;

        public Builder(int note, int velocity, long time) {
            this.note = note;
            this.velocity = velocity;
            this.time = time;
        }

        public Note build() {
            return new Note(
                    note,
                    velocity,
                    time,
                    length
            );
        }
    }
}