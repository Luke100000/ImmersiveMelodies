package immersive_melodies.client.animation;

import immersive_melodies.client.MelodyProgress;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

public interface ItemAngleHandler {
    void setAngles(ModelPart left, ModelPart right, BipedEntityModel<?> model, LivingEntity entity, MelodyProgress progress, float time);
}
