package immersive_melodies;

import com.google.common.base.Suppliers;
import immersive_melodies.client.animation.ItemAnimators;
import immersive_melodies.client.animation.animators.Animator;
import immersive_melodies.item.InstrumentItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface Items {
    Map<Identifier, Supplier<Item>> items = new ConcurrentHashMap<>();

    Supplier<Item> BAGPIPE = register(Common.MOD_ID, "bagpipe", 300, new Vector3f(0.5f, 0.6f, 0.05f));
    Supplier<Item> DIDGERIDOO = register(Common.MOD_ID, "didgeridoo", 400, new Vector3f(0.0f, -0.45f, 1.0f));
    Supplier<Item> FLUTE = register(Common.MOD_ID, "flute", 100, new Vector3f(0.0f, 0.15f, 0.9f));
    Supplier<Item> LUTE = register(Common.MOD_ID, "lute", 300, new Vector3f(0.0f, 0.0f, 0.5f));
    Supplier<Item> PIANO = register(Common.MOD_ID, "piano", 500, new Vector3f(0.0f, 0.25f, 0.5f));
    Supplier<Item> TRIANGLE = register(Common.MOD_ID, "triangle", 300, new Vector3f(0.0f, 0.0f, 0.6f));
    Supplier<Item> TRUMPET = register(Common.MOD_ID, "trumpet", 100, new Vector3f(0.0f, 0.25f, 1.4f));
    Supplier<Item> TINY_DRUM = register(Common.MOD_ID, "tiny_drum", 500, new Vector3f(0.0f, 0.25f, 0.5f));
    Supplier<Item> VIELLE = register(Common.MOD_ID, "vielle", 200, new Vector3f(-0.25f, 0.4f, 0.35f));
    Supplier<Item> ENDER_BASS = register(Common.MOD_ID, "ender_bass", 100, new Vector3f(0.0f, 0.0f, 0.65f));
    Supplier<Item> HANDPAN = register(Common.MOD_ID, "handpan", 300, new Vector3f(0.0f, 0.25f, 0.5f));

    /**
     * Open method to create custom items, for addons
     *
     * @param namespace Your addon's namespace
     * @param name      Your addon's item name
     * @param animator  Your item's animator. Allows defining how the entity
     *                  model should be animated when playing the instrument.
     * @param sustain   The instruments sustain, in ticks.
     * @param offset    Determines the offset from the player's location
     *                  of the position at which a note particle should be displayed.
     * @return The registered item's provider.
     */
    static Supplier<Item> register(@NotNull String namespace, @NotNull String name, Animator animator, long sustain, Vector3f offset) {
        Identifier identifier = Identifier.fromNamespaceAndPath(namespace, name);
        ItemAnimators.register(identifier, animator);
        return register(namespace, name, sustain, offset);
    }

    /**
     * Open-method to create custom items, for addons.
     * If using this method, make sure to also register an {@link Animator} for your item.
     *
     * @param namespace Your addon's namespace
     * @param name      Your addon's item name
     * @param sustain   The instruments sustain, in ticks.
     * @param offset    Determines the offset from the player's location
     *                  of the position at which a note particle should be displayed.
     * @return The registered item's provider.
     */
    static Supplier<Item> register(@NotNull String namespace, @NotNull String name, long sustain, Vector3f offset) {
        Identifier location = Identifier.fromNamespaceAndPath(namespace, name);
        Supplier<Item> supplier = Suppliers.memoize(() -> new InstrumentItem(
                baseProps().setId(ResourceKey.create(Registries.ITEM, location)), new Sounds.Instrument(namespace, name), sustain, offset
        ));
        items.put(location, supplier);
        return supplier;
    }

    static Item.Properties baseProps() {
        return new Item.Properties().stacksTo(1);
    }

    static Collection<ItemStack> getSortedItems() {
        return items.values().stream().map(Supplier::get).map(Item::getDefaultInstance).toList();
    }

    static void registerItems(Common.RegisterHelper<Item> helper) {
        items.forEach((identifier, supplier) -> helper.register(identifier, supplier.get()));
    }
}
