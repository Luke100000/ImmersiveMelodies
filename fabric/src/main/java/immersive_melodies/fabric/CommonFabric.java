package immersive_melodies.fabric;

import immersive_melodies.Common;
import immersive_melodies.ItemGroups;
import immersive_melodies.Items;
import immersive_melodies.Sounds;
import immersive_melodies.fabric.resources.FabricMelody;
import immersive_melodies.network.ImmersivePayload;
import immersive_melodies.network.Network;
import immersive_melodies.resources.ServerMelodyManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Consumer;

public final class CommonFabric implements ModInitializer {
    private static <T> void registerHelper(Registry<T> register, Consumer<Common.RegisterHelper<T>> consumer) {
        consumer.accept((name, value) -> Registry.register(register, name, value));
    }

    @Override
    public void onInitialize() {
        registerHelper(BuiltInRegistries.ITEM, Items::registerItems);
        registerHelper(BuiltInRegistries.SOUND_EVENT, Sounds::registerSounds);

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new FabricMelody());

        ServerLifecycleEvents.SERVER_STARTING.register(server -> ServerMelodyManager.server = server);

        CreativeModeTab group = FabricCreativeModeTab.builder()
                .title(ItemGroups.getDisplayName())
                .icon(ItemGroups::getIcon)
                .displayItems((enabledFeatures, entries) -> entries.acceptAll(Items.getSortedItems()))
                .build();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Common.locate("group"), group);

        Network.register(fabricRegistrar);
        Network.registerSender(ServerPlayNetworking::send);
    }

    Network.Registrar fabricRegistrar = new Network.Registrar() {
        @Override
        public <T extends ImmersivePayload> void register(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, boolean isServer) {
            if (isServer) {
                PayloadTypeRegistry.serverboundPlay().register(type, codec);
                ServerPlayNetworking.registerGlobalReceiver(type, (payload, ctx) -> ctx.server().execute(() -> payload.handle(ctx.player())));
            } else {
                PayloadTypeRegistry.clientboundPlay().register(type, codec);
                if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                    ClientProxy.register(type);
                }
            }
        }
    };

    private static final class ClientProxy {
        public static <T extends ImmersivePayload> void register(ImmersivePayload.Type<T> type) {
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, ctx) -> ctx.client().execute(() -> payload.handle(ctx.player())));
        }
    }
}

