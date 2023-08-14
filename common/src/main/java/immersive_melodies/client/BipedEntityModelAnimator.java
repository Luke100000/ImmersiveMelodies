package immersive_melodies.client;

import immersive_melodies.item.InstrumentItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class BipedEntityModelAnimator {
    public static <T extends LivingEntity> boolean holdsInstrument(T entity) {
        for (ItemStack handItem : entity.getHandItems()) {
            if (handItem.getItem() instanceof InstrumentItem) {
                return true;
            }
        }
        return false;
    }

    public static <T extends LivingEntity> ModelPart getLeftArm(BipedEntityModel<T> model, T entity) {
        return entity.getMainHandStack().getItem() instanceof InstrumentItem ? model.leftArm : model.rightArm;
    }

    public static <T extends LivingEntity> ModelPart getRightArm(BipedEntityModel<T> model, T entity) {
        return entity.getMainHandStack().getItem() instanceof InstrumentItem ? model.rightArm : model.leftArm;
    }

    public static <T extends LivingEntity> void setAngles(BipedEntityModel<T> model, T entity) {
        if (holdsInstrument(entity)) {
            ModelPart left = getLeftArm(model, entity);
            ModelPart right = getRightArm(model, entity);

            float time = (MinecraftClient.getInstance().isPaused() ? 0.0f : MinecraftClient.getInstance().getTickDelta()) + entity.age;

            MelodyProgressHandler.MelodyProgress progress = MelodyProgressHandler.INSTANCE.getProgress(entity);
            progress.tick(time);

            float delta = (float) Math.sin(progress.getCurrent() * Math.PI * 0.5);

            left.pitch = -1.1f + progress.getCurrentPitch() * 0.25f;
            left.yaw = delta * 0.6f;

            right.pitch = -1.6f;
            right.roll = (float) Math.cos(time * 0.25f) * 0.05f * progress.getCurrentVolume();
            right.yaw = -0.5f;
        }
    }
}
