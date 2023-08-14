package immersive_melodies.forge;

import immersive_melodies.*;
import immersive_melodies.forge.cobalt.network.NetworkHandlerImpl;
import immersive_melodies.forge.cobalt.registration.RegistrationImpl;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import static net.minecraft.registry.RegistryKeys.ITEM_GROUP;

@Mod(Common.MOD_ID)
@Mod.EventBusSubscriber(modid = Common.MOD_ID, bus = Bus.MOD)
public final class CommonForge {
    public CommonForge() {
        RegistrationImpl.bootstrap();
        new NetworkHandlerImpl();
        DEF_REG.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @SubscribeEvent
    public static void onRegistryEvent(RegisterEvent event) {
        Items.bootstrap();
        Messages.bootstrap();
        Sounds.bootstrap();
    }

    public static final DeferredRegister<ItemGroup> DEF_REG = DeferredRegister.create(ITEM_GROUP, Common.MOD_ID);

    @SuppressWarnings("unused")
    public static final RegistryObject<ItemGroup> TAB = DEF_REG.register(Common.MOD_ID, () -> ItemGroup.builder()
            .displayName(ItemGroups.getDisplayName())
            .icon(ItemGroups::getIcon)
            .entries((featureFlags, output) -> output.addAll(Items.getSortedItems()))
            .build()
    );
}
