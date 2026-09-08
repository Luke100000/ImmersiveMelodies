package immersive_melodies;

import immersive_melodies.client.sound.CancelableSoundInstance;
import immersive_melodies.client.sound.SoundManagerImpl;
import immersive_melodies.item.InstrumentItem;
import immersive_melodies.network.ClientNetworkManager;
import immersive_melodies.network.Network;
import immersive_melodies.network.c2s.ItemActionMessage;
import immersive_melodies.network.c2s.NoteBroadcastRequest;
import immersive_melodies.resources.Note;
import immersive_melodies.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Client {
    public static void postLoad() {
        Minecraft client = Minecraft.getInstance();
        Common.networkManager = new ClientNetworkManager();
        Common.soundManager = new SoundManagerImpl(client);

        MidiListener.launch();
    }

    private static final Map<Integer, CancelableSoundInstance> playingSounds = new ConcurrentHashMap<>();

    public static void playNote(int tone, int velocity) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && !client.isPaused() && playNote(client.player, tone, velocity)) {
            Network.sendToServer(new NoteBroadcastRequest(tone, velocity));
        }
    }

    public static boolean playNote(LivingEntity entity, int tone, int velocity) {
        if (entity instanceof LocalPlayer) {
            for (ItemStack handStack : Utils.getHandItems(entity)) {
                if (handStack.getItem() instanceof InstrumentItem instrument && instrument.isPlaying(handStack)) {
                    Network.sendToServer(ItemActionMessage.fromState(ItemActionMessage.State.PAUSE));
                    break;
                }
            }
        }
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.getItem() instanceof InstrumentItem instrument) {
                if (velocity > 0) {
                    if (!playingSounds.containsKey(tone)) {
                        Note note = new Note(tone, velocity, 0, 10_000, 200);
                        CancelableSoundInstance soundInstance = instrument.playNote(entity, note, 0);
                        playingSounds.put(tone, soundInstance);
                        return true;
                    }
                } else {
                    CancelableSoundInstance soundInstance = playingSounds.get(tone);
                    if (soundInstance != null) {
                        soundInstance.cancel();
                        playingSounds.remove(tone);
                        return true;
                    }
                }
                break;
            }
        }
        return false;
    }
}
