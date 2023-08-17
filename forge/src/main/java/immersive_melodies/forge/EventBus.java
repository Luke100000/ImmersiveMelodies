package immersive_melodies.forge;

import immersive_melodies.Client;
import immersive_melodies.Common;
import immersive_melodies.resources.MelodyLoader;
import immersive_melodies.resources.ServerMelodyManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Common.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventBus {
    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        ServerMelodyManager.server = event.getServer();
    }

    public static boolean firstLoad = true;

    @SubscribeEvent
    public static void onClientStart(TickEvent.ClientTickEvent event) {
        //forge decided to be funny and won't trigger the client load event
        if (firstLoad) {
            Client.postLoad();
            firstLoad = false;
        }
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new MelodyLoader());
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent event) {
        Common.timer.getTime();
    }
}
