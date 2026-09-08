package immersive_melodies.mixin;

import immersive_melodies.item.InstrumentItem;
import immersive_melodies.util.EntityEquiper;
import immersive_melodies.util.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerLevel.class, priority = 900)
public class ServerWorldMixin {
    @Inject(method = "addEntity", at = @At("HEAD"))
    private void onAddEntity(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (entity instanceof LivingEntity livingEntity) {
            EntityEquiper.equip(livingEntity);
        }
    }

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
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        boolean playing = false;
        for (ItemStack itemStack : Utils.getHandItems(livingEntity)) {
            if (itemStack.getItem() instanceof InstrumentItem item) {
                item.inventoryServerTick(itemStack, (ServerLevel) (Object) this, livingEntity);
                playing |= item.isPlaying(itemStack);
            }
        }

        //noinspection ConstantValue
        if (playing && entity.tickCount % 10 == 0) {
            entity.gameEvent(GameEvent.INSTRUMENT_PLAY);
        }
    }
}
