package immersive_melodies.neoforge;

import immersive_melodies.*;
import immersive_melodies.network.ImmersivePayload;
import immersive_melodies.network.Network;
import immersive_melodies.resources.MelodyLoader;
import immersive_melodies.resources.ServerMelodyManager;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

import static net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB;

@Mod(Common.MOD_ID)
@EventBusSubscriber(modid = Common.MOD_ID)
public final class CommonNeoForge {
    private static <T> void registerHelper(RegisterEvent event, Registry<T> register, Consumer<Common.RegisterHelper<T>> consumer) {
        event.register(register.key(), registry -> consumer.accept(registry::register));
    }

    public CommonNeoForge(IEventBus bus) {
        DEF_REG.register(bus);
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        registerHelper(event, BuiltInRegistries.ITEM, Items::registerItems);
        registerHelper(event, BuiltInRegistries.SOUND_EVENT, Sounds::registerSounds);
    }

    public static final DeferredRegister<CreativeModeTab> DEF_REG = DeferredRegister.create(CREATIVE_MODE_TAB, Common.MOD_ID);

    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = DEF_REG.register(Common.MOD_ID, () -> CreativeModeTab.builder()
            .title(ItemGroups.getDisplayName())
            .icon(ItemGroups::getIcon)
            .displayItems((featureFlags, output) -> output.acceptAll(Items.getSortedItems()))
            .build()
    );

    static class NeoForgeRegistrar implements Network.Registrar {
        PayloadRegistrar registrar;

        public NeoForgeRegistrar(PayloadRegistrar registrar) {
            this.registrar = registrar;
        }

        @Override
        public <T extends ImmersivePayload> void register(ImmersivePayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, boolean isServer) {
            if (isServer) {
                registrar.playToServer(type, codec, (payload, ctx) -> ctx.enqueueWork(() -> payload.handle(ctx.player())));
            } else {
                registrar.playToClient(type, codec, (payload, ctx) -> ctx.enqueueWork(() -> payload.handle(ctx.player())));
            }
        }
    }

    @SubscribeEvent
    public static void registerNetwork(final RegisterPayloadHandlersEvent event) {
        Network.register(new NeoForgeRegistrar(event.registrar("1")));
        Network.registerSender(PacketDistributor::sendToPlayer);
        Network.registerClientSender(net.neoforged.neoforge.client.network.ClientPacketDistributor::sendToServer);
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        ServerMelodyManager.server = event.getServer();
    }

    public static boolean firstLoad = true;

    @SubscribeEvent
    public static void onClientStart(ClientTickEvent.Pre event) {
        //forge decided to be funny and won't trigger the client load event
        if (firstLoad) {
            Client.postLoad();
            firstLoad = false;
        }
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(Common.locate("melody"), new MelodyLoader());
    }
}
