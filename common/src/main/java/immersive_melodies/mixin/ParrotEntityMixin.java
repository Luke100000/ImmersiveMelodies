package immersive_melodies.mixin;

import immersive_melodies.client.MelodyProgressManager;
import net.minecraft.entity.passive.ParrotEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParrotEntity.class)
public class ParrotEntityMixin {
    @Inject(method = "isSongPlaying()Z", at = @At("HEAD"), cancellable = true)
    void immersiveMelodiesIsSongPlaying(CallbackInfoReturnable<Boolean> cir) {
        if (MelodyProgressManager.INSTANCE.isClose(((ParrotEntity) (Object) this).getPos(), 5.0f)) {
            cir.setReturnValue(true);
        }
    }
}
