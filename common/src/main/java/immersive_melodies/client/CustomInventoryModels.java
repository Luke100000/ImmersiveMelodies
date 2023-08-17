package immersive_melodies.client;

import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class CustomInventoryModels {
    private static final Map<Item, ModelIdentifier> handIdentifierCache = new HashMap<>();
    private static final Map<Item, ModelIdentifier> identifierCache = new HashMap<>();

    public static ModelIdentifier computeHandIdentifier(Identifier id) {
        return new ModelIdentifier(id.getNamespace(), id.getPath() + "_hand", "inventory");
    }

    public static ModelIdentifier computeIdentifier(Identifier id) {
        return new ModelIdentifier(id.getNamespace(), id.getPath(), "inventory");
    }

    public static ModelIdentifier computeHandIdentifier(Item item) {
        Identifier id = Registries.ITEM.getId(item);
        return computeHandIdentifier(id);
    }

    public static ModelIdentifier computeIdentifier(Item item) {
        Identifier id = Registries.ITEM.getId(item);
        return computeIdentifier(id);
    }

    public static ModelIdentifier getHandIdentifier(Item item) {
        return handIdentifierCache.computeIfAbsent(item, CustomInventoryModels::computeHandIdentifier);
    }

    public static ModelIdentifier getIdentifier(Item item) {
        return identifierCache.computeIfAbsent(item, CustomInventoryModels::computeIdentifier);
    }
}
