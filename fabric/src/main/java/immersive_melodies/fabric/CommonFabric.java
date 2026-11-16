package immersive_melodies.fabric;

import immersive_melodies.Common;
import immersive_melodies.ItemGroups;
import immersive_melodies.Items;
import immersive_melodies.Sounds;
import immersive_melodies.fabric.resources.FabricMelody;
import immersive_melodies.network.ImmersivePayload;
import immersive_melodies.network.Network;
import immersive_melodies.resources.ServerMelodyManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTab;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

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
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ServerMelodyManager.server = null);

        CreativeModeTab group = FabricItemGroup.builder()
                .title(ItemGroups.getDisplayName())
                .icon(ItemGroups::getIcon)
                .displayItems((enabledFeatures, entries) -> entries.acceptAll(Items.getSortedItems()))
                .build();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Common.locate("group"), group);

        Network.register(fabricRegistrar);
        Network.registerSender((payload, player) -> {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            payload.encode(buf);
            ServerPlayNetworking.send(player, fabricRegistrar.getMessageIdentifier(payload), buf);
        });
        Network.registerClientSender((payload) -> {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            payload.encode(buf);
            ClientPlayNetworking.send(fabricRegistrar.getMessageIdentifier(payload), buf);
        });
    }

    FabricRegistrar fabricRegistrar = new FabricRegistrar();

    public static class FabricRegistrar implements Network.Registrar {
        private final Map<Class<?>, ResourceLocation> identifiers = new HashMap<>();
        private int id = 0;

        private <T> ResourceLocation createMessageIdentifier(Class<T> msg) {
            return Common.locate(msg.getSimpleName().toLowerCase(Locale.ROOT).substring(0, 8) + id++);
        }

        private ResourceLocation getMessageIdentifier(ImmersivePayload msg) {
            return Objects.requireNonNull(identifiers.get(msg.getClass()), "Used unregistered message!");
        }

        @Override
        public <T extends ImmersivePayload> void register(Class<T> msg, Function<FriendlyByteBuf, T> constructor) {
            ResourceLocation identifier = createMessageIdentifier(msg);
            identifiers.put(msg, identifier);

            ServerPlayNetworking.registerGlobalReceiver(identifier, (server, player, handler, buffer, responder) -> {
                ImmersivePayload m = constructor.apply(buffer);
                server.execute(() -> m.handle(player));
            });

            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientProxy.register(identifier, constructor);
            }
        }
    }

    private static final class ClientProxy {
        public static <T extends ImmersivePayload> void register(ResourceLocation id, Function<FriendlyByteBuf, T> constructor) {
            ClientPlayNetworking.registerGlobalReceiver(id, (client, ignore1, buffer, ignore2) -> {
                ImmersivePayload m = constructor.apply(buffer);
                client.execute(() -> m.handle(client.player));
            });
        }
    }
}

