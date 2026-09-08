package immersive_melodies.mixin;

import immersive_melodies.client.animation.EntityModelAnimator;
import immersive_melodies.client.animation.accessors.ArmsAndHeadAccessor;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IllagerModel.class)
public class IllagerEntityModelMixin<S extends IllagerRenderState> {
    @Shadow
    @Final
    private ModelPart head;

    @Shadow
    @Final
    private ModelPart hat;

    @Shadow
    @Final
    private ModelPart leftArm;

    @Shadow
    @Final
    private ModelPart rightArm;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/IllagerRenderState;)V", at = @At("TAIL"))
    public void immersiveMelodies$injectSetupAnim(S state, CallbackInfo ci) {
        LivingEntity entity = EntityModelAnimator.getEntity(state);
        if (entity != null) {
            EntityModelAnimator.setAngles(new ArmsAndHeadAccessor<>(entity, head, hat, leftArm, rightArm), state);
        }
    }
}
