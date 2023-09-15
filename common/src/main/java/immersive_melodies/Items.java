package immersive_melodies;

import immersive_melodies.cobalt.registration.Registration;
import immersive_melodies.item.InstrumentItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
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

    Supplier<Item> BAGPIPE = register("bagpipe", () -> new InstrumentItem(baseProps(), Sounds.BAGPIPE, 200, 0.5f, 0.1f));
    Supplier<Item> DIDGERIDOO = register("didgeridoo", () -> new InstrumentItem(baseProps(), Sounds.DIDGERIDOO, 200, 1.0f, -0.45f));
    Supplier<Item> FLUTE = register("flute", () -> new InstrumentItem(baseProps(), Sounds.FLUTE, 100, 0.9f, 0.1f));
    Supplier<Item> LUTE = register("lute", () -> new InstrumentItem(baseProps(), Sounds.LUTE, 200, 0.5f, 0.0f));
    Supplier<Item> PIANO = register("piano", () -> new InstrumentItem(baseProps(), Sounds.PIANO, 300, 0.5f, 0.25f));
    Supplier<Item> TRIANGLE = register("triangle", () -> new InstrumentItem(baseProps(), Sounds.TRIANGLE, 300, 0.6f, 0.0f));
    Supplier<Item> TRUMPET = register("trumpet", () -> new InstrumentItem(baseProps(), Sounds.TRUMPET, 100, 1.4f, 0.2f));

    static Supplier<Item> register(String name, Supplier<Item> item) {
        Identifier identifier = Common.locate(name);
        Supplier<Item> register = Registration.register(Registry.ITEM, identifier, item);
        items.add(register);
        customInventoryModels.add(identifier);
        return register;
    }

    static @Nullable Supplier<Item> register(@NotNull String namespace, @NotNull String name, long sustain,
                                                    float hOffset, float vOffset) {
        Identifier identifier = Identifier.of(namespace, name);
        if (identifier == null) {
            return null;
        }
        Sounds.Instrument instrument = new Sounds.Instrument(namespace, name);
        Supplier<Item> itemSupplier = () -> new InstrumentItem(baseProps(), instrument, sustain, hOffset, vOffset);
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
