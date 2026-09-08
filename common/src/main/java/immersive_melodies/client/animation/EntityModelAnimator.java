package immersive_melodies.client.animation;

import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.MelodyProgressManager;
import immersive_melodies.client.animation.accessors.ModelAccessor;
import immersive_melodies.item.InstrumentItem;
import immersive_melodies.util.Utils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityModelAnimator {
    public static Item getInstrument(LivingEntity entity) {
        for (ItemStack handItem : Utils.getHandItems(entity)) {
            if (handItem.getItem() instanceof InstrumentItem instrument && instrument.isPlaying(handItem)) {
                return handItem.getItem();
            }
        }

        for (ItemStack handItem : Utils.getHandItems(entity)) {
            if (handItem.getItem() instanceof InstrumentItem) {
                return handItem.getItem();
            }
        }

        return null;
    }

    public static Item getInstrument(EntityRenderState state) {
        if (state instanceof ArmedEntityRenderState armed) {
            ItemStack mainHand = armed.mainArm == HumanoidArm.RIGHT ? armed.rightHandItemStack : armed.leftHandItemStack;
            ItemStack offHand = armed.mainArm == HumanoidArm.RIGHT ? armed.leftHandItemStack : armed.rightHandItemStack;
            Item playing = getInstrument(mainHand, offHand, true);
            return playing != null ? playing : getInstrument(mainHand, offHand, false);
        }

        LivingEntity entity = getEntity(state);
        return entity == null ? null : getInstrument(entity);
    }

    private static Item getInstrument(ItemStack mainHand, ItemStack offHand, boolean playingOnly) {
        for (ItemStack handItem : List.of(mainHand, offHand)) {
            if (handItem.getItem() instanceof InstrumentItem instrument
                && (!playingOnly || instrument.isPlaying(handItem))) {
                return handItem.getItem();
            }
        }
        return null;
    }

    @Nullable
    public static LivingEntity getEntity(EntityRenderState state) {
        return state instanceof EntityRenderStateAccessor accessor ? accessor.immersiveMelodies$getEntity() : null;
    }

    private static boolean isInMainHand(LivingEntity entity) {
        return entity.getMainHandItem().getItem() instanceof InstrumentItem;
    }

    @Deprecated
    public static ModelPart getLeftArm(HumanoidModel<?> model, LivingEntity entity) {
        return isInMainHand(entity) ? model.leftArm : model.rightArm;
    }

    @Deprecated
    public static ModelPart getRightArm(HumanoidModel<?> model, LivingEntity entity) {
        return isInMainHand(entity) ? model.rightArm : model.leftArm;
    }

    public static <T extends LivingEntity> void setAngles(ModelAccessor<T> accessor, EntityRenderState state) {
        setAngles(accessor, getInstrument(state), state.ageInTicks);
    }

    private static <T extends LivingEntity> void setAngles(ModelAccessor<T> accessor, Item item, float time) {
        T entity = accessor.getEntity();
        if (item != null) {
            MelodyProgress progress = MelodyProgressManager.INSTANCE.getProgress(entity);
            progress.visualTick(time);

            // Apply animations
            ItemAnimators.get(BuiltInRegistries.ITEM.getKey(item)).setAngles(accessor, progress, time);
        }
    }
}
