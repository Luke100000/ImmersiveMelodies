package immersive_melodies.fabric.resources;

import immersive_melodies.Common;
import immersive_melodies.resources.MelodyLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;

public class FabricMelody extends MelodyLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Common.locate("melody");

    @Override
    public Identifier getFabricId() {
        return ID;
    }
}
