package immersive_melodies.client.animation.animators;

import immersive_melodies.client.MelodyProgress;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

public class FluteAnimator implements Animator {
    protected float getVerticalOffset(float time) {
        return (float) (Math.cos(time * 0.15f) * 0.05f) + 0.25f;
    }

    protected float getHorizontalOffset(float time) {
        return (float) (Math.sin(time * 0.1f) * 0.05f);
    }

    @Override
    public void setAngles(ModelPart left, ModelPart right, BipedEntityModel<?> model, LivingEntity entity, MelodyProgress progress, float time) {
        model.head.pitch = getVerticalOffset(time);
        model.head.yaw = 0.0f;

        float horizontalOffset = getHorizontalOffset(time);

        left.pitch = (float) (-Math.PI / 2.0f) + model.head.pitch;
        left.yaw = 0.4f - 0.1f * progress.getCurrentPitch() + horizontalOffset;
        left.roll = 0.25f * progress.getCurrentPitch();

        right.pitch = (float) (-Math.PI / 2.0f) + model.head.pitch;
        right.yaw = -0.45f + horizontalOffset;
    }
}
