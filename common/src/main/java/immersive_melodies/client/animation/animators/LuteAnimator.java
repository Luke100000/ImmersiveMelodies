package immersive_melodies.client.animation.animators;

import immersive_melodies.client.MelodyProgress;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

public class LuteAnimator implements Animator {
    @Override
    public void setAngles(ModelPart left, ModelPart right, BipedEntityModel<?> model, LivingEntity entity, MelodyProgress progress, float time) {
        left.pitch = -0.5f;
        left.roll = -0.2f;
        left.yaw = (progress.getCurrentPitch() - 0.5f) - 0.4f;

        right.pitch = -0.75f;
        right.yaw = 0.0f;
        right.roll = right.roll * 0.25f - 0.2f;
    }
}
