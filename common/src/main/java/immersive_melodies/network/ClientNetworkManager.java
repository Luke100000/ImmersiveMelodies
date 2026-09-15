package immersive_melodies.network;

import immersive_melodies.Client;
import immersive_melodies.client.gui.ImmersiveMelodiesScreen;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.network.s2c.NoteMessage;
import immersive_melodies.network.s2c.OpenGuiRequest;
import immersive_melodies.resources.ClientMelodyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ClientNetworkManager implements NetworkManager {
    @Override
    public void handleOpenGuiRequest(OpenGuiRequest request) {
        Minecraft.getInstance().gui.setScreen(new ImmersiveMelodiesScreen());
    }

    @Override
    public void handleMelodyListMessage(MelodyListMessage response) {
        ClientMelodyManager.getMelodiesList().clear();
        ClientMelodyManager.getMelodiesList().putAll(response.melodies());

        if (Minecraft.getInstance().gui.screen() instanceof ImmersiveMelodiesScreen screen) {
            screen.refreshPage();
        }
    }

    @Override
    public void handleNoteMessage(NoteMessage e) {
        Minecraft client = Minecraft.getInstance();
        Entity entity = client.level != null ? client.level.getEntity(e.entity()) : null;
        if (entity instanceof LivingEntity livingEntity) {
            Client.playNote(livingEntity, e.tone(), e.velocity());
        }
    }
}
