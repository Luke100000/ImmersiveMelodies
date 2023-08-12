package immersive_melodies.item;

import immersive_melodies.Common;
import immersive_melodies.cobalt.network.NetworkHandler;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.network.s2c.OpenGuiRequest;
import immersive_melodies.resources.ClientMelodyManager;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.Note;
import net.minecraft.block.enums.Instrument;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InstrumentItem extends Item {
    public InstrumentItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            NetworkHandler.sendToPlayer(new MelodyListMessage(), (ServerPlayerEntity) user);
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.SELECTOR), (ServerPlayerEntity) user);
        }

        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {

        // State
        if (isPlaying(stack)) {
            tooltip.add(Text.translatable("immersive_melodies.playing").formatted(Formatting.GREEN));
        }

        // Name
        Melody melody = getMelody(stack);
        tooltip.add(Text.literal(melody.getName()).formatted(Formatting.ITALIC));

        super.appendTooltip(stack, world, tooltip, context);
    }

    public boolean isPlaying(ItemStack stack) {
        return stack.getOrCreateNbt().getBoolean("playing");
    }

    public Melody getMelody(ItemStack stack) {
        String identifier = stack.getOrCreateNbt().getString("melody");
        return ClientMelodyManager.getMelody(new Identifier(identifier));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (isPlaying(stack)) {
            NbtCompound nbt = stack.getOrCreateNbt();
            long progress = nbt.getLong("progress");
            progress += 50;
            nbt.putLong("progress", progress);

            if (world.isClient) {
                Melody melody = getMelody(stack);
                // todo optimize
                for (int i = 0; i < melody.getNotes().size(); i++) {
                    Note note = melody.getNotes().get(i);
                    if (progress >= note.getTime() && progress < note.getTime() + 50) {
                        float volume = note.getVelocity() / 255.0f * 3.0f;
                        float pitch = (float) Math.pow(2, (note.getNote() - 72) / 12.0);
                        float length = note.getLength() / 1000.0f;
                        Common.soundManager.playSound(entity.getX(), entity.getY(), entity.getZ(), Instrument.XYLOPHONE.getSound().value(), SoundCategory.RECORDS, volume, pitch, length);
                    }
                }
            }
        }
    }

    public void play(ItemStack stack, Identifier melody) {
        stack.getOrCreateNbt().putString("melody", melody.toString());
        stack.getOrCreateNbt().putLong("progress", 0);
        stack.getOrCreateNbt().putBoolean("playing", true);
    }

    public void play(ItemStack stack) {
        stack.getOrCreateNbt().putBoolean("playing", true);
    }

    public void pause(ItemStack stack) {
        stack.getOrCreateNbt().putBoolean("playing", false);
    }
}
