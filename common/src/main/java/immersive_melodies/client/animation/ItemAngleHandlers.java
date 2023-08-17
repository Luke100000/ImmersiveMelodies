package immersive_melodies.client.animation;

import immersive_melodies.Common;
import immersive_melodies.client.MelodyProgress;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ItemAngleHandlers {
    private static final Map<Identifier, ItemAngleHandler> handlers = new HashMap<>();

    public static void register(Identifier id, ItemAngleHandler handler) {
        handlers.put(id, handler);
    }

    public static ItemAngleHandler get(Identifier id) {
        return handlers.get(id);
    }

    static {
        register(Common.locate("bagpipe"), (ModelPart left, ModelPart right, BipedEntityModel<?> model, LivingEntity entity, MelodyProgress progress, float time) -> {

        });

        register(Common.locate("didgeridoo"), (ModelPart left, ModelPart right, BipedEntityModel<?> model, LivingEntity entity, MelodyProgress progress, float time) -> {

        });

        register(Common.locate("flute"), (ModelPart left, ModelPart right, BipedEntityModel<?> model, LivingEntity entity, MelodyProgress progress, float time) -> {

        });

        register(Common.locate("lute"), (ModelPart left, ModelPart right, BipedEntityModel<?> model, LivingEntity entity, MelodyProgress progress, float time) -> {

        });

        register(Common.locate("piano"), (ModelPart left, ModelPart right, BipedEntityModel<?> model, LivingEntity entity, MelodyProgress progress, float time) -> {

        });

        register(Common.locate("triangle"), (ModelPart left, ModelPart right, BipedEntityModel<?> model, LivingEntity entity, MelodyProgress progress, float time) -> {
            float delta = (float) Math.sin(progress.getCurrent() * Math.PI * 0.5);

            left.pitch = -1.1f + progress.getCurrentPitch() * 0.25f;
            left.yaw = delta * 0.6f;

            right.pitch = -1.6f;
            right.roll = (float) Math.cos(time * 0.25f) * 0.05f * progress.getCurrentVolume();
            right.yaw = -0.5f;
        });

        register(Common.locate("trumpet"), (ModelPart left, ModelPart right, BipedEntityModel<?> model, LivingEntity entity, MelodyProgress progress, float time) -> {

        });
    }
}
