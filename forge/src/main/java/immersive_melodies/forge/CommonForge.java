package immersive_melodies.forge;

import immersive_melodies.*;
import immersive_melodies.forge.cobalt.network.NetworkHandlerImpl;
import immersive_melodies.forge.cobalt.registration.RegistrationImpl;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod(Common.MOD_ID)
@Mod.EventBusSubscriber(modid = Common.MOD_ID, bus = Bus.MOD)
public final class CommonForge {
    public CommonForge() {
        RegistrationImpl.bootstrap();
        new NetworkHandlerImpl();

        ItemGroups.GROUP = new ItemGroup(ItemGroup.getGroupCountSafe(), ItemGroups.getIdentifier().toString().replace(":", ".")) {
            @Override
            public ItemStack createIcon() {
                return ItemGroups.getIcon();
            }
        };

        Items.bootstrap();
        Messages.bootstrap();
        Sounds.bootstrap();
    }
}
