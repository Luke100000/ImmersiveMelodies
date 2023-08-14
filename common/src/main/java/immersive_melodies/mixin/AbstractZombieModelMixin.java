package immersive_melodies.mixin;

import immersive_melodies.client.BipedEntityModelAnimator;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AbstractZombieModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.mob.HostileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractZombieModel.class)
public abstract class AbstractZombieModelMixin<T extends HostileEntity> extends BipedEntityModel<T> {
    public AbstractZombieModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/mob/HostileEntity;FFFFF)V", at = @At("HEAD"), cancellable = true)
    public void immersiveMelodies$injectSetAngles(T entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (BipedEntityModelAnimator.holdsInstrument(entity)) {
            super.setAngles(entity, f, g, h, i, j);
            ci.cancel();
        }
    }
}
