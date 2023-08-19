package immersive_melodies.client.animation.animators;

import immersive_melodies.client.MelodyProgress;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

public class BagpipeAnimator implements Animator {
    @Override
    public void setAngles(ModelPart left, ModelPart right, BipedEntityModel<?> model, LivingEntity entity, MelodyProgress progress, float time) {
        model.head.yaw = 0.0f;
        model.head.pitch = 0.25f;

        left.pitch = -0.4f;
        left.yaw = -0.5f;

        right.pitch = -0.75f;
        right.yaw = -0.15f;
        right.roll = 0.0f;
    }
}
