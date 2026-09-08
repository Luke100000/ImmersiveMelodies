package immersive_melodies.resources;

import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import immersive_melodies.Config;
import immersive_melodies.util.MidiParser;
import immersive_melodies.util.Utils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class MelodyLoader extends SimplePreparableReloadListener<Map<Identifier, MelodyLoader.LazyMelody>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T> Supplier<T> memoize(Supplier<T> delegate) {
        AtomicReference<T> value = new AtomicReference<>();
        return () -> {
            T val = value.get();
            if (val == null) {
                val = value.updateAndGet(cur -> cur == null ? Objects.requireNonNull(delegate.get()) : cur);
            }
            return val;
        };
    }

    public static class LazyMelody {
        public final String name;
        public final Supplier<Melody> supplier;
        public final MelodyDescriptor descriptor;

        public LazyMelody(String name, Supplier<Melody> supplier) {
            this.name = name;
            this.supplier = memoize(supplier);
            this.descriptor = new MelodyDescriptor(name);
        }

        public Melody get() {
            return supplier.get();
        }

        public MelodyDescriptor getDescriptor() {
            return descriptor;
        }
    }

    @Override
    protected Map<Identifier, LazyMelody> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, LazyMelody> map = Maps.newHashMap();

        // Support both 1.21+ singular and 1.20.x plural resource locations
        Map<Identifier, Resource> resources = new HashMap<>();
        resources.putAll(manager.listResources("melodies", MelodyLoader::filter));
        resources.putAll(manager.listResources("melody", MelodyLoader::filter));

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            if (!Config.getInstance().loadInbuiltMidis && entry.getKey().getNamespace().equals("immersive_melodies")) {
                continue;
            }
            try {
                String name = Utils.toTitle(Utils.removeLastPart(Utils.getLastPart(entry.getKey().getPath(), "/"), "."));
                Identifier identifier = Identifier.fromNamespaceAndPath(entry.getKey().getNamespace(), entry.getKey().getPath());
                map.put(identifier, new LazyMelody(name, () -> {
                    try {
                        return MidiParser.parseMidi(entry.getValue().open(), name);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            } catch (IllegalArgumentException | JsonParseException exception) {
                LOGGER.error("Couldn't load melody {} ({})", entry.getKey(), exception);
            }
        }

        return map;
    }

    private static boolean filter(Identifier path) {
        return path.getPath().endsWith(".midi") || path.getPath().endsWith(".mid");
    }

    @Override
    protected void apply(Map<Identifier, LazyMelody> prepared, ResourceManager manager, ProfilerFiller profiler) {
        ServerMelodyManager.setDatapackMelodies(prepared);
    }
}
