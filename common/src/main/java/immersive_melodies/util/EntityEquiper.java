package immersive_melodies.util;

import immersive_melodies.Config;
import immersive_melodies.Items;
import immersive_melodies.item.InstrumentItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

public class EntityEquiper {
    public static void equip(LivingEntity entity) {
        if (shouldEquip(entity)) {
            Item item = Items.items.values().stream()
                    .skip(new Random().nextInt(Items.items.size()))
                    .findFirst()
                    .orElse(Items.LUTE)
                    .get();
            entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(item));
            if (entity instanceof Mob mobEntity) {
                mobEntity.setDropChance(EquipmentSlot.MAINHAND, Config.getInstance().mobInstrumentDropFactor);
            }
        }
    }

    public static boolean shouldEquip(Entity entity) {
        if (entity instanceof LivingEntity livingEntity && hasInstrument(livingEntity)) {
            return false;
        }

        String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        return Config.getInstance().mobInstrumentFactors.containsKey(id) && entity.level().getRandom().nextFloat() < Config.getInstance().mobInstrumentFactors.get(id);
    }

    public static boolean canPickUp(Entity entity) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        return Config.getInstance().mobInstrumentFactors.containsKey(id) && Config.getInstance().mobInstrumentFactors.get(id) > 0;
    }

    private static boolean hasInstrument(LivingEntity entity) {
        for (ItemStack handItem : Utils.getHandItems(entity)) {
            if (handItem.getItem() instanceof InstrumentItem) {
                return true;
            }
        }
        return false;
    }
}
