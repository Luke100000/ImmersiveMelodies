package immersive_melodies.mixin;

import immersive_melodies.item.InstrumentItem;
import immersive_melodies.util.Utils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientLevel.class, priority = 900)
public class ClientWorldMixin {
    @Inject(method = "tickNonPassenger", at = @At("HEAD"))
    public void immersiveMelodies$injectTickEntity(Entity entity, CallbackInfo ci) {
        immersiveMelodies$tick(entity);
    }

    @Inject(method = "tickPassenger", at = @At("HEAD"))
    public void immersiveMelodies$injectTickPassenger(Entity entity, Entity passenger, CallbackInfo ci) {
        immersiveMelodies$tick(passenger);
    }

    @Unique
    private void immersiveMelodies$tick(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            Utils.getHandItems(livingEntity).forEach(itemStack -> {
                if (itemStack.getItem() instanceof InstrumentItem item) {
                    item.inventoryClientTick(itemStack, (ClientLevel) (Object) this, livingEntity);
                }
            });
        }
    }
}
