package immersive_melodies.mixin;

import immersive_melodies.client.animation.EntityModelAnimator;
import immersive_melodies.client.animation.accessors.BipedModelAccessor;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class BipedEntityModelMixin<T extends HumanoidRenderState> extends EntityModel<T> implements ArmedModel<T>, HeadedModel {
    protected BipedEntityModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    public void immersiveMelodies$injectSetupAnim(T state, CallbackInfo ci) {
        LivingEntity entity = EntityModelAnimator.getEntity(state);
        if (entity != null) {
            //noinspection unchecked
            EntityModelAnimator.setAngles(new BipedModelAccessor<>((HumanoidModel<T>) (Object) this, entity), state);
        }
    }
}
