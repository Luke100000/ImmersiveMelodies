package immersive_melodies.client.animation;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface EntityRenderStateAccessor {
    @Nullable
    LivingEntity immersiveMelodies$getEntity();

    void immersiveMelodies$setEntity(LivingEntity entity);
}
