package immersive_melodies.forge.cobalt.network;

import immersive_melodies.Common;
import immersive_melodies.cobalt.network.Message;
import immersive_melodies.cobalt.network.NetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
    private static final String PROTOCOL_VERSION = "1";

    private final SimpleChannel channel = NetworkRegistry.newSimpleChannel(
            new Identifier(Common.SHORT_MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private int id = 0;

    @Override
    public <T extends Message> void registerMessage(Class<T> msg, Function<PacketByteBuf, T> constructor) {
        channel.registerMessage(id++, msg,
                Message::encode,
                constructor,
                (m, ctx) -> {
                    ctx.get().enqueueWork(() -> {
                        ServerPlayerEntity sender = ctx.get().getSender();
                        m.receive(sender);
                    });
                    ctx.get().setPacketHandled(true);
                });
    }

    @Override
    public void sendToServer(Message m) {
        channel.sendToServer(m);
    }

    @Override
    public void sendToPlayer(Message m, ServerPlayerEntity e) {
        channel.send(PacketDistributor.PLAYER.with(() -> e), m);
    }
}
