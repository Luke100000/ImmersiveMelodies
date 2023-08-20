package immersive_melodies.mixin;

import immersive_melodies.util.EntityEquiper;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(method = "addEntity(Lnet/minecraft/entity/Entity;)Z",
            at = @At("HEAD")
    )
    private void onAddEntity(Entity entity, CallbackInfoReturnable<Boolean> info) {
        EntityEquiper.equip(entity, entity.getWorld().getRandom());
    }
}
