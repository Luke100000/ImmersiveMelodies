package immersive_melodies.resources;

import immersive_melodies.Common;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.level.storage.LevelStorage;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ServerMelodyManager {
    static final Melody DEFAULT = new Melody();
    static final Random RANDOM = new Random();

    public static MinecraftServer server;
    private static Map<Identifier, Melody> datapackMelodies = new HashMap<>();
    private static File directory = new File("data/melodies");

    public static void instantiate(ServerWorld world, LevelStorage.Session session) {
        directory = session.getWorldDirectory(world.getRegistryKey()).resolve("data/melodies").toFile();
    }

    private static File getFile(String id) {
        File file = new File(directory, id + ".bin");
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        return file;
    }

    public static CustomServerMelodiesIndex getIndex() {
        return server.getOverworld().getPersistentStateManager().getOrCreate(CustomServerMelodiesIndex::fromNbt, CustomServerMelodiesIndex::new, "immersive_melodies");
    }

    public static Map<Identifier, Melody> getDatapackMelodies() {
        return datapackMelodies;
    }

    public static void setDatapackMelodies(Map<Identifier, Melody> datapackMelodies) {
        ServerMelodyManager.datapackMelodies = datapackMelodies;
    }

    public static Identifier getRandomMelody() {
        Object[] datapack = getDatapackMelodies().keySet().toArray();
        Object[] custom = getIndex().melodies.keySet().toArray();
        int i = RANDOM.nextInt(datapack.length + custom.length);
        if (i < datapack.length) {
            return (Identifier) datapack[i];
        } else {
            return (Identifier) custom[i - datapack.length];
        }
    }

    /**
     * Registers a melody to the server and saves it to disk.
     *
     * @param identifier The identifier of the melody to register.
     * @param melody     The melody to register.
     */
    public static void registerMelody(Identifier identifier, Melody melody) {
        getIndex().getMelodies().put(identifier, melody);
        getIndex().setDirty(true);

        try {
            PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
            melody.encode(buffer);

            // Write to disk
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(getFile(identifier.toString())));
            bos.write(buffer.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes a melody from the server.
     *
     * @param identifier The identifier of the melody to delete.
     */
    public static void deleteMelody(Identifier identifier) {
        getIndex().getMelodies().remove(identifier);
        getIndex().setDirty(true);

        try {
            Files.delete(getFile(identifier.toString()).toPath());
        } catch (IOException e) {
            Common.LOGGER.error("Couldn't delete melody {} ({})", identifier, e);
        }
    }

    public static Melody getMelody(Identifier identifier) {
        if (datapackMelodies.containsKey(identifier)) {
            return datapackMelodies.get(identifier);
        } else {
            Melody melody = DEFAULT;
            try {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(getFile(identifier.toString())));
                melody = new Melody(new PacketByteBuf(Unpooled.wrappedBuffer(bis.readAllBytes())));
            } catch (IOException e) {
                Common.LOGGER.error("Couldn't load melody {} ({})", identifier, e);
            }
            return melody;
        }
    }

    /**
     * The melody index, containing only important information about the melodies.
     */
    public static class CustomServerMelodiesIndex extends PersistentState {
        final Map<Identifier, MelodyDescriptor> melodies = new HashMap<>();

        public static CustomServerMelodiesIndex fromNbt(NbtCompound nbt) {
            CustomServerMelodiesIndex c = new CustomServerMelodiesIndex();
            for (String key : nbt.getKeys()) {
                PacketByteBuf buffer = new PacketByteBuf(Unpooled.wrappedBuffer(nbt.getByteArray(key)));
                c.melodies.put(new Identifier(key), new MelodyDescriptor(buffer));
            }
            return c;
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
            NbtCompound c = new NbtCompound();
            for (Map.Entry<Identifier, MelodyDescriptor> entry : melodies.entrySet()) {
                PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
                entry.getValue().encodeLite(buffer);
                c.putByteArray(entry.getKey().toString(), buffer.array());
            }
            return c;
        }

        public Map<Identifier, MelodyDescriptor> getMelodies() {
            return melodies;
        }
    }
}
