package immersive_melodies.mixin;

import immersive_melodies.Items;
import immersive_melodies.client.CustomInventoryModels;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModelLoader.class, priority = 2000)
public abstract class ModelLoaderMixin {
    @Shadow
    protected abstract void addModel(ModelIdentifier modelId);

    @Shadow
    @Final
    public static ModelIdentifier MISSING_ID;

    @Inject(method = "addModel(Lnet/minecraft/client/util/ModelIdentifier;)V", at = @At("HEAD"))
    void immersiveMelodies$injectModelLoaderInit(ModelIdentifier modelId, CallbackInfo ci) {
        if (modelId == MISSING_ID) {
            for (Identifier identifier : Items.customInventoryModels) {
                ModelIdentifier modelIdentifier = CustomInventoryModels.computeHandIdentifier(identifier);
                addModel(modelIdentifier);
            }
        }
    }
}
