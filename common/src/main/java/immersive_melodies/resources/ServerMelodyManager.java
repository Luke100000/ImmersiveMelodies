package immersive_melodies.resources;

import immersive_melodies.Common;
import io.netty.buffer.Unpooled;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ServerMelodyManager {
    static final Random RANDOM = new Random();

    public static MinecraftServer server;
    private static Map<ResourceLocation, MelodyLoader.LazyMelody> datapackMelodies = new HashMap<>();
    private static File directory = new File("data/melodies");

    public static void instantiate(ServerLevel world, LevelStorageSource.LevelStorageAccess session) {
        directory = session.getDimensionPath(world.dimension()).resolve("data/melodies").toFile();
    }

    private static File getFile(String id) {
        File file = new File(directory, id.replace(":", "/") + ".bin");
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        return file;
    }

    public static CustomServerMelodiesIndex getIndex() {
        //noinspection DataFlowIssue
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(CustomServerMelodiesIndex::new, CustomServerMelodiesIndex::fromNbt, null),
                "immersive_melodies");
    }

    public static MelodyTrackSettings getSettings() {
        //noinspection DataFlowIssue
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(MelodyTrackSettings::new, MelodyTrackSettings::fromNbt, null),
                "immersive_melodies_settings"
        );
    }

    public static Map<ResourceLocation, MelodyLoader.LazyMelody> getDatapackMelodies() {
        return datapackMelodies;
    }

    public static void setDatapackMelodies(Map<ResourceLocation, MelodyLoader.LazyMelody> datapackMelodies) {
        ServerMelodyManager.datapackMelodies = datapackMelodies;
    }

    public static ResourceLocation getRandomMelody() {
        Object[] datapack = getDatapackMelodies().keySet().toArray();
        Object[] custom = getIndex().melodies.keySet().toArray();
        if (datapack.length + custom.length == 0) {
            return Common.locate("missing");
        }
        int i = RANDOM.nextInt(datapack.length + custom.length);
        if (i < datapack.length) {
            return (ResourceLocation) datapack[i];
        } else {
            return (ResourceLocation) custom[i - datapack.length];
        }
    }

    /**
     * Chooses a matching datapack or uploaded melody, with each distinct ID equally likely.
     * Call on the server thread after the melody manager is initialized.
     *
     * @param random The random source used for selection.
     * @param filter A predicate for allowed melody IDs, such as a namespace filter.
     * @return Empty if no melody matches the filter.
     */
    public static Optional<ResourceLocation> getRandomMelody(RandomSource random, Predicate<ResourceLocation> filter) {
        Set<ResourceLocation> ids = new HashSet<>(getDatapackMelodies().keySet());
        ids.addAll(getIndex().getMelodies().keySet());
        List<ResourceLocation> melodies = ids.stream().filter(filter).toList();
        return melodies.isEmpty() ? Optional.empty() : Optional.of(melodies.get(random.nextInt(melodies.size())));
    }

    /**
     * Registers a melody to the server and saves it to disk.
     *
     * @param identifier The identifier of the melody to register.
     * @param melody     The melody to register.
     */
    public static void registerMelody(ResourceLocation identifier, Melody melody) {
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
    public static void deleteMelody(ResourceLocation identifier) {
        getIndex().getMelodies().remove(identifier);
        getIndex().setDirty(true);

        try {
            Files.delete(getFile(identifier.toString()).toPath());
        } catch (IOException e) {
            Common.LOGGER.error("Couldn't delete melody {} ({})", identifier, e);
        }
    }

    public static Melody getMelody(ResourceLocation identifier) {
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
        final Map<ResourceLocation, MelodyDescriptor> melodies = new HashMap<>();

        public static CustomServerMelodiesIndex fromNbt(CompoundTag nbt, HolderLookup.Provider provider) {
            CustomServerMelodiesIndex c = new CustomServerMelodiesIndex();
            for (String key : nbt.getAllKeys()) {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(nbt.getByteArray(key)));
                c.melodies.put(ResourceLocation.parse(key), MelodyDescriptor.STREAM_CODEC.decode(buffer));
            }
            return c;
        }

        @Override
        public CompoundTag save(CompoundTag c, HolderLookup.Provider provider) {
            for (Map.Entry<ResourceLocation, MelodyDescriptor> entry : melodies.entrySet()) {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                MelodyDescriptor.STREAM_CODEC.encode(buffer, entry.getValue());
                c.putByteArray(entry.getKey().toString(), buffer.array());
            }
            return c;
        }

        public Map<ResourceLocation, MelodyDescriptor> getMelodies() {
            return melodies;
        }
    }

    /**
     * Stores the settings for the melody tracks.
     */
    public static class MelodyTrackSettings extends SavedData {
        final Map<ResourceLocation, Map<String, Set<Integer>>> enabledTracks = new HashMap<>();

        public static MelodyTrackSettings fromNbt(CompoundTag nbt, HolderLookup.Provider provider) {
            MelodyTrackSettings c = new MelodyTrackSettings();
            for (String key : nbt.getAllKeys()) {
                CompoundTag map = nbt.getCompound(key);
                Map<String, Set<Integer>> m = new HashMap<>();
                for (String k : map.getAllKeys()) {
                    Set<Integer> tracks = Arrays.stream(map.getIntArray(k))
                            .boxed()
                            .collect(Collectors.toSet());
                    m.put(k, tracks);
                }
                c.enabledTracks.put(ResourceLocation.parse(key), m);
            }
            return c;
        }

        @Override
        public CompoundTag save(CompoundTag c, HolderLookup.Provider provider) {
            for (Map.Entry<ResourceLocation, Map<String, Set<Integer>>> entry : enabledTracks.entrySet()) {
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

        public void enableTrack(ResourceLocation melody, String identifier, int track) {
            enabledTracks.computeIfAbsent(melody, k -> new HashMap<>()).computeIfAbsent(identifier, k -> new HashSet<>()).add(track);
            setDirty(true);
        }

        public void disableTrack(ResourceLocation melody, String identifier, int track) {
            Map<String, Set<Integer>> uuidSetMap = enabledTracks.computeIfAbsent(melody, k -> new HashMap<>());
            uuidSetMap.computeIfAbsent(identifier, k -> new HashSet<>()).remove(track);
            setDirty(true);
        }

        public Set<Integer> getEnabledTracks(ResourceLocation name, String identifier) {
            Map<String, Set<Integer>> playerSettings = enabledTracks.getOrDefault(name, Collections.emptyMap());
            return playerSettings.getOrDefault(identifier, playerSettings.values().stream().findFirst().orElse(Set.of()));
        }
    }

    public static String getIdentifier(Entity entity, Item item) {
        return getIdentifier(entity, BuiltInRegistries.ITEM.getKey(item));
    }

    public static String getIdentifier(Entity entity, ResourceLocation instrument) {
        // Here I use only the instrument
        // That means track lists are managed globally, which is a "security issue" but usually more convenient
        return instrument.toString();
    }
}
