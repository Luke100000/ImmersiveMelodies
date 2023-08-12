package immersive_melodies.resources;

import net.minecraft.nbt.NbtCompound;

public class MelodyDescriptor {
    private final String name;
    private final int bpm;

    public MelodyDescriptor() {
        this("unknown", 120);
    }

    public MelodyDescriptor(String name, int bpm) {
        this.name = name;
        this.bpm = bpm;
    }

    public MelodyDescriptor(NbtCompound compound) {
        this.name = compound.getString("name");
        this.bpm = compound.getInt("bpm");
    }

    public String getName() {
        return name;
    }

    public int getBPM() {
        return bpm;
    }

    public NbtCompound toLiteNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", name);
        nbt.putInt("bpm", bpm);
        return nbt;
    }
}