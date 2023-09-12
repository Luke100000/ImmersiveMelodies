package immersive_melodies.network.c2s;

import immersive_melodies.cobalt.network.NetworkHandler;
import immersive_melodies.network.FragmentedMessage;
import immersive_melodies.network.PacketSplitter;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.ServerMelodyManager;
import immersive_melodies.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class UploadMelodyRequest extends FragmentedMessage {
    public UploadMelodyRequest(String name, byte[] fragment, int length) {
        super(name, fragment, length);
    }

    public UploadMelodyRequest(PacketByteBuf b) {
        super(b);
    }

    @Override
    protected void finish(PlayerEntity e, String name, Melody melody) {
        String id = Utils.getPlayerName(e) + "/" + Utils.escapeString(name);
        Identifier identifier = new Identifier("player", id);

        // Register
        ServerMelodyManager.registerMelody(
                identifier,
                melody
        );

        // Update the index
        NetworkHandler.sendToPlayer(new MelodyListMessage(e), (ServerPlayerEntity) e);

        // Send the melody to all players
        e.getWorld().getPlayers().forEach(player -> {
            PacketSplitter.sendToPlayer(identifier, melody, (ServerPlayerEntity) player);
        });
    }
}
