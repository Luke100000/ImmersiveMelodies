package immersive_melodies.mixin;

import immersive_melodies.Config;
import immersive_melodies.Items;
import immersive_melodies.item.InstrumentItem;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
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
    @Final
    private DefaultedList<ItemStack> handItems;

    @Shadow
    protected abstract Vec3i getItemPickUpRangeExpander();

    @Shadow
    protected abstract void loot(ItemEntity item);

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

    @Inject(method = "initialize(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/world/LocalDifficulty;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/entity/EntityData;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/entity/EntityData;", at = @At("TAIL"))
    private void immersiveArmors$injectGetEquipmentForSlot(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound entityNbt, CallbackInfoReturnable<EntityData> cir) {
        if (Config.getInstance().mobInstrumentFactor > 0 && random.nextFloat() < Config.getInstance().mobInstrumentFactor) {
            Item item = Items.items.get(random.nextInt(Items.items.size())).get();
            equipStack(EquipmentSlot.MAINHAND, new ItemStack(item));
        }
    }

    @Inject(method = "tickMovement()V", at = @At("TAIL"))
    private void immersiveMelodies$injectTickMovement(CallbackInfo ci) {
        if (Config.getInstance().forceMobsToPickUp && !this.getWorld().isClient && this.isAlive() && !this.dead) {
            Vec3i vec3i = this.getItemPickUpRangeExpander();
            for (ItemEntity itementity : this.getWorld().getNonSpectatingEntities(ItemEntity.class, this.getBoundingBox().expand(vec3i.getX(), vec3i.getY(), vec3i.getZ()))) {
                if (!itementity.isRemoved() && !itementity.getStack().isEmpty() && itementity.getStack().getItem() instanceof InstrumentItem) {
                    this.loot(itementity);
                }
            }
        }
    }

    @Inject(method= "prefersNewEquipment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", at=@At("HEAD"), cancellable = true)
    private void immersiveMelodies$injectPrefersNewEquipment(ItemStack newStack, ItemStack oldStack, CallbackInfoReturnable<Boolean> cir) {
        if (newStack.getItem() instanceof InstrumentItem && !(oldStack.getItem() instanceof InstrumentItem)) {
            cir.setReturnValue(true);
        } else if (oldStack.getItem() instanceof InstrumentItem) {
            cir.setReturnValue(false);
        }
    }
}
