package immersive_melodies.mixin;

import immersive_melodies.item.InstrumentItem;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract Iterable<ItemStack> getHandItems();

    @Shadow
    public abstract World getWorld();

    @Inject(method = "tick()V", at = @At("HEAD"))
    public void immersiveMelodies$injectTick(CallbackInfo ci) {
        this.getHandItems().forEach(itemStack -> {
            if (itemStack.getItem() instanceof InstrumentItem item) {
                item.inventoryTick(itemStack, getWorld(), (Entity) (Object) this, 0, true);
            }
        });
    }
}
