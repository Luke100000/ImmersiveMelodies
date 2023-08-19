package immersive_melodies.client.animation;

import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.MelodyProgressManager;
import immersive_melodies.item.InstrumentItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public class BipedEntityModelAnimator {
    public static <T extends LivingEntity> Item getInstrument(T entity) {
        for (ItemStack handItem : entity.getHandItems()) {
            if (handItem.getItem() instanceof InstrumentItem) {
                return handItem.getItem();
            }
        }
        return null;
    }

    public static <T extends LivingEntity> ModelPart getLeftArm(BipedEntityModel<T> model, T entity) {
        return entity.getMainHandStack().getItem() instanceof InstrumentItem ? model.leftArm : model.rightArm;
    }

    public static <T extends LivingEntity> ModelPart getRightArm(BipedEntityModel<T> model, T entity) {
        return entity.getMainHandStack().getItem() instanceof InstrumentItem ? model.rightArm : model.leftArm;
    }

    public static <T extends LivingEntity> void setAngles(BipedEntityModel<T> model, T entity) {
        Item item = getInstrument(entity);
        if (item != null) {
            ModelPart left = getLeftArm(model, entity);
            ModelPart right = getRightArm(model, entity);

            float time = (MinecraftClient.getInstance().isPaused() ? 0.0f : MinecraftClient.getInstance().getTickDelta()) + entity.age;

            MelodyProgress progress = MelodyProgressManager.INSTANCE.getProgress(entity);
            progress.tick(time);

            ItemAnimators.get(Registries.ITEM.getId(item)).setAngles(left, right, model, entity, progress, time);
        }
    }
}
