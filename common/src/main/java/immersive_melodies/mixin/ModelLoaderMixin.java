package immersive_melodies.mixin;

import immersive_melodies.Items;
import immersive_melodies.client.CustomInventoryModels;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {
    @Shadow
    protected abstract void addModel(ModelIdentifier modelId);

    @Shadow
    @Final
    private Map<Identifier, UnbakedModel> modelsToBake;

    @Shadow
    public abstract UnbakedModel getOrLoadModel(Identifier id);

    @Unique
    private boolean immersive_melodies$done = false;

    @Inject(method = "addModel(Lnet/minecraft/client/util/ModelIdentifier;)V", at = @At("HEAD"))
    void immersiveMelodies$injectModelLoaderInit(ModelIdentifier modelId, CallbackInfo ci) {
        if (!immersive_melodies$done) {
            immersive_melodies$done = true;

            for (Identifier identifier : Items.customInventoryModels) {
                ModelIdentifier modelIdentifier = CustomInventoryModels.computeHandIdentifier(identifier);
                addModel(modelIdentifier);
            }
        }
    }
}
