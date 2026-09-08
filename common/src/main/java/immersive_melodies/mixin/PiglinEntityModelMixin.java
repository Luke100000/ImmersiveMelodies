package immersive_melodies.mixin;

import immersive_melodies.client.animation.EntityModelAnimator;
import net.minecraft.client.model.monster.piglin.AbstractPiglinModel;
import net.minecraft.client.model.monster.piglin.PiglinModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.PiglinRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinModel.class)
public abstract class PiglinEntityModelMixin extends AbstractPiglinModel<PiglinRenderState> {
    public PiglinEntityModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PiglinRenderState;)V", at = @At("HEAD"), cancellable = true)
    public void immersiveMelodies$injectSetAnim(PiglinRenderState state, CallbackInfo ci) {
        if (EntityModelAnimator.getInstrument(state) != null) {
            super.setupAnim(state);
            ci.cancel();
        }
    }
}
