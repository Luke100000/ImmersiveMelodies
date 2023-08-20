package immersive_melodies.fabric;

import immersive_melodies.ItemGroups;
import immersive_melodies.Items;
import immersive_melodies.Messages;
import immersive_melodies.Sounds;
import immersive_melodies.fabric.cobalt.network.NetworkHandlerImpl;
import immersive_melodies.fabric.cobalt.registration.RegistrationImpl;
import immersive_melodies.fabric.resources.FabricMelody;
import immersive_melodies.resources.ServerMelodyManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public final class CommonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        new RegistrationImpl();
        new NetworkHandlerImpl();

        Items.bootstrap();
        Messages.bootstrap();
        Sounds.bootstrap();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new FabricMelody());

        ServerLifecycleEvents.SERVER_STARTING.register(server -> ServerMelodyManager.server = server);

        FabricItemGroup.builder(ItemGroups.getIdentifier())
                .displayName(ItemGroups.getDisplayName())
                .icon(ItemGroups::getIcon)
                .entries((enabledFeatures, entries) -> entries.addAll(Items.items.stream().map(i -> i.get().getDefaultStack()).toList()))
                .build();
    }
}

