package immersive_melodies;


import immersive_melodies.cobalt.network.NetworkHandler;
import immersive_melodies.network.c2s.ItemActionMessage;
import immersive_melodies.network.c2s.MelodyDeleteRequest;
import immersive_melodies.network.c2s.MelodyRequest;
import immersive_melodies.network.c2s.UploadMelodyRequest;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.network.s2c.MelodyResponse;
import immersive_melodies.network.s2c.OpenGuiRequest;

public class Messages {
    public static void bootstrap() {
        // nop
    }

    static {
        NetworkHandler.registerMessage(MelodyRequest.class, MelodyRequest::new);
        NetworkHandler.registerMessage(MelodyListMessage.class, MelodyListMessage::new);
        NetworkHandler.registerMessage(MelodyResponse.class, MelodyResponse::new);
        NetworkHandler.registerMessage(UploadMelodyRequest.class, UploadMelodyRequest::new);
        NetworkHandler.registerMessage(OpenGuiRequest.class, OpenGuiRequest::new);
        NetworkHandler.registerMessage(ItemActionMessage.class, ItemActionMessage::new);
        NetworkHandler.registerMessage(MelodyDeleteRequest.class, MelodyDeleteRequest::new);
    }
}
