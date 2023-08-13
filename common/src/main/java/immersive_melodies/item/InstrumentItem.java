package immersive_melodies.item;

import immersive_melodies.Common;
import immersive_melodies.client.MelodyProgressHandler;
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

    private static final MelodyProgressHandler melodyProgressHandler = new MelodyProgressHandler();

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

        // Progress
        //melodyProgressHandler.getAndAdvanceProgress()
        tooltip.add(Text.literal(String.valueOf(stack.getOrCreateNbt().getLong("progress"))).formatted(Formatting.ITALIC).formatted(Formatting.GRAY));

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

        // check if player holds it
        boolean isHolding = false;
        for (ItemStack handItem : entity.getHandItems()) {
            if (handItem == stack) {
                isHolding = true;
                break;
            }
        }

        // play
        if (isPlaying(stack) && isHolding && world.isClient) {
            long progress = melodyProgressHandler.getAndAdvanceProgress(entity, stack);
            Melody melody = getMelody(stack);
            // todo optimize
            for (int i = 0; i < melody.getNotes().size(); i++) {
                Note note = melody.getNotes().get(i);
                if (progress >= note.getTime() && progress < note.getTime() + 50) {
                    float volume = note.getVelocity() / 255.0f * 3.0f;
                    float pitch = (float) Math.pow(2, (note.getNote() - 66) / 12.0) * 4.0f; // todo
                    long length = note.getLength();
                    Common.soundManager.playSound(entity.getX(), entity.getY(), entity.getZ(), Instrument.BELL.getSound().value(), SoundCategory.RECORDS, volume, pitch, length, entity);
                }
            }
        }
    }

    public void play(ItemStack stack, Identifier melody, World world) {
        stack.getOrCreateNbt().putString("melody", melody.toString());
        stack.getOrCreateNbt().putBoolean("playing", true);
        stack.getOrCreateNbt().putLong("start_time", world.getTime());
    }

    public void play(ItemStack stack, World world) {
        stack.getOrCreateNbt().putBoolean("playing", true);
        stack.getOrCreateNbt().putLong("start_time", world.getTime());
    }

    public void pause(ItemStack stack) {
        stack.getOrCreateNbt().putBoolean("playing", false);
    }
}
