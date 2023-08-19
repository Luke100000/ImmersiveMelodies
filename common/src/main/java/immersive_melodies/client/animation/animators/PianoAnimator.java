package immersive_melodies.client.animation.animators;

import immersive_melodies.client.MelodyProgress;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

public class PianoAnimator implements Animator {
    @Override
    public void setAngles(ModelPart left, ModelPart right, BipedEntityModel<?> model, LivingEntity entity, MelodyProgress progress, float time) {
        left.pitch = -1.4f - progress.getCurrent() * progress.getCurrentVolume() * 0.5f;
        left.yaw = (progress.getCurrentPitch() - 0.5f) + 0.2f;

        right.pitch = -0.9f;
        right.yaw = -0.15f;
        right.roll = 0.0f;
    }
}
