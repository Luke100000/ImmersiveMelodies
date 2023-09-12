package immersive_melodies.mixin;

import immersive_melodies.Config;
import immersive_melodies.item.InstrumentItem;
import immersive_melodies.util.EntityEquiper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
    @Shadow
    protected abstract void loot(ItemEntity item);

    @Shadow
    @Final
    private DefaultedList<ItemStack> handItems;

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at = @At("HEAD"), cancellable = true)
    private void immersiveMelodies$injectInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        for (ItemStack handItem : this.handItems) {
            if (handItem.getItem() instanceof InstrumentItem) {
                ItemEntity itemEntity = dropStack(handItem.copy());
                handItem.setCount(0);
                if (itemEntity != null) {
                    itemEntity.setThrower(getUuid());
                }
                cir.setReturnValue(ActionResult.CONSUME);
                break;
            }
        }
    }

    @Inject(method = "tickMovement()V", at = @At("TAIL"))
    private void immersiveMelodies$injectTickMovement(CallbackInfo ci) {
        if (Config.getInstance().forceMobsToPickUp && EntityEquiper.canPickUp(this) && !this.getWorld().isClient && this.isAlive() && !this.dead) {
            for (ItemEntity itementity : this.getWorld().getNonSpectatingEntities(ItemEntity.class, this.getBoundingBox().expand(1.0, 0.0, 1.0))) {
                if ((itementity.getOwner() == null || !itementity.getOwner().equals(getUuid())) && !itementity.isRemoved() && !itementity.getStack().isEmpty() && itementity.getStack().getItem() instanceof InstrumentItem) {
                    System.out.println(itementity.getOwner());
                    this.loot(itementity);
                }
            }
        }
    }

    @Inject(method = "prefersNewEquipment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void immersiveMelodies$injectPrefersNewEquipment(ItemStack newStack, ItemStack oldStack, CallbackInfoReturnable<Boolean> cir) {
        if (newStack.getItem() instanceof InstrumentItem && !(oldStack.getItem() instanceof InstrumentItem)) {
            cir.setReturnValue(true);
        } else if (oldStack.getItem() instanceof InstrumentItem) {
            cir.setReturnValue(false);
        }
    }
}
