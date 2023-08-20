package immersive_melodies.util;

import immersive_melodies.Config;
import immersive_melodies.Items;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

import java.util.Random;

public class EntityEquiper {
    public static void equip(Entity entity, Random random) {
        String id = Registry.ENTITY_TYPE.getId(entity.getType()).toString();
        if (Config.getInstance().mobInstrumentFactors.containsKey(id) && random.nextFloat() < Config.getInstance().mobInstrumentFactors.get(id)) {
            Item item = Items.items.get(random.nextInt(Items.items.size())).get();
            entity.equipStack(EquipmentSlot.MAINHAND, new ItemStack(item));
        }
    }
}
