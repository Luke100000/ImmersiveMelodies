package immersive_melodies.network.c2s;

import immersive_melodies.cobalt.network.Message;
import immersive_melodies.item.InstrumentItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ItemActionMessage extends Message {
    private final int slot;
    private final State state;
    private final Identifier melody;

    public ItemActionMessage(State state) {
        this(state, new Identifier("_"));
    }

    public ItemActionMessage(State state, Identifier melody) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        slot = player == null ? -1 : player.getInventory().selectedSlot;
        this.state = state;
        this.melody = melody;
    }

    public ItemActionMessage(PacketByteBuf b) {
        slot = b.readInt();
        state = b.readEnumConstant(State.class);
        melody = b.readIdentifier();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeInt(slot);
        b.writeEnumConstant(state);
        b.writeIdentifier(melody);
    }

    @Override
    public void receive(PlayerEntity e) {
        ItemStack stack = e.getInventory().getStack(slot);
        if (stack.getItem() instanceof InstrumentItem instrument) {
            if (!melody.getPath().equals("_")) {
                instrument.play(stack, melody);
            } else if (state == State.PAUSE) {
                instrument.pause(stack);
            } else {
                instrument.play(stack);
            }
        }
    }

    public enum State {
        PLAY,
        PAUSE
    }
}