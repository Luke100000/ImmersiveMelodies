package immersive_melodies.client.animation.animators;

import immersive_melodies.client.MelodyProgress;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

public class TriangleAnimator implements Animator {
    @Override
    public void setAngles(ModelPart left, ModelPart right, BipedEntityModel<?> model, LivingEntity entity, MelodyProgress progress, float time) {
        float delta = (float) Math.sin(progress.getCurrent() * Math.PI * 0.5);

        left.pitch = -1.1f + progress.getCurrentPitch() * 0.25f;
        left.yaw = delta * 0.6f;

        right.pitch = -1.6f;
        right.roll = (float) Math.cos(time * 0.25f) * 0.05f * progress.getCurrentVolume();
        right.yaw = -0.5f;
    }
}
