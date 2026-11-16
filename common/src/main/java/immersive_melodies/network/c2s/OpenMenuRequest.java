package immersive_melodies.network.c2s;

import immersive_melodies.item.InstrumentItem;
import immersive_melodies.network.ImmersivePayload;
import immersive_melodies.network.Network;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.network.s2c.OpenGuiRequest;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record OpenMenuRequest() implements ImmersivePayload {
    public OpenMenuRequest(FriendlyByteBuf b) {
        this();
    }

    @Override
    public void encode(FriendlyByteBuf b) {
    }

    @Override
    public void handle(Player e) {
        for (ItemStack stack : e.getHandSlots()) {
            if (stack.getItem() instanceof InstrumentItem) {
                Network.sendToPlayer(new MelodyListMessage(e), (ServerPlayer) e);
                Network.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.SELECTOR), (ServerPlayer) e);
                return;
            }
        }
    }
}
