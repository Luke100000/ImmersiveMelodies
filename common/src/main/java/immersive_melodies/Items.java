package immersive_melodies;

import immersive_melodies.cobalt.registration.Registration;
import immersive_melodies.item.InstrumentItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public interface Items {
    List<Supplier<Item>> items = new LinkedList<>();

    Supplier<Item> TRIANGLE = register("triangle", () -> new InstrumentItem(baseProps()));

    static Supplier<Item> register(String name, Supplier<Item> item) {
        Supplier<Item> register = Registration.register(Registries.ITEM, Common.locate(name), item);
        items.add(register);
        return register;
    }

    static void bootstrap() {
        // nop
    }

    static Item.Settings baseProps() {
        return new Item.Settings();
    }

    static Collection<ItemStack> getSortedItems() {
        return items.stream().map(i -> i.get().getDefaultStack()).toList();
    }
}
