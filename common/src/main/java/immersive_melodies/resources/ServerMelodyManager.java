package immersive_melodies.resources;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;

public class ServerMelodyManager {
    static final Melody DEFAULT = new Melody();

    public static MinecraftServer server;
    private static Map<Identifier, Melody> datapackMelodies = new HashMap<>();

    public static CustomServerMelodies get() {
        return server.getOverworld().getPersistentStateManager()
                .getOrCreate(CustomServerMelodies::fromNbt, CustomServerMelodies::new, "immersive_melodies");
    }

    public static Map<Identifier, Melody> getDatapackMelodies() {
        return datapackMelodies;
    }

    public static void setDatapackMelodies(Map<Identifier, Melody> datapackMelodies) {
        ServerMelodyManager.datapackMelodies = datapackMelodies;
    }

    public static void registerMelody(Identifier identifier, Melody melody) {
        get().getCustomServerMelodies().put(identifier, melody);
        get().setDirty(true);
    }

    public static void deleteMelody(Identifier identifier) {
        get().getCustomServerMelodies().remove(identifier);
        get().setDirty(true);
    }

    public static Melody getMelody(Identifier identifier) {
        if (datapackMelodies.containsKey(identifier)) {
            return datapackMelodies.get(identifier);
        } else {
            return get().customServerMelodies.getOrDefault(identifier, DEFAULT);
        }
    }

    public static class CustomServerMelodies extends PersistentState {
        final Map<Identifier, Melody> customServerMelodies = new HashMap<>();

        public static CustomServerMelodies fromNbt(NbtCompound nbt) {
            CustomServerMelodies c = new CustomServerMelodies();
            for (String key : nbt.getKeys()) {
                c.customServerMelodies.put(new Identifier(key), Melody.fromNbt(nbt.getCompound(key)));
            }
            return c;
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
            NbtCompound c = new NbtCompound();
            for (Map.Entry<Identifier, Melody> entry : customServerMelodies.entrySet()) {
                c.put(entry.getKey().toString(), entry.getValue().toNbt());
            }
            return c;
        }

        public Map<Identifier, Melody> getCustomServerMelodies() {
            return customServerMelodies;
        }
    }
}
