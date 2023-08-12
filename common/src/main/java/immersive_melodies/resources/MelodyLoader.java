package immersive_melodies.resources;

import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import immersive_melodies.util.MidiParser;
import immersive_melodies.util.Utils;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class MelodyLoader extends SinglePreparationResourceReloader<Map<Identifier, Melody>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    final String dataType = "melodies";

    @Override
    protected Map<Identifier, Melody> prepare(ResourceManager manager, Profiler profiler) {
        Map<Identifier, Melody> map = Maps.newHashMap();

        Map<Identifier, Resource> resources = manager.findResources(dataType, path -> path.getPath().endsWith(".midi") || path.getPath().endsWith(".mid"));
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            try {
                InputStream inputStream = entry.getValue().getInputStream();
                Melody melody = MidiParser.parseMidi(inputStream, Utils.toTitle(entry.getKey().getPath()));
                map.put(entry.getKey(), melody);
            } catch (IllegalArgumentException | IOException | JsonParseException exception) {
                LOGGER.error("Couldn't load melody {} ({})", entry.getKey(), exception);
            }
        }

        return map;
    }

    @Override
    protected void apply(Map<Identifier, Melody> prepared, ResourceManager manager, Profiler profiler) {
        ServerMelodyManager.setDatapackMelodies(prepared);
    }
}
