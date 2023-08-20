package immersive_melodies.forge;

import immersive_melodies.*;
import immersive_melodies.forge.cobalt.network.NetworkHandlerImpl;
import immersive_melodies.forge.cobalt.registration.RegistrationImpl;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod(Common.MOD_ID)
@Mod.EventBusSubscriber(modid = Common.MOD_ID, bus = Bus.MOD)
public final class CommonForge {
    public CommonForge() {
        RegistrationImpl.bootstrap();
        new NetworkHandlerImpl();

        Items.bootstrap();
        Messages.bootstrap();
        Sounds.bootstrap();
    }

    @SubscribeEvent
    public static void register(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(ItemGroups.getIdentifier(), builder -> builder
                .displayName(ItemGroups.getDisplayName())
                .icon(ItemGroups::getIcon)
                .entries((featureFlags, output) -> output.addAll(Items.items.stream().map(i -> i.get().getDefaultStack()).toList()))
        );
    }
}
