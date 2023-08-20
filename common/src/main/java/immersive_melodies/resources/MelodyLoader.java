package immersive_melodies.resources;

import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import immersive_melodies.util.MidiParser;
import immersive_melodies.util.Utils;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MelodyLoader extends SinglePreparationResourceReloader<Map<Identifier, Melody>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    final String dataType = "melodies";

    @Override
    protected Map<Identifier, Melody> prepare(ResourceManager manager, Profiler profiler) {
        Map<Identifier, Melody> map = Maps.newHashMap();

        Collection<Identifier> resources = manager.findResources(dataType, path -> path.endsWith(".midi") || path.endsWith(".mid"));
        for (Identifier entry : resources) {
            try {
                InputStream inputStream = manager.getResource(entry).getInputStream();
                List<Melody> melodies = MidiParser.parseMidi(inputStream, Utils.toTitle(Utils.removeLastPart(Utils.getLastPart(entry.getPath(), "/"), ".")), true);
                int i = 0;
                for (Melody melody : melodies) {
                    map.put(new Identifier(entry.getNamespace(), entry.getPath() + "_" + (i++)), melody);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException exception) {
                LOGGER.error("Couldn't load melody {} ({})", entry, exception);
            }
        }

        return map;
    }

    @Override
    protected void apply(Map<Identifier, Melody> prepared, ResourceManager manager, Profiler profiler) {
        ServerMelodyManager.setDatapackMelodies(prepared);
    }
}
