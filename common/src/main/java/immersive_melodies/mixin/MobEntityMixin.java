package immersive_melodies.mixin;

import immersive_melodies.item.InstrumentItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity  {
    @Shadow @Final private DefaultedList<ItemStack> handItems;

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    public void immersiveMelodies$injectTick(CallbackInfo ci) {
        handItems.forEach(itemStack -> {
            if (itemStack.getItem() instanceof InstrumentItem item) {
                item.inventoryTick(itemStack, getWorld(), (MobEntity) (Object) this, 0, true);
            }
        });
    }
}
