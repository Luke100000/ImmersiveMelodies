package immersive_melodies.network;

import immersive_melodies.cobalt.network.NetworkHandler;
import immersive_melodies.network.c2s.UploadMelodyRequest;
import immersive_melodies.network.s2c.MelodyResponse;
import immersive_melodies.resources.Melody;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;

public class PacketSplitter {
    private static final int FRAGMENT_SIZE = 8192;

    private static List<byte[]> fragmentate(Melody melody) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        melody.encode(buffer);
        byte[] array = new byte[buffer.writerIndex()];
        buffer.getBytes(0, array);

        List<byte[]> fragments = new LinkedList<>();
        for (int i = 0; i < array.length; i += FRAGMENT_SIZE) {
            byte[] fragment = new byte[Math.min(FRAGMENT_SIZE, array.length - i)];
            System.arraycopy(array, i, fragment, 0, fragment.length);
            fragments.add(fragment);
        }
        return fragments;
    }

    /**
     * Splits into smaller packets and sends to the server
     *
     * @param name   The name of the melody
     * @param melody The melody to send
     */
    public static void sendToServer(String name, Melody melody) {
        List<byte[]> fragments = fragmentate(melody);
        int length = fragments.stream().mapToInt(f -> f.length).sum();
        for (byte[] fragment : fragments) {
            NetworkHandler.sendToServer(new UploadMelodyRequest(name, fragment, length));
        }
    }

    /**
     * Splits into smaller packets and sends to the player
     *
     * @param identifier The identifier of the melody
     * @param melody     The melody to send
     * @param player     The player to send the melody to
     */
    public static void sendToPlayer(Identifier identifier, Melody melody, ServerPlayerEntity player) {
        List<byte[]> fragments = fragmentate(melody);
        int length = fragments.stream().mapToInt(f -> f.length).sum();
        for (byte[] fragment : fragments) {
            NetworkHandler.sendToPlayer(new MelodyResponse(identifier, fragment, length), player);
        }
    }
}
