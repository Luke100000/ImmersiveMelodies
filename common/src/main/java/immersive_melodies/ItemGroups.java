package immersive_melodies;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemGroups {
    public static Identifier getIdentifier() {
        return Common.locate(Common.MOD_ID + "_tab");
    }

    public static Text getDisplayName() {
        return Text.translatable("itemGroup." + ItemGroups.getIdentifier().toTranslationKey());
    }

    public static ItemStack getIcon() {
        return Items.TRIANGLE.get().getDefaultStack();
    }
}
