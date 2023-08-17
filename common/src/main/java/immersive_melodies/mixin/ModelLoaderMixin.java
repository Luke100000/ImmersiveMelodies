package immersive_melodies.mixin;

import immersive_melodies.Items;
import immersive_melodies.client.CustomInventoryModels;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {
    @Shadow
    protected abstract void addModel(ModelIdentifier modelId);

    @Shadow @Final private Map<Identifier, UnbakedModel> modelsToBake;

    @Shadow public abstract UnbakedModel getOrLoadModel(Identifier id);

    @Inject(method = "<init>(Lnet/minecraft/client/color/block/BlockColors;Lnet/minecraft/util/profiler/Profiler;Ljava/util/Map;Ljava/util/Map;)V", at = @At("TAIL"))
    void immersiveMelodies$injectModelLoaderInit(BlockColors blockColors, Profiler profiler, Map<Identifier, JsonUnbakedModel> jsonUnbakedModels, Map<Identifier, List<ModelLoader.SourceTrackedData>> blockStates, CallbackInfo ci) {
        for (Identifier identifier : Items.customInventoryModels) {
            ModelIdentifier modelIdentifier = CustomInventoryModels.computeHandIdentifier(identifier);
            addModel(modelIdentifier);
            modelsToBake.get(modelIdentifier).setParents(this::getOrLoadModel);
        }
    }
}
