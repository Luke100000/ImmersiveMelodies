package immersive_melodies.resources;

import immersive_melodies.Common;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ServerMelodyManager {
    static final Random RANDOM = new Random();

    public static MinecraftServer server;
    private static Map<Identifier, MelodyLoader.LazyMelody> datapackMelodies = new HashMap<>();
    private static File directory = new File("data/melodies");
    private static final SavedDataType<CustomServerMelodiesIndex> INDEX_TYPE = new SavedDataType<>(
            Common.locate("index"), CustomServerMelodiesIndex::new,
            CompoundTag.CODEC.xmap(CustomServerMelodiesIndex::fromNbt, data -> data.save(new CompoundTag())), DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    private static final SavedDataType<MelodyTrackSettings> SETTINGS_TYPE = new SavedDataType<>(
            Common.locate("settings"), MelodyTrackSettings::new,
            CompoundTag.CODEC.xmap(MelodyTrackSettings::fromNbt, data -> data.save(new CompoundTag())), DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    public static void instantiate(ServerLevel world, LevelStorageSource.LevelStorageAccess session) {
        Path dataDirectory = session.getDimensionPath(world.dimension()).resolve("data");
        migrateSavedData(dataDirectory, "immersive_melodies.dat", "index.dat");
        migrateSavedData(dataDirectory, "immersive_melodies_settings.dat", "settings.dat");
        directory = dataDirectory.resolve("melodies").toFile();
    }

    private static void migrateSavedData(Path dataDirectory, String oldName, String newName) {
        Path oldPath = dataDirectory.resolve(oldName);
        Path newPath = dataDirectory.resolve(Common.MOD_ID).resolve(newName);
        if (!Files.exists(oldPath) || Files.exists(newPath)) {
            return;
        }
        try {
            Files.createDirectories(newPath.getParent());
            Files.move(oldPath, newPath);
        } catch (IOException e) {
            Common.LOGGER.error("Couldn't migrate saved melody data from {} to {}", oldPath, newPath, e);
        }
    }

    private static File getFile(String id) {
        File file = new File(directory, id.replace(":", "/") + ".bin");
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        return file;
    }

    public static CustomServerMelodiesIndex getIndex() {
        //noinspection DataFlowIssue
        return server.overworld().getDataStorage().computeIfAbsent(INDEX_TYPE);
    }

    public static MelodyTrackSettings getSettings() {
        //noinspection DataFlowIssue
        return server.overworld().getDataStorage().computeIfAbsent(SETTINGS_TYPE);
    }

    public static Map<Identifier, MelodyLoader.LazyMelody> getDatapackMelodies() {
        return datapackMelodies;
    }

    public static void setDatapackMelodies(Map<Identifier, MelodyLoader.LazyMelody> datapackMelodies) {
        ServerMelodyManager.datapackMelodies = datapackMelodies;
    }

    public static Identifier getRandomMelody() {
        Object[] datapack = getDatapackMelodies().keySet().toArray();
        Object[] custom = getIndex().melodies.keySet().toArray();
        if (datapack.length + custom.length == 0) {
            return Common.locate("missing");
        }
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
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            Melody.STREAM_CODEC.encode(buffer, melody);

            // Write to disk
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(getFile(identifier.toString())));
            bos.write(buffer.array());
            bos.close();
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
            return datapackMelodies.get(identifier).get();
        } else {
            Melody melody = Melody.DEFAULT;
            try {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(getFile(identifier.toString())));
                melody = Melody.STREAM_CODEC.decode(new FriendlyByteBuf(Unpooled.wrappedBuffer(bis.readAllBytes())));
            } catch (Exception e) {
                Common.LOGGER.error("Couldn't load melody {} ({})", identifier, e);
                deleteMelody(identifier);
            }
            return melody;
        }
    }

    /**
     * The melody index, containing only important information about the melodies.
     */
    public static class CustomServerMelodiesIndex extends SavedData {
        final Map<Identifier, MelodyDescriptor> melodies = new HashMap<>();

        public static CustomServerMelodiesIndex fromNbt(CompoundTag nbt) {
            CustomServerMelodiesIndex c = new CustomServerMelodiesIndex();
            for (String key : nbt.keySet()) {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(nbt.getByteArray(key).orElseThrow()));
                c.melodies.put(Identifier.parse(key), MelodyDescriptor.STREAM_CODEC.decode(buffer));
            }
            return c;
        }

        public CompoundTag save(CompoundTag c) {
            for (Map.Entry<Identifier, MelodyDescriptor> entry : melodies.entrySet()) {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                MelodyDescriptor.STREAM_CODEC.encode(buffer, entry.getValue());
                c.putByteArray(entry.getKey().toString(), buffer.array());
            }
            return c;
        }

        public Map<Identifier, MelodyDescriptor> getMelodies() {
            return melodies;
        }
    }

    /**
     * Stores the settings for the melody tracks.
     */
    public static class MelodyTrackSettings extends SavedData {
        final Map<Identifier, Map<String, Set<Integer>>> enabledTracks = new HashMap<>();

        public static MelodyTrackSettings fromNbt(CompoundTag nbt) {
            MelodyTrackSettings c = new MelodyTrackSettings();
            for (String key : nbt.keySet()) {
                CompoundTag map = nbt.getCompoundOrEmpty(key);
                Map<String, Set<Integer>> m = new HashMap<>();
                for (String k : map.keySet()) {
                    Set<Integer> tracks = Arrays.stream(map.getIntArray(k).orElseThrow())
                            .boxed()
                            .collect(Collectors.toSet());
                    m.put(k, tracks);
                }
                c.enabledTracks.put(Identifier.parse(key), m);
            }
            return c;
        }

        public CompoundTag save(CompoundTag c) {
            for (Map.Entry<Identifier, Map<String, Set<Integer>>> entry : enabledTracks.entrySet()) {
                CompoundTag map = new CompoundTag();
                for (Map.Entry<String, Set<Integer>> e : entry.getValue().entrySet()) {
                    map.putIntArray(
                            e.getKey(),
                            e.getValue().stream()
                                    .mapToInt(Integer::intValue)
                                    .sorted()
                                    .toArray()
                    );
                }
                c.put(entry.getKey().toString(), map);
            }
            return c;
        }

        public void enableTrack(Identifier melody, String identifier, int track) {
            enabledTracks.computeIfAbsent(melody, k -> new HashMap<>()).computeIfAbsent(identifier, k -> new HashSet<>()).add(track);
            setDirty(true);
        }

        public void disableTrack(Identifier melody, String identifier, int track) {
            Map<String, Set<Integer>> uuidSetMap = enabledTracks.computeIfAbsent(melody, k -> new HashMap<>());
            uuidSetMap.computeIfAbsent(identifier, k -> new HashSet<>()).remove(track);
            setDirty(true);
        }

        public Set<Integer> getEnabledTracks(Identifier name, String identifier) {
            Map<String, Set<Integer>> playerSettings = enabledTracks.getOrDefault(name, Collections.emptyMap());
            return playerSettings.getOrDefault(identifier, playerSettings.values().stream().findFirst().orElse(Set.of()));
        }
    }

    public static String getIdentifier(Entity entity, Item item) {
        return getIdentifier(entity, BuiltInRegistries.ITEM.getKey(item));
    }

    public static String getIdentifier(Entity entity, Identifier instrument) {
        // Here I use only the instrument
        // That means track lists are managed globally, which is a "security issue" but usually more convenient
        return instrument.toString();
    }
}
