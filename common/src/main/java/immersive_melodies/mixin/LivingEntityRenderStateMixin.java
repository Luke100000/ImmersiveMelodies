package immersive_melodies.mixin;

import immersive_melodies.client.animation.EntityRenderStateAccessor;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements EntityRenderStateAccessor {
    @Unique
    @Nullable
    private LivingEntity immersiveMelodies$entity;

    @Override
    public LivingEntity immersiveMelodies$getEntity() {
        return immersiveMelodies$entity;
    }

    @Override
    public void immersiveMelodies$setEntity(LivingEntity entity) {
        immersiveMelodies$entity = entity;
    }
}
