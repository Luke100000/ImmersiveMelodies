package immersive_melodies.mixin;

import immersive_melodies.client.animation.EntityModelAnimator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.zombie.AbstractZombieModel;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractZombieModel.class)
public abstract class AbstractZombieModelMixin<S extends ZombieRenderState> extends HumanoidModel<S> {
    public AbstractZombieModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/ZombieRenderState;)V", at = @At("HEAD"), cancellable = true)
    public void immersiveMelodies$injectSetupAnim(S state, CallbackInfo ci) {
        if (EntityModelAnimator.getInstrument(state) != null) {
            super.setupAnim(state);
            ci.cancel();
        }
    }
}
