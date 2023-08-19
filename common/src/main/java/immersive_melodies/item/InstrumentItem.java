package immersive_melodies.item;

import immersive_melodies.Common;
import immersive_melodies.Sounds;
import immersive_melodies.client.MelodyProgressHandler;
import immersive_melodies.cobalt.network.NetworkHandler;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.network.s2c.OpenGuiRequest;
import immersive_melodies.resources.ClientMelodyManager;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.Note;
import immersive_melodies.resources.ServerMelodyManager;
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
    private final Sounds.Instrument sound;
    private final long sustain;

    public InstrumentItem(Settings settings, Sounds.Instrument sound, long sustain) {
        super(settings);

        this.sound = sound;
        this.sustain = sustain;
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
        if (!melody.getName().equals("unknown")) {
            tooltip.add(Text.literal(melody.getName()).formatted(Formatting.ITALIC));
        }

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

        // autoplay
        if (!world.isClient && !(entity instanceof PlayerEntity) && !isPlaying(stack)) {
            Identifier randomMelody = ServerMelodyManager.getRandomMelody();
            play(stack, randomMelody, world);
        }

        // play
        if (isPlaying(stack) && selected && world.isClient) {
            long time = MelodyProgressHandler.INSTANCE.getProgress(entity, stack);
            Melody melody = getMelody(stack);
            for (int i = MelodyProgressHandler.INSTANCE.getProgress(entity).getLastIndex(); i < melody.getNotes().size(); i++) {
                Note note = melody.getNotes().get(i);
                if (time >= note.getTime()) {
                    float volume = note.getVelocity() / 255.0f * 3.0f;
                    float pitch = (float) Math.pow(2, (note.getNote() - 24) / 12.0);
                    int octave = 1;
                    while (octave < 8 && pitch > 4.0 / 3.0) {
                        pitch /= 2;
                        octave++;
                    }
                    long length = note.getLength();
                    long sustain = Math.min(this.sustain, note.getSustain());

                    Common.soundManager.playSound(entity.getX(), entity.getY(), entity.getZ(),
                            sound.get(octave), SoundCategory.RECORDS,
                            volume, pitch, length, sustain,
                            note.getTime() - time, entity);

                    MelodyProgressHandler.INSTANCE.setLastNote(entity, volume, pitch, length);

                    if (i == melody.getNotes().size() - 1) {
                        if (entity instanceof PlayerEntity) {
                            MelodyProgressHandler.INSTANCE.setLastIndex(entity, i + 1);
                        } else {
                            // other entities loop
                            rewind(stack, world);
                        }
                    }
                } else {
                    MelodyProgressHandler.INSTANCE.setLastIndex(entity, i);
                    break;
                }
            }
        }
    }

    public void play(ItemStack stack, Identifier melody, World world) {
        stack.getOrCreateNbt().putString("melody", melody.toString());
        stack.getOrCreateNbt().putBoolean("playing", true);
        stack.getOrCreateNbt().putLong("start_time", world.getTime());
    }

    public void rewind(ItemStack stack, World world) {
        stack.getOrCreateNbt().putLong("start_time", world.getTime());
    }

    public void play(ItemStack stack) {
        stack.getOrCreateNbt().putBoolean("playing", true);
    }

    public void pause(ItemStack stack) {
        stack.getOrCreateNbt().putBoolean("playing", false);
    }
}
