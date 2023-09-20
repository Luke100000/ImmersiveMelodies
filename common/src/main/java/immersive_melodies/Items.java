package immersive_melodies;

import immersive_melodies.client.animation.ItemAnimators;
import immersive_melodies.client.animation.animators.Animator;
import immersive_melodies.cobalt.registration.Registration;
import immersive_melodies.item.InstrumentItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface Items {
    List<Supplier<Item>> items = new LinkedList<>();
    List<Identifier> customInventoryModels = new LinkedList<>();

    Supplier<Item> BAGPIPE = register(Common.MOD_ID, "bagpipe", 200, new Vec3f(0.5f, 0.6f, 0.05f));
    Supplier<Item> DIDGERIDOO = register(Common.MOD_ID, "didgeridoo", 200, new Vec3f(0.0f, -0.45f, 1.0f));
    Supplier<Item> FLUTE = register(Common.MOD_ID, "flute", 100, new Vec3f(0.0f, 0.15f, 0.9f));
    Supplier<Item> LUTE = register(Common.MOD_ID, "lute", 200, new Vec3f(0.0f, 0.0f, 0.5f));
    Supplier<Item> PIANO = register(Common.MOD_ID, "piano", 300, new Vec3f(0.0f, 0.25f, 0.5f));
    Supplier<Item> TRIANGLE = register(Common.MOD_ID, "triangle", 300, new Vec3f(0.0f, 0.0f, 0.6f));
    Supplier<Item> TRUMPET = register(Common.MOD_ID, "trumpet", 100, new Vec3f(0.0f, 0.25f, 1.4f));

    /**
     * Open method to create custom items, for addons
     *
     * @param namespace Your addon's namespace
     * @param name      Your addon's item name
     * @param animator  Your item's animator. Allows to define how the entity
     *                  model should be animated when playing the instrument.
     * @param sustain   Determines the instrument's note sustain capacity.
     *                  Defines how many ticks can a note hold for the longest
     *                  with your custom instrument.
     * @param offset   Determines the offset from the player's location
     *                  of the position at which a note particle should be displayed.
     * @return The registered item's provider.
     */
    static @Nullable Supplier<Item> register(@NotNull String namespace, @NotNull String name, Animator animator,
                                             long sustain, Vec3f offset) {
        Identifier identifier = new Identifier(namespace, name);
        Supplier<Item> supplier = register(namespace, name, sustain, offset);
        ItemAnimators.register(identifier, animator);
        return supplier;
    }

    /**
     * Open method to create custom items, for addons.
     * If using this method, make sure to also register an {@link Animator} for your item.
     *
     * @param namespace Your addon's namespace
     * @param name      Your addon's item name
     * @param sustain   Your item's animator. Allows to define how the entity
     *                  model should be animated when playing the instrument.
     * @param offset   Determines the offset from the player's location
     *                  of the position at which a note particle should be displayed.
     * @return The registered item's provider.
     */
    static @Nullable Supplier<Item> register(@NotNull String namespace, @NotNull String name,
                                             long sustain, Vec3f offset) {
        Identifier identifier = new Identifier(namespace, name);
        Sounds.Instrument instrument = new Sounds.Instrument(namespace, name);
        Supplier<Item> itemSupplier = () -> new InstrumentItem(baseProps(), instrument, sustain, offset);
        Supplier<Item> supplier = Registration.register(Registry.ITEM, identifier, itemSupplier);
        items.add(supplier);
        customInventoryModels.add(identifier);
        return supplier;
    }

    static void bootstrap() {
        // nop
    }

    static Item.Settings baseProps() {
        return new Item.Settings().maxCount(1).group(ItemGroups.GROUP);
    }

    static Collection<ItemStack> getSortedItems() {
        return items.stream().map(i -> i.get().getDefaultStack()).toList();
    }
}
