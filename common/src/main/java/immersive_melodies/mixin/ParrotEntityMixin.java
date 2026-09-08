package immersive_melodies.mixin;

import immersive_melodies.client.MelodyProgressManager;
import net.minecraft.world.entity.animal.parrot.Parrot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Parrot.class)
public class ParrotEntityMixin {
    @Inject(method = "isPartyParrot()Z", at = @At("HEAD"), cancellable = true)
    void immersiveMelodies$injectIsPartyParrot(CallbackInfoReturnable<Boolean> cir) {
        if (MelodyProgressManager.INSTANCE.isClose(((Parrot) (Object) this).position(), 5.0f)) {
            cir.setReturnValue(true);
        }
    }
}
