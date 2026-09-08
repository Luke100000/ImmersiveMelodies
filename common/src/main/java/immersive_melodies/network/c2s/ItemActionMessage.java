package immersive_melodies.network.c2s;

import immersive_melodies.Common;
import immersive_melodies.util.Utils;
import immersive_melodies.item.InstrumentItem;
import immersive_melodies.network.ImmersivePayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntFunction;

public record ItemActionMessage(State state, Identifier melody) implements ImmersivePayload {
    public static final Type<ItemActionMessage> TYPE = new CustomPacketPayload.Type<>(Common.locate("item_action_message"));
    public static final StreamCodec<FriendlyByteBuf, ItemActionMessage> STREAM_CODEC = StreamCodec.composite(
            State.STREAM_CODEC, ItemActionMessage::state,
            Identifier.STREAM_CODEC, ItemActionMessage::melody,
            ItemActionMessage::new
    );
    public static ItemActionMessage fromStateAndMelody(State state, Identifier melody) {
        return new ItemActionMessage(state, melody);
    }

    public static ItemActionMessage fromState(State state) {
        return new ItemActionMessage(state, Identifier.withDefaultNamespace("empty"));
    }

    @Override
    public void handle(Player e) {
        Utils.getHandItems(e).forEach(stack -> {
            if (stack.getItem() instanceof InstrumentItem instrument) {
                switch (state) {
                    case PLAY -> instrument.play(stack, melody, e.level(), e);
                    case CONTINUE -> instrument.play(stack, e.level());
                    case PAUSE -> instrument.pause(stack, e.level());
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum State {
        PLAY(0),
        CONTINUE(1),
        PAUSE(2);

        State(int id) {
            this.id = id;
        }

        private final int id;

        public int id() {
            return this.id;
        }

        public static final IntFunction<State> BY_ID = ByIdMap.continuous(State::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, State> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, State::id);
    }
}
